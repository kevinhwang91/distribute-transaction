package org.jackson.puppy.tcc.transaction.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TccTransactional {

	Propagation propagation() default Propagation.REQUIRED;

	String confirmMethod() default "";

	String cancelMethod() default "";

	Class<? extends TransactionContextEditor> transactionContextEditor() default DefaultTransactionContextEditor.class;

	class NullableTransactionContextEditor implements TransactionContextEditor {

		@Override
		public TransactionContext get(Object target, Method method, Object[] args) {
			return null;
		}

		@Override
		public void set(TransactionContext transactionContext, Object target, Method method, Object[] args) {

		}

		@Override
		public void clear() {

		}
	}

	class DefaultTransactionContextEditor implements TransactionContextEditor {

		public static int getTransactionContextParamPosition(Class<?>[] parameterTypes) {

			int position = -1;

			for (int i = 0; i < parameterTypes.length; i++) {
				if (parameterTypes[i].equals(TransactionContext.class)) {
					position = i;
					break;
				}
			}
			return position;
		}

		public static TransactionContext getTransactionContextFromArgs(Object[] args) {
			TransactionContext transactionContext = null;

			for (Object arg : args) {
				if (arg != null && TransactionContext.class.isAssignableFrom(arg.getClass())) {
					transactionContext = (TransactionContext) arg;
				}
			}

			return transactionContext;
		}

		@Override
		public TransactionContext get(Object target, Method method, Object[] args) {
			int position = getTransactionContextParamPosition(method.getParameterTypes());
			if (position >= 0) {
				return (TransactionContext) args[position];
			}

			return null;
		}

		@Override
		public void set(TransactionContext transactionContext, Object target, Method method, Object[] args) {
			int position = getTransactionContextParamPosition(method.getParameterTypes());
			if (position >= 0) {
				args[position] = transactionContext;
			}
		}

		@Override
		public void clear() {

		}
	}
}