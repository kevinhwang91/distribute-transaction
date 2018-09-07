package org.jackson.puppy.tcc.transaction.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public final class FactoryBuilder {


	private static List<BeanFactory> beanFactories = new ArrayList<>();
	private static ConcurrentHashMap<Class, SingletonFactory> classFactoryMap = new ConcurrentHashMap<Class, SingletonFactory>();

	private FactoryBuilder() {

	}

	public static <T> SingletonFactory<T> factoryOf(Class<T> clazz) {

		if (!classFactoryMap.containsKey(clazz)) {

			for (BeanFactory beanFactory : beanFactories) {
				if (beanFactory.isFactoryOf(clazz)) {
					classFactoryMap.putIfAbsent(clazz, new SingletonFactory<T>(clazz, beanFactory.getBean(clazz)));
				}
			}

			if (!classFactoryMap.containsKey(clazz)) {
				classFactoryMap.putIfAbsent(clazz, new SingletonFactory<>(clazz));
			}
		}

		return classFactoryMap.get(clazz);
	}

	public static void registerBeanFactory(BeanFactory beanFactory) {
		beanFactories.add(beanFactory);
	}

	public static class SingletonFactory<T> {

		private volatile T instance = null;

		private String className;

		public SingletonFactory(Class<T> clazz, T instance) {
			this.className = clazz.getName();
			this.instance = instance;
		}

		public SingletonFactory(Class<T> clazz) {
			this.className = clazz.getName();
		}

		public T getInstance() {

			if (instance == null) {
				synchronized (SingletonFactory.class) {
					if (instance == null) {
						try {
							ClassLoader loader = Thread.currentThread().getContextClassLoader();

							Class<?> clazz = loader.loadClass(className);

							instance = (T) clazz.newInstance();
						} catch (Exception e) {
							throw new RuntimeException("Failed to create an instance of " + className, e);
						}
					}
				}
			}

			return instance;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (other == null || getClass() != other.getClass()) {
				return false;
			}

			SingletonFactory that = (SingletonFactory) other;

			if (!className.equals(that.className)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return className.hashCode();
		}
	}
}