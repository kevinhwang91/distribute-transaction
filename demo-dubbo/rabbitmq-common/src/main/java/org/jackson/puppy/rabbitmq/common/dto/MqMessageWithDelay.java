package org.jackson.puppy.rabbitmq.common.dto;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class MqMessageWithDelay extends MqMessage {

	private long timeExpiration;

	public MqMessageWithDelay(Object message, String exchange, String routeKey, long timeExpiration) {
		super(message, exchange, routeKey);
		this.timeExpiration = timeExpiration;
	}

	public long getTimeExpiration() {
		return timeExpiration;
	}
}
