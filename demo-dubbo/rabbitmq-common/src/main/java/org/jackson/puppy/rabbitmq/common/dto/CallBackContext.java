package org.jackson.puppy.rabbitmq.common.dto;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class CallBackContext {

	private Class targetClass;

	private String methodName;

	private Class[] parameterTypes;

	private Object[] args;

	private CallBackContext() {
	}

	public CallBackContext(Class targetClass, String methodName, Class[] parameterTypes, Object[] args) {
		this.targetClass = targetClass;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.args = args;
	}

	public Class getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(Class targetClass) {
		this.targetClass = targetClass;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}
}
