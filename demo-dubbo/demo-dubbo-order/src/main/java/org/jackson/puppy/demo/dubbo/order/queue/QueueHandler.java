package org.jackson.puppy.demo.dubbo.order.queue;

import com.rabbitmq.client.Channel;
import org.jackson.puppy.demo.dubbo.confirm.api.constant.ConfirmQueue;
import org.jackson.puppy.demo.dubbo.order.api.dto.OrderDto;
import org.jackson.puppy.demo.dubbo.order.api.service.OrderService;
import org.jackson.puppy.demo.dubbo.order.util.KryoSerializer;
import org.jackson.puppy.rabbitmq.common.dto.MqMessageWithDelay;
import org.jackson.puppy.rabbitmq.common.queue.DispatcherHandler;
import org.jackson.puppy.rabbitmq.common.queue.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Component
public class QueueHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Producer producer;

	@Autowired
	private OrderService orderService;

	@Autowired
	private ApplicationContext applicationContext;

	@RabbitListener(queues = ConfirmQueue.CONFIRM_QUEUE)
	public void confirmSelector(Message message, Channel channel) throws IOException {
		QueueHandler queueHandler = applicationContext.getBean(QueueHandler.class);
		queueHandler.processConfirmQueue(message, channel);
	}

	@Async("asyncExecutor")
	public void processConfirmQueue(Message message, Channel channel) throws IOException {
		boolean handle = true;
		MessageProperties messageProperties = message.getMessageProperties();
		long deliveryTag = messageProperties.getDeliveryTag();

		try {
			OrderDto orderDto = KryoSerializer.deSerializer(message.getBody(), OrderDto.class);
			orderService.handleOrder(orderDto.getOrderNumber(), orderDto);
		} catch (Throwable ex) {
			logger.warn("Throw Exception. Retry again handleOrder.", ex);
			Long retryCount = DispatcherHandler.getRetryCount(messageProperties);
			if (retryCount < 10) {
				MqMessageWithDelay msgWithDelay = new MqMessageWithDelay(message, ConfirmQueue.CONFIRM_EXCHANGE, ConfirmQueue.CONFIRM_ROUTE_KEY, 30 * 1000);
				try {
					//  发送数据用同步确认替代异步确认，保证消息发到MQ
					//  一旦抛出异常这条Message就能返回RabbitMq
					producer.sendDelaySyncWaitConfirm(channel, msgWithDelay, 1000);
				} catch (InterruptedException | TimeoutException | IOException e) {
					handle = false;
					logger.error(e.toString());
				}
			} else {
				//TODO
				logger.error("Please persistence the data!!");
			}
		} finally {
			if (handle) {
				channel.basicAck(deliveryTag, false);
			} else {
				channel.basicNack(deliveryTag, false, true);
			}
		}
	}
}
