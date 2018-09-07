package org.jackson.puppy.rabbitmq.common.queue;

import com.rabbitmq.client.Channel;
import org.jackson.puppy.rabbitmq.common.dto.MqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Component
public class DispatcherHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Producer producer;

	@SuppressWarnings("unchecked")
	public static Long getRetryCount(MessageProperties messageProperties) {
		return Optional.ofNullable(messageProperties.getHeaders())
				.map(headers -> (List<Map<String, Object>>) headers.get("x-death"))
				.map(mapList -> mapList.get(0))
				.map(map -> (Long) map.get("count"))
				.orElse(0L);
	}

	@RabbitListener(queues = "dispatcher.queue")
	public void dispatcherHandler(Message message, Channel channel) throws IOException {
		boolean handle = true;
		MessageProperties messageProperties = message.getMessageProperties();
		long deliveryTag = messageProperties.getDeliveryTag();

		Map<String, Object> headers = messageProperties.getHeaders();
		String exchange = (String) headers.get("original-exchange");
		String routingKey = (String) headers.get("original-routingKey");

		if (StringUtils.isEmpty(exchange) || StringUtils.isEmpty(routingKey)) {
			logger.info("exchange : {}, routingKey : {}. stop to dispatcher queue.");
		} else {
			try {
				MqMessage mqMessage = new MqMessage(message, exchange, routingKey);
				producer.sendSyncWaitConfirm(channel, mqMessage, 2 * 1000);
			} catch (InterruptedException | IOException | TimeoutException e) {
				handle = false;
				logger.error(e.getMessage());
			}
		}

		if (handle) {
			channel.basicAck(deliveryTag, false);
			logger.info("Dispatch exchange : {}, routingKey : {} completed.", exchange, routingKey);
		} else {
			channel.basicNack(deliveryTag, false, true);
		}
	}

}
