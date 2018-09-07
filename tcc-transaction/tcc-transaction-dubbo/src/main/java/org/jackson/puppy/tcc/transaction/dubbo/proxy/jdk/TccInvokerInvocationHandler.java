package org.jackson.puppy.tcc.transaction.dubbo.proxy.jdk;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.jackson.puppy.tcc.transaction.api.Propagation;
import org.jackson.puppy.tcc.transaction.api.TccTransactional;
import org.jackson.puppy.tcc.transaction.dubbo.context.DubboTransactionContextEditor;
import org.jackson.puppy.tcc.transaction.spring.aspect.ResourceCoordinatorAspect;
import org.jackson.puppy.tcc.transaction.support.FactoryBuilder;
import org.jackson.puppy.tcc.transaction.utils.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TccInvokerInvocationHandler extends InvokerInvocationHandler {

	private Object target;

	public TccInvokerInvocationHandler(Invoker<?> handler) {
		super(handler);
	}

	public <T> TccInvokerInvocationHandler(T target, Invoker<T> invoker) {
		super(invoker);
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		TccTransactional tccTransactional = method.getAnnotation(TccTransactional.class);

		if (tccTransactional != null) {

			if (StringUtils.isEmpty(tccTransactional.confirmMethod())) {
				ReflectionUtils.changeAnnotationValue(tccTransactional, "confirmMethod", method.getName());
				ReflectionUtils.changeAnnotationValue(tccTransactional, "cancelMethod", method.getName());
				ReflectionUtils.changeAnnotationValue(tccTransactional, "transactionContextEditor", DubboTransactionContextEditor.class);
				ReflectionUtils.changeAnnotationValue(tccTransactional, "propagation", Propagation.SUPPORTS);
			}

			ProceedingJoinPoint pjp = new MethodProceedingJoinPoint(proxy, target, method, args);
			return FactoryBuilder.factoryOf(ResourceCoordinatorAspect.class).getInstance().interceptTransactionContextMethod(pjp);
		} else {
			return super.invoke(target, method, args);
		}
	}


}
