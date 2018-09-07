package org.jackson.puppy.rabbitmq.common.queue;

import com.github.benmanes.caffeine.cache.Cache;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.jackson.puppy.rabbitmq.common.dto.CallBackContext;
import org.jackson.puppy.rabbitmq.common.dto.MqMessage;
import org.jackson.puppy.rabbitmq.common.dto.MqMessageWithDelay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Component
public class Producer implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private Cache<String, CallBackContext> cache;

	@PostConstruct
	public void init() {
		rabbitTemplate.setConfirmCallback(this);
		rabbitTemplate.setReturnCallback(this);
	}

	public void send(MqMessage msg) {
		Objects.requireNonNull(msg);

		String correlationId = UUID.randomUUID().toString();
		CallBackContext callBackContext = msg.getCache();
		Optional.ofNullable(callBackContext).ifPresent(c ->
				cache.put(correlationId, callBackContext));

		rabbitTemplate.convertAndSend(msg.getExchange(), msg.getRouteKey(), msg.getMessage(), message -> {
			final MessageProperties messageProperties = message.getMessageProperties();
			messageProperties.setCorrelationId(correlationId);
			return message;
		}, new CorrelationData(correlationId));
	}

	public void sendSyncWaitConfirm(Channel channel, MqMessage msg, long timeOut) throws IOException, InterruptedException, TimeoutException {
		Objects.requireNonNull(msg);

		String correlationId = UUID.randomUUID().toString();
		MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
		Message message = converterMessage(messageConverter, msg.getMessage());

		MessageProperties messageProperties = message.getMessageProperties();
		AMQP.BasicProperties convertedMessageProperties = new DefaultMessagePropertiesConverter()
				.fromMessageProperties(messageProperties, "UTF-8");

		channel.confirmSelect();
		channel.basicPublish(msg.getExchange(), msg.getRouteKey(), true, convertedMessageProperties, message.getBody());
		if (!channel.waitForConfirms(timeOut)) {
			throw new IOException(String.format("Message %s confirm had failed.", correlationId));
		}
	}

	public void sendDelaySyncWaitConfirm(Channel channel, MqMessageWithDelay msg, long timeOut) throws IOException, InterruptedException, TimeoutException {
		Objects.requireNonNull(msg);

		String correlationId = UUID.randomUUID().toString();
		MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
		Message message = converterMessage(messageConverter, msg.getMessage());

		MessageProperties messageProperties = message.getMessageProperties();
		messageProperties.setCorrelationId(correlationId);
		messageProperties.setHeader("original-exchange", msg.getExchange());
		messageProperties.setHeader("original-routingKey", msg.getRouteKey());
		messageProperties.setExpiration(String.valueOf(msg.getTimeExpiration()));

		AMQP.BasicProperties convertedMessageProperties = new DefaultMessagePropertiesConverter()
				.fromMessageProperties(messageProperties, "UTF-8");

		channel.confirmSelect();
		channel.basicPublish(TtlQueue.TTL_EXCHANGE, TtlQueue.TTL_ROUTE_KEY, true, convertedMessageProperties, message.getBody());
		if (!channel.waitForConfirms(timeOut)) {
			throw new IOException(String.format("message %s confirm had failed.", correlationId));
		}
	}

	private Message converterMessage(MessageConverter messageConverter, Object msg) {
		Message message;
		if (msg instanceof Message) {
			message = (Message) msg;
		} else {
			message = messageConverter.toMessage(msg, new MessageProperties());
		}
		return message;
	}


	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
		String key = correlationData.getId();

		if (ack) {
			logger.info("Message {} has arrived at broker!", key);
			if (key != null) {
				Optional.ofNullable(cache.getIfPresent(key)).ifPresent(c -> {
					callBackToConfirm(c);
					cache.invalidate(key);
					logger.info("Invalidate cache key: {}.", key);
				});
			}
		} else {
			logger.warn("Message {} fail to send to broker, caused by {}.", key, cause);
		}
	}

	private void callBackToConfirm(CallBackContext c) {
		Class targetClass = c.getTargetClass();
		String methodName = c.getMethodName();
		Class[] parameterTypes = c.getParameterTypes();
		Object[] args = c.getArgs();

		try {
			Method method = targetClass.getMethod(methodName, parameterTypes);
			if (Modifier.isStatic(method.getModifiers())) {
				method.invoke(null, args);
			} else {
				Object target;
				try {
					target = applicationContext.getBean(targetClass);
				} catch (NoSuchBeanDefinitionException e) {
					target = targetClass.newInstance();
				}
				method.invoke(target, args);
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
			logger.info(e.getMessage());
		}
	}

	@Override
	public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
		logger.info("message: {}, replyCode: {}, replyText: {}, exchange: {}, routingKey: {}.",
				message, replyCode, replyText, exchange, routingKey);

	}
}
