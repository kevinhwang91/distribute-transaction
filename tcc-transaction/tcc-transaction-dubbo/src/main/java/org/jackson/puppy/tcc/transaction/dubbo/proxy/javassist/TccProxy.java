/*
 * Copyright 1999-2011 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jackson.puppy.tcc.transaction.dubbo.proxy.javassist;

import org.apache.dubbo.common.bytecode.Proxy;
import org.apache.dubbo.common.utils.ClassHelper;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.jackson.puppy.tcc.transaction.api.TccTransactional;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public abstract class TccProxy extends Proxy {

	public static final InvocationHandler RETURN_NULL_INVOKER = (proxy, method, args) -> null;

	public static final InvocationHandler THROW_UNSUPPORTED_INVOKER = (proxy, method, args) -> {
		throw new UnsupportedOperationException("Method [" + ReflectUtils.getName(method) + "] unimplemented.");
	};
	private static final AtomicLong PROXY_CLASS_COUNTER = new AtomicLong(0);
	private static final String PACKAGE_NAME = TccProxy.class.getPackage().getName();
	private static final Map<ClassLoader, Map<String, Object>> ProxyCacheMap = new WeakHashMap<>();

	private static final Object PendingGenerationMarker = new Object();

	protected TccProxy() {
	}

	/**
	 * Get proxy.
	 *
	 * @param ics interface class array.
	 * @return TccProxy instance.
	 */
	public static TccProxy getProxy(Class<?>... ics) {
		return getProxy(ClassHelper.getCallerClassLoader(TccProxy.class), ics);
	}

	/**
	 * Get proxy.
	 *
	 * @param cl  class loader.
	 * @param ics interface class array.
	 * @return TccProxy instance.
	 */
	public static TccProxy getProxy(ClassLoader cl, Class<?>... ics) {
		if (ics.length > 65535) {
			throw new IllegalArgumentException("interface limit exceeded");
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ics.length; i++) {
			String itf = ics[i].getName();
			if (!ics[i].isInterface()) {
				throw new RuntimeException(itf + " is not a interface.");
			}

			Class<?> tmp = null;
			try {
				tmp = Class.forName(itf, false, cl);
			} catch (ClassNotFoundException e) {
			}

			if (tmp != ics[i]) {
				throw new IllegalArgumentException(ics[i] + " is not visible from class loader");
			}

			sb.append(itf).append(';');
		}

		// use interface class name list as key.
		String key = sb.toString();

		// get cache by class loader.
		Map<String, Object> cache;
		synchronized (ProxyCacheMap) {
			cache = ProxyCacheMap.get(cl);
			if (cache == null) {
				cache = new HashMap<>();
				ProxyCacheMap.put(cl, cache);
			}
		}

		TccProxy proxy = null;
		synchronized (cache) {
			do {
				Object value = cache.get(key);
				if (value instanceof Reference<?>) {
					proxy = (TccProxy) ((Reference<?>) value).get();
					if (proxy != null) {
						return proxy;
					}
				}

				if (value == PendingGenerationMarker) {
					try {
						cache.wait();
					} catch (InterruptedException e) {
					}
				} else {
					cache.put(key, PendingGenerationMarker);
					break;
				}
			}
			while (true);
		}

		long id = PROXY_CLASS_COUNTER.getAndIncrement();
		String pkg = null;
		TccClassGenerator ccp = null, ccm = null;
		try {
			ccp = TccClassGenerator.newInstance(cl);

			Set<String> worked = new HashSet<String>();
			List<Method> methods = new ArrayList<Method>();

			for (int i = 0; i < ics.length; i++) {
				if (!Modifier.isPublic(ics[i].getModifiers())) {
					String npkg = ics[i].getPackage().getName();
					if (pkg == null) {
						pkg = npkg;
					} else {
						if (!pkg.equals(npkg)) {
							throw new IllegalArgumentException("non-public interfaces from different packages");
						}
					}
				}
				ccp.addInterface(ics[i]);

				for (Method method : ics[i].getMethods()) {
					String desc = ReflectUtils.getDesc(method);
					if (worked.contains(desc)) {
						continue;
					}
					worked.add(desc);

					int ix = methods.size();
					Class<?> rt = method.getReturnType();
					Class<?>[] pts = method.getParameterTypes();

					StringBuilder code = new StringBuilder("Object[] args = new Object[").append(pts.length).append("];");
					for (int j = 0; j < pts.length; j++) {
						code.append(" args[").append(j).append("] = ($w)$").append(j + 1).append(";");
					}
					code.append(" Object ret = handler.invoke(this, methods[" + ix + "], args);");
					if (!Void.TYPE.equals(rt)) {
						code.append(" return ").append(asArgument(rt, "ret")).append(";");
					}

					methods.add(method);

					TccTransactional tccTransactional = method.getAnnotation(TccTransactional.class);

					if (tccTransactional != null) {
						ccp.addMethod(true, method.getName(), method.getModifiers(), rt, pts, method.getExceptionTypes(), code.toString());
					} else {
						ccp.addMethod(false, method.getName(), method.getModifiers(), rt, pts, method.getExceptionTypes(), code.toString());
					}
				}
			}

			if (pkg == null) {
				pkg = PACKAGE_NAME;
			}

			// create ProxyInstance class.
			String pcn = pkg + ".proxy" + id;
			ccp.setClassName(pcn);
			ccp.addField("public static java.lang.reflect.Method[] methods;");
			ccp.addField("private " + InvocationHandler.class.getName() + " handler;");
			ccp.addConstructor(Modifier.PUBLIC, new Class<?>[]{InvocationHandler.class}, new Class<?>[0], "handler=$1;");
			ccp.addDefaultConstructor();
			Class<?> clazz = ccp.toClass();
			clazz.getField("methods").set(null, methods.toArray(new Method[0]));

			// create TccProxy class.
			String fcn = TccProxy.class.getName() + id;
			ccm = TccClassGenerator.newInstance(cl);
			ccm.setClassName(fcn);
			ccm.addDefaultConstructor();
			ccm.setSuperClass(TccProxy.class);
			ccm.addMethod("public Object newInstance(" + InvocationHandler.class.getName() + " h){ return new " + pcn + "($1); }");
			Class<?> pc = ccm.toClass();
			proxy = (TccProxy) pc.newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			// release TccClassGenerator
			if (ccp != null) {
				ccp.release();
			}
			if (ccm != null) {
				ccm.release();
			}
			synchronized (cache) {
				if (proxy == null) {
					cache.remove(key);
				} else {
					cache.put(key, new WeakReference<>(proxy));
				}
				cache.notifyAll();
			}
		}
		return proxy;
	}

	private static String asArgument(Class<?> cl, String name) {
		if (cl.isPrimitive()) {
			if (Boolean.TYPE == cl) {
				return name + "==null?false:((Boolean)" + name + ").booleanValue()";
			}
			if (Byte.TYPE == cl) {
				return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
			}
			if (Character.TYPE == cl) {
				return name + "==null?(char)0:((Character)" + name + ").charValue()";
			}
			if (Double.TYPE == cl) {
				return name + "==null?(double)0:((Double)" + name + ").doubleValue()";
			}
			if (Float.TYPE == cl) {
				return name + "==null?(float)0:((Float)" + name + ").floatValue()";
			}
			if (Integer.TYPE == cl) {
				return name + "==null?(int)0:((Integer)" + name + ").intValue()";
			}
			if (Long.TYPE == cl) {
				return name + "==null?(long)0:((Long)" + name + ").longValue()";
			}
			if (Short.TYPE == cl) {
				return name + "==null?(short)0:((Short)" + name + ").shortValue()";
			}
			throw new RuntimeException(name + " is unknown primitive type.");
		}
		return "(" + ReflectUtils.getName(cl) + ")" + name;
	}

	/**
	 * get instance with default handler.
	 *
	 * @return instance.
	 */
	@Override
	public Object newInstance() {
		return newInstance(THROW_UNSUPPORTED_INVOKER);
	}

	/**
	 * get instance with special handler.
	 *
	 * @return instance.
	 */
	@Override
	abstract public Object newInstance(InvocationHandler handler);
}