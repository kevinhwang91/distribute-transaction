package org.jackson.puppy.tcc.transaction.spring;

import org.jackson.puppy.tcc.transaction.TransactionManager;
import org.jackson.puppy.tcc.transaction.TransactionRepository;
import org.jackson.puppy.tcc.transaction.recover.RecoverConfig;
import org.jackson.puppy.tcc.transaction.recover.TransactionRecovery;
import org.jackson.puppy.tcc.transaction.repository.AbstractTransactionCacheRepository;
import org.jackson.puppy.tcc.transaction.spring.recover.TransactionRecoveryJob;
import org.jackson.puppy.tcc.transaction.spring.support.SpringBeanFactory;
import org.jackson.puppy.tcc.transaction.spring.support.SpringPostProcessor;
import org.jackson.puppy.tcc.transaction.spring.support.SpringTccTransactionConfig;
import org.jackson.puppy.tcc.transaction.support.TransactionConfigurator;
import org.quartz.*;
import org.quartz.spi.MutableTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Configuration
public class SpringTccConfig {

	private RecoverConfig recoverConfig;

	private Executor tccExecutor;

	@Autowired(required = false)
	public void setRecoverConfig(RecoverConfig recoverConfig) {
		this.recoverConfig = recoverConfig;
	}

	@Autowired(required = false)
	@Qualifier("tccExecutor")
	public void setTccExecutor(Executor tccExecutor) {
		this.tccExecutor = tccExecutor;
	}


	@Bean
	public SpringBeanFactory springBeanFactory(ApplicationContext applicationContext) {
		SpringBeanFactory springBeanFactory = new SpringBeanFactory();
		springBeanFactory.setApplicationContext(applicationContext);
		return springBeanFactory;
	}

	@Bean
	public SpringPostProcessor springPostProcessor() {
		return new SpringPostProcessor();
	}

	@Bean
	public SpringTccTransactionConfig springTransactionConfigurator(TransactionRepository transactionRepository) {

		SpringTccTransactionConfig springTccTransactionConfig = new SpringTccTransactionConfig();
		springTccTransactionConfig.setTransactionRepository(transactionRepository);

		if (recoverConfig != null) {
			springTccTransactionConfig.setRecoverConfig(recoverConfig);
		}
		TransactionManager transactionManager = new TransactionManager();
		transactionManager.setTransactionRepository(transactionRepository);

		if (tccExecutor != null) {
			transactionManager.setTccExecutor(tccExecutor);
		}

		springTccTransactionConfig.setTransactionManager(transactionManager);

		if (transactionRepository instanceof AbstractTransactionCacheRepository) {
			((AbstractTransactionCacheRepository) transactionRepository).setExpireDuration(springTccTransactionConfig.getRecoverConfig().getRecoverDuration());
		}
		return springTccTransactionConfig;
	}


	@Bean
	public TransactionRecovery transactionRecovery(TransactionConfigurator transactionConfigurator) {
		TransactionRecovery transactionRecovery = new TransactionRecovery();
		transactionRecovery.setTransactionConfigurator(transactionConfigurator);
		return transactionRecovery;
	}

	@Bean
	public JobDetail transactionRecoveryJobDetail() {
		return JobBuilder
				.newJob(TransactionRecoveryJob.class)
				.withIdentity("transactionRecoveryJob", "tccTransaction")
				.storeDurably()
				.build();
	}

	@Bean
	public Trigger transactionRecoveryCronTrigger(TransactionConfigurator transactionConfigurator) {
		MutableTrigger cron = CronScheduleBuilder
				.cronSchedule(transactionConfigurator.getRecoverConfig().getCronExpression())
				.build();
		JobKey key = transactionRecoveryJobDetail().getKey();
		cron.setJobKey(key);
		cron.setKey(new TriggerKey("transactionRecoveryCronTrigger", "tccTransaction"));
		return cron;
	}
}
