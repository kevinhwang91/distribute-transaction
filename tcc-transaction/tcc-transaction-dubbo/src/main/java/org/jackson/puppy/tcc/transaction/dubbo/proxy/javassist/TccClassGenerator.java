package org.jackson.puppy.tcc.transaction.dubbo.proxy.javassist;

import com.alibaba.dubbo.common.utils.ReflectUtils;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public final class TccClassGenerator {
	private static final AtomicLong CLASS_NAME_COUNTER = new AtomicLong(0);
	private static final String SIMPLE_NAME_TAG = "<init>";
	private static final Map<ClassLoader, ClassPool> POOL_MAP = new ConcurrentHashMap<>(); //ClassLoader - ClassPool
	private ClassPool mPool;
	private CtClass mCtc;
	private String mClassName, mSuperClass;
	private Set<String> mInterfaces;
	private List<String> mFields, mConstructors, mMethods;
	private Set<String> tccTransactionMethods = new HashSet<>();
	private Map<String, Method> mCopyMethods; // <method desc,method instance>
	private Map<String, Constructor<?>> mCopyConstructors; // <constructor desc,constructor instance>
	private boolean mDefaultConstructor = false;

	private TccClassGenerator() {
	}

	private TccClassGenerator(ClassPool pool) {
		mPool = pool;
	}

	public static TccClassGenerator newInstance() {
		return new TccClassGenerator(getClassPool(Thread.currentThread().getContextClassLoader()));
	}

	public static TccClassGenerator newInstance(ClassLoader loader) {
		return new TccClassGenerator(getClassPool(loader));
	}

	public static boolean isDynamicClass(Class<?> cl) {
		return TccClassGenerator.DC.class.isAssignableFrom(cl);
	}

	public static ClassPool getClassPool(ClassLoader loader) {
		if (loader == null) {
			return ClassPool.getDefault();
		}

		ClassPool pool = POOL_MAP.get(loader);
		if (pool == null) {
			pool = new ClassPool(true);
			pool.appendClassPath(new LoaderClassPath(loader));
			POOL_MAP.put(loader, pool);
		}
		return pool;
	}

	private static String modifier(int mod) {
		if (java.lang.reflect.Modifier.isPublic(mod)) {
			return "public";
		}
		if (java.lang.reflect.Modifier.isProtected(mod)) {
			return "protected";
		}
		if (java.lang.reflect.Modifier.isPrivate(mod)) {
			return "private";
		}
		return "";
	}

	public String getClassName() {
		return mClassName;
	}

	public TccClassGenerator setClassName(String name) {
		mClassName = name;
		return this;
	}

	public TccClassGenerator addInterface(String cn) {
		if (mInterfaces == null) {
			mInterfaces = new HashSet<String>();
		}
		mInterfaces.add(cn);
		return this;
	}

	public TccClassGenerator addInterface(Class<?> cl) {
		return addInterface(cl.getName());
	}

	public TccClassGenerator setSuperClass(String cn) {
		mSuperClass = cn;
		return this;
	}

	public TccClassGenerator setSuperClass(Class<?> cl) {
		mSuperClass = cl.getName();
		return this;
	}

	public TccClassGenerator addField(String code) {
		if (mFields == null) {
			mFields = new ArrayList<>();
		}
		mFields.add(code);
		return this;
	}

	public TccClassGenerator addField(String name, int mod, Class<?> type) {
		return addField(name, mod, type, null);
	}

	public TccClassGenerator addField(String name, int mod, Class<?> type, String def) {
		StringBuilder sb = new StringBuilder();
		sb.append(modifier(mod)).append(' ').append(ReflectUtils.getName(type)).append(' ');
		sb.append(name);
		if (def != null && def.length() > 0) {
			sb.append('=');
			sb.append(def);
		}
		sb.append(';');
		return addField(sb.toString());
	}

	public TccClassGenerator addMethod(String code) {
		if (mMethods == null) {
			mMethods = new ArrayList<>();
		}
		mMethods.add(code);
		return this;
	}

	public TccClassGenerator addMethod(String name, int mod, Class<?> rt, Class<?>[] pts, String body) {
		return addMethod(false, name, mod, rt, pts, null, body);
	}

	public TccClassGenerator addMethod(boolean isTccTransactionMethod, String name, int mod, Class<?> rt, Class<?>[] pts, Class<?>[] ets, String body) {
		StringBuilder sb = new StringBuilder();

		sb.append(modifier(mod)).append(' ').append(ReflectUtils.getName(rt)).append(' ').append(name);
		sb.append('(');
		for (int i = 0; i < pts.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(ReflectUtils.getName(pts[i]));
			sb.append(" arg").append(i);
		}
		sb.append(')');
		if (ets != null && ets.length > 0) {
			sb.append(" throws ");
			for (int i = 0; i < ets.length; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(ReflectUtils.getName(ets[i]));
			}
		}
		sb.append('{').append(body).append('}');

		if (isTccTransactionMethod) {
			tccTransactionMethods.add(sb.toString());
		}

		return addMethod(sb.toString());
	}

	public TccClassGenerator addMethod(Method m) {
		addMethod(m.getName(), m);
		return this;
	}

	public TccClassGenerator addMethod(String name, Method m) {
		String desc = name + ReflectUtils.getDescWithoutMethodName(m);
		addMethod(':' + desc);
		if (mCopyMethods == null) {
			mCopyMethods = new ConcurrentHashMap<>(8);
		}
		mCopyMethods.put(desc, m);
		return this;
	}

	public TccClassGenerator addConstructor(String code) {
		if (mConstructors == null) {
			mConstructors = new LinkedList<>();
		}
		mConstructors.add(code);
		return this;
	}

	public TccClassGenerator addConstructor(int mod, Class<?>[] pts, String body) {
		return addConstructor(mod, pts, null, body);
	}

	public TccClassGenerator addConstructor(int mod, Class<?>[] pts, Class<?>[] ets, String body) {
		StringBuilder sb = new StringBuilder();
		sb.append(modifier(mod)).append(' ').append(SIMPLE_NAME_TAG);
		sb.append('(');
		for (int i = 0; i < pts.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(ReflectUtils.getName(pts[i]));
			sb.append(" arg").append(i);
		}
		sb.append(')');
		if (ets != null && ets.length > 0) {
			sb.append(" throws ");
			for (int i = 0; i < ets.length; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(ReflectUtils.getName(ets[i]));
			}
		}
		sb.append('{').append(body).append('}');
		return addConstructor(sb.toString());
	}

	public TccClassGenerator addConstructor(Constructor<?> c) {
		String desc = ReflectUtils.getDesc(c);
		addConstructor(":" + desc);
		if (mCopyConstructors == null) {
			mCopyConstructors = new ConcurrentHashMap<>(4);
		}
		mCopyConstructors.put(desc, c);
		return this;
	}

	public TccClassGenerator addDefaultConstructor() {
		mDefaultConstructor = true;
		return this;
	}

	public ClassPool getClassPool() {
		return mPool;
	}

	public Class<?> toClass() {
		if (mCtc != null) {
			mCtc.detach();
		}
		long id = CLASS_NAME_COUNTER.getAndIncrement();
		try {
			CtClass ctcs = mSuperClass == null ? null : mPool.get(mSuperClass);
			if (mClassName == null) {
				mClassName = (mSuperClass == null || javassist.Modifier.isPublic(ctcs.getModifiers())
						? TccClassGenerator.class.getName() : mSuperClass + "$sc") + id;
			}
			mCtc = mPool.makeClass(mClassName);
			if (mSuperClass != null) {
				mCtc.setSuperclass(ctcs);
			}
			mCtc.addInterface(mPool.get(DC.class.getName())); // add dynamic class tag.
			if (mInterfaces != null) {
				for (String cl : mInterfaces) {
					mCtc.addInterface(mPool.get(cl));
				}
			}
			if (mFields != null) {
				for (String code : mFields) {
					mCtc.addField(CtField.make(code, mCtc));
				}
			}
			if (mMethods != null) {
				for (String code : mMethods) {
					if (code.charAt(0) == ':') {
						mCtc.addMethod(CtNewMethod.copy(getCtMethod(mCopyMethods.get(code.substring(1))), code.substring(1, code.indexOf('(')), mCtc, null));
					} else {

						CtMethod ctMethod = CtNewMethod.make(code, mCtc);

						if (tccTransactionMethods.contains(code)) {

							ConstPool constpool = mCtc.getClassFile().getConstPool();
							AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
							Annotation annot = new Annotation("org.jackson.puppy.tcc.transaction.api.TccTransactional", constpool);
							EnumMemberValue enumMemberValue = new EnumMemberValue(constpool);
							enumMemberValue.setType("org.jackson.puppy.tcc.transaction.api.Propagation");
							enumMemberValue.setValue("SUPPORTS");
							annot.addMemberValue("propagation", enumMemberValue);
							annot.addMemberValue("confirmMethod", new StringMemberValue(ctMethod.getName(), constpool));
							annot.addMemberValue("cancelMethod", new StringMemberValue(ctMethod.getName(), constpool));

							ClassMemberValue classMemberValue = new ClassMemberValue("org.jackson.puppy.tcc.transaction.dubbo.context.DubboTransactionContextEditor", constpool);
							annot.addMemberValue("transactionContextEditor", classMemberValue);

							attr.addAnnotation(annot);
							ctMethod.getMethodInfo().addAttribute(attr);
						}

						mCtc.addMethod(ctMethod);
					}
				}
			}
			if (mDefaultConstructor) {
				mCtc.addConstructor(CtNewConstructor.defaultConstructor(mCtc));
			}
			if (mConstructors != null) {
				for (String code : mConstructors) {
					if (code.charAt(0) == ':') {
						mCtc.addConstructor(CtNewConstructor.copy(getCtConstructor(mCopyConstructors.get(code.substring(1))), mCtc, null));
					} else {
						String[] sn = mCtc.getSimpleName().split("\\$+"); // inner class name include $.
						mCtc.addConstructor(CtNewConstructor.make(code.replaceFirst(SIMPLE_NAME_TAG, sn[sn.length - 1]), mCtc));
					}
				}
			}
			return mCtc.toClass();
		} catch (RuntimeException e) {
			throw e;
		} catch (NotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (CannotCompileException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void release() {
		if (mCtc != null) {
			mCtc.detach();
		}
		if (mInterfaces != null) {
			mInterfaces.clear();
		}
		if (mFields != null) {
			mFields.clear();
		}
		if (mMethods != null) {
			mMethods.clear();
		}
		if (mConstructors != null) {
			mConstructors.clear();
		}
		if (mCopyMethods != null) {
			mCopyMethods.clear();
		}
		if (mCopyConstructors != null) {
			mCopyConstructors.clear();
		}
	}

	private CtClass getCtClass(Class<?> c) throws NotFoundException {
		return mPool.get(c.getName());
	}

	private CtMethod getCtMethod(Method m) throws NotFoundException {
		return getCtClass(m.getDeclaringClass()).getMethod(m.getName(), ReflectUtils.getDescWithoutMethodName(m));
	}

	private CtConstructor getCtConstructor(Constructor<?> c) throws NotFoundException {
		return getCtClass(c.getDeclaringClass()).getConstructor(ReflectUtils.getDesc(c));
	}

	public static interface DC {
	} // dynamic class tag interface.
}