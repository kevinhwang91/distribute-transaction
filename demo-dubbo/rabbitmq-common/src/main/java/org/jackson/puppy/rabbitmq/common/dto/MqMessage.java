package org.jackson.puppy.rabbitmq.common.dto;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class MqMessage {

	private Object message;

	private String exchange;

	private String routeKey;

	private CallBackContext cache;

	public MqMessage(Object message, String exchange, String routeKey) {
		this.message = message;
		this.exchange = exchange;
		this.routeKey = routeKey;
	}

	private MqMessage() {
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getRouteKey() {
		return routeKey;
	}

	public void setRouteKey(String routeKey) {
		this.routeKey = routeKey;
	}

	public CallBackContext getCache() {
		return cache;
	}

	public void setCache(CallBackContext cache) {
		this.cache = cache;
	}
}
