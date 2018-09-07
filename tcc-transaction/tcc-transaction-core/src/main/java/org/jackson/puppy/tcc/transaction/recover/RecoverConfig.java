package org.jackson.puppy.tcc.transaction.recover;

import java.util.Set;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public interface RecoverConfig {

	int getMaxRetryCount();

	int getRecoverDuration();

	String getCronExpression();

	Set<Class<? extends Exception>> getDelayCancelExceptions();

	void setDelayCancelExceptions(Set<Class<? extends Exception>> delayRecoverExceptions);
}
