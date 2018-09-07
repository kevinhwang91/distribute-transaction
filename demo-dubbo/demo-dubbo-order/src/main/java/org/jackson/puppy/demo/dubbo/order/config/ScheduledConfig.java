package org.jackson.puppy.demo.dubbo.order.config;

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
}
