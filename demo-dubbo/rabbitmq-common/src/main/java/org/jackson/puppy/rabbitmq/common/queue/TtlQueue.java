package org.jackson.puppy.rabbitmq.common.queue;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TtlQueue {

	public static final String DISPATCHER_EXCHANGE = "dispatcher.exchange";

	public static final String DISPATCHER_QUEUE = "dispatcher.queue";

	public static final String DISPATCHER_ROUTE_KEY = "dispatcher";

	public static final String TTL_EXCHANGE = "ttl.exchange";

	public static final String TTL_QUEUE = "ttl.queue";

	public static final String TTL_ROUTE_KEY = "ttl";
}
