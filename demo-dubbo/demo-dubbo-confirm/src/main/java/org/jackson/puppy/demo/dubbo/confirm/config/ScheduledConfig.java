package org.jackson.puppy.demo.dubbo.confirm.config;

import org.jackson.puppy.demo.dubbo.confirm.schedule.RetrySendConfirmJob;
import org.quartz.*;
import org.quartz.spi.MutableTrigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Configuration
public class ScheduledConfig {

	@Bean
	public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer(@Qualifier("quartzDataSource") DataSource quartzDataSource, ApplicationContext applicationContext) {
		return bean -> {
			bean.setDataSource(quartzDataSource);
			bean.setJobFactory(new AutowireCapableBeanJobFactory(applicationContext.getAutowireCapableBeanFactory()));
		};
	}

	@Bean
	public JobDetail retrySendConfirmJobDetail() {
		return JobBuilder
				.newJob(RetrySendConfirmJob.class)
				.withIdentity("confirmJob", "retrySend")
				.storeDurably()
				.build();
	}

	@Bean
	public Trigger retrySendConfirmJobTrigger() {
		MutableTrigger cron = CronScheduleBuilder
				.cronSchedule("0 */2 * * * ? ")
				.build();
		JobKey key = retrySendConfirmJobDetail().getKey();
		cron.setJobKey(key);
		cron.setKey(new TriggerKey("confirmTrigger", "retrySend"));
		return cron;
	}
}
