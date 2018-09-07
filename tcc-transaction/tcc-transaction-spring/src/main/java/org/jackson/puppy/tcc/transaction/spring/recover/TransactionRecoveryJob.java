package org.jackson.puppy.tcc.transaction.spring.recover;

import org.jackson.puppy.tcc.transaction.recover.TransactionRecovery;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TransactionRecoveryJob extends QuartzJobBean {

	@Autowired
	private TransactionRecovery transactionRecovery;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		transactionRecovery.startRecover();
	}
}
