/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.jrouter.bytecode.javassist;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.jrouter.JRouterException;
import net.jrouter.util.MethodUtil;

/**
 * 提供 根据对象实例、类型生成指定接口或抽象类型对象 的生成器。
 */
@Slf4j
public class JavassistProxyObjectFactory {

    /** 原代理对象的{@code Class}名 */
    static final String PROXY_CLASS_TARGET_CLASS_FIELD_NAME = "_targetClass";

    /** 原代理对象 */
    static final String PROXY_CLASS_TARGET_FIELD_NAME = "_target";

    /** 生成的代理类包名 */
    private static final String PROXY_CLASS_PACKAGE_NAME = JavassistProxyObjectFactory.class.getPackage().getName();

    /** 接口代理类后缀 */
    private static final String PROXY_CLASS_SUFFIX = "$$JR_Proxy$$";

    /** 计数器 */
    private static final AtomicInteger COUNTER = new AtomicInteger(0x10000);

    static {
        ClassPool.getDefault().insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    /**
     * 设置未匹配的方法抛出的异常类型；{@code null}值则无异，返回默认（null/0/false）;
     * 异常类型必须包含空构造方法。
     */
    @lombok.Setter
    private Class<? extends Throwable> mismatchedMethodExceptionClass = null;

    /**
     * Constructor.
     */
    public JavassistProxyObjectFactory() {
    }

    /**
     * Generate dynamic proxy instance of proxied interface or abstract class.
     *
     * @param instance 对象实例。
     * @param proxiedInterface 被代理的（接口或抽象类）类型。
     * @param <T> type.
     *
     * @return 动态生成的代理对象。
     */
    public <T> T newInstance(Object instance, Class<T> proxiedInterface) {
        return newInstance(instance, instance.getClass(), proxiedInterface);
    }

    /**
     * Generate dynamic proxy instance of proxied interface or abstract class.
     *
     * @param instance 对象实例。
     * @param instanceClass 对象实例类型。
     * @param proxiedInterface 被代理的（接口或抽象类）类型。
     * @param <T> type.
     *
     * @return 动态生成的代理对象。
     */
    public <T> T newInstance(Object instance, Class<?> instanceClass, Class<T> proxiedInterface) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating proxy class from {} : {}", instanceClass, proxiedInterface);
            }
            Class<T> proxyClass = (Class) createInterfaceProxyClass(instanceClass, proxiedInterface)
                    .toClass(proxiedInterface.getClassLoader(), proxiedInterface.getProtectionDomain());
            T t = proxyClass.getDeclaredConstructor().newInstance();
            Field f = null;
            f = proxyClass.getDeclaredField(PROXY_CLASS_TARGET_CLASS_FIELD_NAME);
            f.setAccessible(true);
            f.set(t, instanceClass);
            f = proxyClass.getDeclaredField(PROXY_CLASS_TARGET_FIELD_NAME);
            f.setAccessible(true);
            f.set(t, instance);
            return t;
        } catch (Exception e) {
            throw new JRouterException(e);
        }
    }

    /**
     * Create proxy class's package name.
     * <p>
     * 如果接口为public为默认包，否则为接口同包。
     */
    private String createProxyPackageName(Class<?> instanceClass, Class<?> proxiedInterface) {
        return Modifier.isPublic(proxiedInterface.getModifiers())
                ? PROXY_CLASS_PACKAGE_NAME
                : proxiedInterface.getPackage().getName();
    }

    /**
     * 根据被代理的（接口或抽象类）类型构建{@link CtClass}对象。
     *
     * @param instanceClass 原对象类型。
     * @param proxiedInterface 被代理的（接口或抽象类）类型。
     *
     * @return CtClass对象。
     *
     * @throws CannotCompileException when bytecode transformation has failed.
     * @throws NotFoundException when class is not found.
     */
    private CtClass createInterfaceProxyClass(Class<?> instanceClass, Class<?> proxiedInterface) throws CannotCompileException, NotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        ClassClassPath classPath = new ClassClassPath(proxiedInterface);
        classPool.insertClassPath(classPath);
        //import proxied interface package
        classPool.importPackage(proxiedInterface.getPackage().getName());

        //同包interface类前缀 + 16进制计数值
        CtClass proxyClass = classPool.makeClass(createProxyPackageName(instanceClass, proxiedInterface) + "." + proxiedInterface.getSimpleName() + PROXY_CLASS_SUFFIX + Integer.toHexString(COUNTER.getAndIncrement()));
        try {
            //继承特定接口/抽象类/类
            proxyClass.addInterface(classPool.getCtClass(proxiedInterface.getName()));
            //public for create new instance
            proxyClass.setModifiers(Modifier.PUBLIC);
            proxyClass.addField(CtField.make("private Class " + PROXY_CLASS_TARGET_CLASS_FIELD_NAME + ";", proxyClass));
            proxyClass.addMethod(createSetMethod(proxyClass, PROXY_CLASS_TARGET_CLASS_FIELD_NAME, Class.class));
            proxyClass.addField(CtField.make(String.format("private %s %s;", instanceClass.getName(), PROXY_CLASS_TARGET_FIELD_NAME), proxyClass));
            proxyClass.addMethod(createSetMethod(proxyClass, PROXY_CLASS_TARGET_FIELD_NAME, instanceClass));

            MethodComparison result = matchMethods(proxiedInterface, instanceClass);
            for (Map.Entry<Method, Method> entry : result.matched.entrySet()) {
                createMatchedProxyMethod(proxyClass, entry.getKey(), entry.getValue());
            }
            for (Method method : result.mismatched) {
                createMismatchedProxyMethod(proxyClass, method);
            }
        } finally {
            classPool.removeClassPath(classPath);
            classPool.clearImportedPackages();
            if (proxyClass != null) {
                proxyClass.detach();
            }
        }
        return proxyClass;
    }

    /**
     * 基于被代理方法和代理方法比对构建{@link CtMethod}方法。
     *
     * @param clazz 代理方法所在的 {@code CtClass} 类。
     * @param proxiedMethod 被代理的（抽象）方法。
     * @param target 代理对象的方法。
     *
     * @throws CannotCompileException when bytecode transformation has failed.
     */
    @SneakyThrows
    private void createMatchedProxyMethod(CtClass clazz, Method proxiedMethod, Method target) {
        Class<?> returnType = proxiedMethod.getReturnType();
        boolean voidMethod = void.class == returnType;
        String methodName = proxiedMethod.getName();
        Class<?>[] proxyParameterTypes = proxiedMethod.getParameterTypes();
        int parameterLength = proxyParameterTypes.length;
        StringBuilder body = new StringBuilder(String.format(
                "public %s %s(",
                voidMethod ? "void" : returnType.getCanonicalName(),
                methodName
        ));
        //proxy parameters begin
        if (parameterLength != 0) {
            for (int i = 0; i < parameterLength - 1; i++) {
                body.append(proxyParameterTypes[i].getCanonicalName()).append(" p").append(i);
                body.append(',');
            }
            body.append(proxyParameterTypes[parameterLength - 1].getCanonicalName()).append(" p").append(parameterLength - 1);
        }
        //proxy parameters end
        body.append("){");

        if (!voidMethod) {
            //auto cast return type
            body.append("return ($r)");
        }
        //_target.method(;
        body.append(String.format("%s.%s(", PROXY_CLASS_TARGET_FIELD_NAME, methodName));
        Class<?>[] targetParameterTypes = target.getParameterTypes();
        int targetParameterLength = targetParameterTypes.length;
        if (targetParameterLength != 0) {
            int[] idx = MethodUtil.match(target, 0, proxyParameterTypes);
            //p1,p2...pn
            for (int i = 0; i < targetParameterLength - 1; i++) {
                if (idx[i] == -1) {
                    //null
                    body.append(getReturnNull(targetParameterTypes[i]));
                } else {
                    body.append(" p").append(idx[i]);
                }
                body.append(',');
            }
            int end = targetParameterLength - 1;
            if (idx[end] == -1) {
                //null
                body.append(getReturnNull(targetParameterTypes[end]));
            } else {
                body.append(" p").append(idx[end]);
            }
        }
        //_target.method...);
        body.append(");}");
        clazz.addMethod(CtNewMethod.make(body.toString(), clazz));
    }

    /**
     * 对未匹配的方法提供默认空方法体。
     *
     * @param clazz 代理方法所在的 {@code CtClass} 类。
     * @param proxiedMethod 被代理的（抽象）方法。
     */
    @SneakyThrows
    private void createMismatchedProxyMethod(CtClass clazz, Method proxiedMethod) {
        Class<?> returnType = proxiedMethod.getReturnType();
        boolean voidMethod = void.class == returnType;
        String methodName = proxiedMethod.getName();
        Class<?>[] proxyParameterTypes = proxiedMethod.getParameterTypes();
        int parameterLength = proxyParameterTypes.length;
        StringBuilder body = new StringBuilder(String.format(
                "public %s %s(",
                voidMethod ? "void" : returnType.getCanonicalName(),
                methodName
        ));
        //proxy parameters begin
        if (parameterLength != 0) {
            for (int i = 0; i < parameterLength - 1; i++) {
                body.append(proxyParameterTypes[i].getCanonicalName()).append(" p").append(i);
                body.append(',');
            }
            body.append(proxyParameterTypes[parameterLength - 1].getCanonicalName()).append(" p").append(parameterLength - 1);
        }
        //proxy parameters end
        body.append("){");
        if (mismatchedMethodExceptionClass == null) {
            if (!voidMethod) {
                //auto cast return type
                body.append("return ($r)");
                body.append(getReturnNull(returnType));
                body.append(';');
            }
        } else {
            body.append(String.format("throw new %s();", mismatchedMethodExceptionClass.getCanonicalName()));
        }
        body.append('}');
        clazz.addMethod(CtNewMethod.make(body.toString(), clazz));
    }

    /**
     * Create a set method.
     *
     * @param clazz 代理方法所在的 {@code CtClass} 类。
     * @param varName 变量名称。
     * @param type 变量类型。
     *
     * @return CtMethod方法。
     *
     * @throws CannotCompileException when bytecode transformation has failed.
     */
    @SneakyThrows
    private CtMethod createSetMethod(CtClass clazz, String varName, Class<?> type) throws CannotCompileException {
        char[] chars = varName.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        StringBuilder body = new StringBuilder("public void set").append(chars).append('(');
        body.append(type.getCanonicalName()).append(" obj){this.").append(varName).append("=obj;}");
        return CtNewMethod.make(body.toString(), clazz);
    }

    /**
     * 指定对象类型（包括基本类型）的默认（null/0/false）返回值。
     *
     * @param clazz 指定的对象类型。
     *
     * @return 返回值。
     */
    private String getReturnNull(Class<?> clazz) {
        if (void.class == clazz) {
            return "";
        }
        if (clazz.isPrimitive()) {
            return (clazz == boolean.class ? "false" : "(" + clazz.getCanonicalName() + ")0");
        }
        return "null";
    }

    /**
     * 获取生成的代理对象的原实际类型。
     *
     * @param obj 生成的代理对象。
     *
     * @return 代理对象的原实际类型。
     */
    //@Override
    public Class<?> getClass(Object obj) {
        if (obj != null) {
            try {
                Field f = obj.getClass().getDeclaredField(PROXY_CLASS_TARGET_CLASS_FIELD_NAME);
                f.setAccessible(true);
                return (Class) f.get(obj);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                log.error("Can't get class {}", obj);
            }
        }
        return obj.getClass();
    }

    /**
     * Method comparison results.
     */
    private final static class MethodComparison {

        /**
         * Matched abstract method with target method.
         */
        Map<Method, Method> matched = Collections.EMPTY_MAP;

        /**
         * Mismatched method.
         */
        Collection<Method> mismatched = Collections.EMPTY_LIST;
    }

    /**
     * 匹配规则： 方法同名、返回值兼容、忽略参数个数类型、默认顺序优先。
     * <p>
     * 返回 匹配 和 不匹配 的抽象方法。
     * <p>
     * 不考虑多接口，避免同名方法。
     *
     * @param proxiedClass 被代理的（接口或抽象类）类型。
     * @param targetClass 代理的类型。
     */
    private static MethodComparison matchMethods(Class<?> proxiedClass, Class<?> targetClass) {
        Map<Method, Method> matched = new LinkedHashMap<>(8);
        //all method
        Method[] abstractMethods = proxiedClass.getMethods();
        Set<Method> mismatched = new LinkedHashSet<>(Arrays.asList(abstractMethods));
        Method[] targetMethods = targetClass.getMethods();
        boolean[] flags = new boolean[targetMethods.length];
        for (Method abstractMethod : abstractMethods) {
            int mod = abstractMethod.getModifiers();
            //public abstract
            if (Modifier.isPublic(mod)
                    && Modifier.isAbstract(mod)) {
                for (int i = 0; i < targetMethods.length; i++) {
                    if (flags[i] == true) {
                        continue;
                    }
                    Method target = targetMethods[i];
                    boolean match = abstractMethod.getName().equals(target.getName())
                            && abstractMethod.getReturnType().isAssignableFrom(target.getReturnType());
                    if (match) {
                        matched.put(abstractMethod, target);
                        mismatched.remove(abstractMethod);
                        flags[i] = true;
                    }
                }
            }
        }
        MethodComparison result = new MethodComparison();
        result.matched = matched;
        result.mismatched = mismatched;
        return result;
    }
}
