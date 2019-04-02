package org.jackson.puppy.demo.dubbo.confirm.controller;

import org.jackson.puppy.demo.dubbo.confirm.service.impl.ConfirmServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

/**
 * @ author Kevin Hwang
 * @ date 4/2/19
 */
@RestController
@RequestMapping(value = "/mock")
public class MockController {

	private final static int THREAD_SIZE = 1000;
	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConfirmServiceImpl confirmService;

    @Value(value = "${host.order}")
    private String orderHost;

    @GetMapping(value = "/sendConfirm")
    public String mockSendConfirm() throws InterruptedException {
	CountDownLatch ready = new CountDownLatch(THREAD_SIZE);

		CountDownLatch begin = new CountDownLatch(1);

		CountDownLatch finish = new CountDownLatch(THREAD_SIZE);


		for (int i = 0; i < THREAD_SIZE; i++) {
			int threadIndex = i;
			new Thread(() -> {
				ready.countDown();
				try {
					begin.await();
				} catch (InterruptedException e) {
				}
				try {
					confirmService.sendConfirm(String.format("ord%06d", threadIndex), BigDecimal.ONE, new BigDecimal(100));
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
        return "ok";
    }

	@GetMapping(value = "/rollBack")
    public String mockRollBack() throws InterruptedException {

		String home = String.format("http://%s:9002", orderHost);

		long start = System.currentTimeMillis();

		CountDownLatch finish = new CountDownLatch(THREAD_SIZE);

		RestTemplate restTemplate = new RestTemplate();
		for (int i = 0; i < THREAD_SIZE; i++) {
			int threadIndex = i;
			new Thread(() -> {
				String url = home + "/rollBack" + String.format("/orderNumber/ord%06d", threadIndex);
				try {
					confirmService.deleteByOrderNumber(String.format("ord%06d", threadIndex));
					restTemplate.getForObject(url, String.class);
				} finally {
					finish.countDown();

				}

			}).start();
		}
		finish.await();
		logger.info("eclipsed time {}", System.currentTimeMillis() - start);
		return "ok";
	}
}
