package org.jackson.puppy.tcc.transaction.spring.recover;


import org.jackson.puppy.tcc.transaction.recover.RecoverConfig;

import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class DefaultRecoverConfig implements RecoverConfig {

	public static final RecoverConfig INSTANCE = new DefaultRecoverConfig();

	private int maxRetryCount = 30;

	private int recoverDuration = 120;

	private String cronExpression = "0 */1 * * * ?";

	private Set<Class<? extends Exception>> delayCancelExceptions = new HashSet<>();

	public DefaultRecoverConfig() {
		delayCancelExceptions.add(SocketTimeoutException.class);
	}

	@Override
	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	@Override
	public int getRecoverDuration() {
		return recoverDuration;
	}

	public void setRecoverDuration(int recoverDuration) {
		this.recoverDuration = recoverDuration;
	}

	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Override
	public Set<Class<? extends Exception>> getDelayCancelExceptions() {
		return this.delayCancelExceptions;
	}

	@Override
	public void setDelayCancelExceptions(Set<Class<? extends Exception>> delayCancelExceptions) {
		this.delayCancelExceptions.addAll(delayCancelExceptions);
	}
}
