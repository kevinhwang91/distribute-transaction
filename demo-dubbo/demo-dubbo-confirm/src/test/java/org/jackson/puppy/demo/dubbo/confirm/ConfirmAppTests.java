package org.jackson.puppy.demo.dubbo.confirm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.amqp.rabbit.core.RabbitAdmin.QUEUE_MESSAGE_COUNT;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ImportAutoConfiguration(classes = RabbitAutoConfiguration.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
public class ConfirmAppTests {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private RabbitAdmin rabbitAdmin;

	@Before
	public void setupQueue() {
		Queue queue = QueueBuilder
				.nonDurable("test.queue")
				.build();

		DirectExchange exchange = (DirectExchange) ExchangeBuilder
				.directExchange("test.exchange")
				.durable(false)
				.build();

		Binding binding = BindingBuilder
				.bind(queue)
				.to(exchange)
				.with("test");

		rabbitAdmin.declareQueue(queue);
		rabbitAdmin.declareExchange(exchange);
		rabbitAdmin.declareBinding(binding);
	}

	//  丢包测试须知：需要在RabbitMq所在的服务器上设置丢包模拟不稳定的网络环境
	//  如iptables -I INPUT -p tcp --dport 目的端口(5672) -m statistic --mode random --probability 丢包概率(0.3) -j DROP
	//  太低的丢包率测不出想要的结果，太高的丢包率会抛出大量RuntimeException异常
	//  测试过程可能会比较久，触发了TCP的重传机制，发送端会阻塞，Have a drink?
	@Test
	public void packageLoss() throws InterruptedException {
		int MSG_NUM = 3000;

		int actualSent = 0;

		boolean isEnd;

		RabbitTemplate rabbitTemplate = rabbitAdmin.getRabbitTemplate();

		do {
			AtomicInteger ignore = new AtomicInteger(0);

			rabbitAdmin.purgeQueue("test.queue", true);

			for (int i = 0; i < MSG_NUM; i++) {
				try {
					rabbitTemplate.convertAndSend("test.exchange",
							"test", "testPackageLoss",
							new CorrelationData(UUID.randomUUID().toString()));
					if ((i + 1) % (MSG_NUM / 10) == 0) {
						logger.warn("Sent {}%.", (i + 1) * 100 / MSG_NUM);
					}
				} catch (RuntimeException re) {
					ignore.incrementAndGet();
					logger.error("Catch exception, ignore this transmit.", re);
				}
			}
			int sentNum = MSG_NUM - ignore.get();
			logger.warn("Sent {} msg.", sentNum);
			//  立刻获取queue数量不准确，等待一段时间再获取
			isEnd = true;
			for (int i = 0; i < 20; i++) {
				Thread.sleep(1000);
				Properties queueProperties = rabbitAdmin.getQueueProperties("test.queue");
				if (queueProperties != null) {
					actualSent = (Integer) queueProperties.get(QUEUE_MESSAGE_COUNT);
					if (actualSent == sentNum) {
						isEnd = false;
						break;
					}
				}
			}

		} while (!isEnd);

		logger.warn("Sent {} actual msg.", actualSent);

	}
}
