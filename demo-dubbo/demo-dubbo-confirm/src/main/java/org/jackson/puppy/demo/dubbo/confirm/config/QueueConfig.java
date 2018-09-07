package org.jackson.puppy.demo.dubbo.confirm.config;

import org.jackson.puppy.demo.dubbo.confirm.api.constant.ConfirmQueue;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Configuration
public class QueueConfig {

	@Bean
	public DirectExchange confirmDirectExchange() {
		return (DirectExchange) ExchangeBuilder
				.directExchange(ConfirmQueue.CONFIRM_EXCHANGE)
				.durable(true)
				.build();
	}

	@Bean
	public Queue confirmQueue() {
		return QueueBuilder
				.durable(ConfirmQueue.CONFIRM_QUEUE)
				.build();
	}

	@Bean
	public Binding confirmQueueBinding() {
		return BindingBuilder
				.bind(confirmQueue())
				.to(confirmDirectExchange())
				.with(ConfirmQueue.CONFIRM_ROUTE_KEY);
	}

}
