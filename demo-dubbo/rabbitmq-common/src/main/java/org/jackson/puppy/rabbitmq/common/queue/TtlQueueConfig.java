package org.jackson.puppy.rabbitmq.common.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Configuration
public class TtlQueueConfig {

	@Bean
	public DirectExchange dispatcherDirectExchange() {
		return (DirectExchange) ExchangeBuilder
				.directExchange(TtlQueue.DISPATCHER_EXCHANGE)
				.durable(true)
				.build();
	}

	@Bean
	public Queue dispatcherQueue() {
		return QueueBuilder
				.durable(TtlQueue.DISPATCHER_QUEUE)
				.build();
	}

	@Bean
	public Binding dispatcherBinding() {
		return BindingBuilder
				.bind(dispatcherQueue())
				.to(dispatcherDirectExchange())
				.with(TtlQueue.DISPATCHER_ROUTE_KEY);
	}

	@Bean
	public DirectExchange ttlDirectExchange() {
		return (DirectExchange) ExchangeBuilder
				.directExchange(TtlQueue.TTL_EXCHANGE)
				.durable(true)
				.build();
	}

	@Bean
	public Queue ttlQueue() {
		return QueueBuilder
				.durable(TtlQueue.TTL_QUEUE)
				.withArgument("x-dead-letter-exchange", TtlQueue.DISPATCHER_EXCHANGE)
				.withArgument("x-dead-letter-routing-key", TtlQueue.DISPATCHER_ROUTE_KEY)
				.build();
	}

	@Bean
	public Binding ttlQueueBinding() {
		return BindingBuilder
				.bind(ttlQueue())
				.to(ttlDirectExchange())
				.with(TtlQueue.TTL_ROUTE_KEY);
	}

}
