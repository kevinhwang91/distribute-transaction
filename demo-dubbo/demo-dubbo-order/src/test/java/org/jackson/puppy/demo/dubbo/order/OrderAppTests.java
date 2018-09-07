package org.jackson.puppy.demo.dubbo.order;

import org.jackson.puppy.demo.dubbo.order.api.service.OrderService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;

public class OrderAppTests {

	private final static int THREAD_SIZE = 1000;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void rollBack() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("application-context-dubbo-test.xml");

		OrderService orderService = context.getBean(OrderService.class);

		TestRestTemplate testRestTemplate = new TestRestTemplate();

		String home = "http://127.0.0.1:9001";

		long start = System.currentTimeMillis();

		CountDownLatch finish = new CountDownLatch(THREAD_SIZE);

		for (int i = 0; i < THREAD_SIZE; i++) {
			int threadIndex = i;
			new Thread(() -> {
				String url = home + "/rollBack" + String.format("/orderNumber/ord%06d", threadIndex);
				try {
					testRestTemplate.getForObject(url, String.class);
					orderService.rollBackOrder(String.format("ord%06d", threadIndex));
				} finally {
					finish.countDown();

				}

			}).start();
		}
		finish.await();
		logger.info("eclipsed time {}", System.currentTimeMillis() - start);

	}

	@Test
	public void sendConfirm() throws Exception {
		CountDownLatch ready = new CountDownLatch(THREAD_SIZE);

		CountDownLatch begin = new CountDownLatch(1);

		CountDownLatch finish = new CountDownLatch(THREAD_SIZE);

		TestRestTemplate testRestTemplate = new TestRestTemplate();

		String home = "http://127.0.0.1:9001";
		for (int i = 0; i < THREAD_SIZE; i++) {
			int threadIndex = i;
			new Thread(() -> {
				String url = home + "/send" + String.format("/orderNumber/ord%06d", threadIndex) + "/agencyFee/1" + "/unit/100";
				ready.countDown();
				try {
					begin.await();
				} catch (InterruptedException e) {
				}
				try {
					testRestTemplate.getForObject(url, String.class);
				} finally {
					finish.countDown();
				}
			}).start();
		}
		ready.await();
		long start = System.currentTimeMillis();
		begin.countDown();
		finish.await();
		logger.info("eclipsed time {}", System.currentTimeMillis() - start);
	}

}
