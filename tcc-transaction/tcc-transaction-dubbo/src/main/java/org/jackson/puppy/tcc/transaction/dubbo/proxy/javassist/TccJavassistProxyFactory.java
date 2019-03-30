package org.jackson.puppy.tcc.transaction.dubbo.proxy.javassist;


import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.proxy.InvokerInvocationHandler;
import org.apache.dubbo.rpc.proxy.javassist.JavassistProxyFactory;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TccJavassistProxyFactory extends JavassistProxyFactory {

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
		return (T) TccProxy.getProxy(interfaces).newInstance(new InvokerInvocationHandler(invoker));
	}
}
