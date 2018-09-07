package org.jackson.puppy.tcc.transaction.support;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public interface BeanFactory {
	<T> T getBean(Class<T> clazz);

	<T> boolean isFactoryOf(Class<T> clazz);
}
