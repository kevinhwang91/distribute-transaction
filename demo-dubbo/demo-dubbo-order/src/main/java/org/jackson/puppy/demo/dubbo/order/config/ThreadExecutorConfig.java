package org.jackson.puppy.demo.dubbo.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Configuration
public class ThreadExecutorConfig {

	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(200);
		executor.setMaxPoolSize(400);
		executor.setQueueCapacity(1000);
		executor.setThreadNamePrefix("order-subThread-");
		executor.initialize();
		return executor;
	}
}
