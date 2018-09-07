package org.jackson.puppy.tcc.transaction.spring.support;

import org.jackson.puppy.tcc.transaction.support.BeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class SpringBeanFactory implements BeanFactory {

	private ApplicationContext applicationContext;

	@Override
	public <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}

	@Override
	public <T> boolean isFactoryOf(Class<T> clazz) {
		final Map<String, T> map = applicationContext.getBeansOfType(clazz);
		return map.size() > 0;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
