package org.jackson.puppy.tcc.transaction.spring.support;

import org.jackson.puppy.tcc.transaction.support.BeanFactory;
import org.jackson.puppy.tcc.transaction.support.FactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class SpringPostProcessor implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();

		if (applicationContext.getParent() == null) {
			FactoryBuilder.registerBeanFactory(applicationContext.getBean(BeanFactory.class));
		}
	}
}
