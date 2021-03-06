package org.jackson.puppy.tcc.transaction.dubbo.proxy.jdk;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.proxy.InvokerInvocationHandler;
import org.apache.dubbo.rpc.proxy.jdk.JdkProxyFactory;

import java.lang.reflect.Proxy;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TccJdkProxyFactory extends JdkProxyFactory {

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {

		T proxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, new InvokerInvocationHandler(invoker));

		T tccProxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, new TccInvokerInvocationHandler(proxy, invoker));

		return tccProxy;
	}
}