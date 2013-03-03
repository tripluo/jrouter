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
package jrouter.bytecode.javassist;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.*;
import jrouter.JRouterException;
import jrouter.ProxyFactory;
import jrouter.util.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供基于javassist的根据<code>Method</code>底层方法动态构建JavassistProxy对象的工厂类。
 *
 * <p> 通常如下使用：
 * <code><blockquote><pre>
 * Method method = ...
 * JavassistProxyFactory factory = new JavassistProxyFactory();
 * JavassistProxy proxy = factory.createProxyClass(method);
 * proxy.invoke(...);
 * </pre></blockquote></code>
 * </p>
 *
 * @see JavassistProxy
 */
public class JavassistProxyFactory implements ProxyFactory {

    /** LOG */
    private static final Logger LOG = LoggerFactory.getLogger(JavassistProxyFactory.class);

    //** 代理对象的类名称前缀 */
    private static final String CLASS_PREFIX = "jrouter.bytecode.javassist.Proxy$$";

    /** 计数器 */
    private static final AtomicInteger COUNTER = new AtomicInteger(0x10000);

    static {
        ClassPool.getDefault().insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public JavassistProxy newInstance(Method method) {
        //only public method can be proxied
        if (!Modifier.isPublic(method.getModifiers())) {
            LOG.warn("Only public method can be proxied, no proxy at : " + MethodUtil.getFullMethod(method));
            return null;
        }
        Class<?> srcClass = method.getDeclaringClass();
        try {
            LOG.debug("Create JavassistProxy at : " + MethodUtil.getMethod(method));
            JavassistProxy proxy = (JavassistProxy) (createProxyClass(method).
                    toClass(srcClass.getClassLoader(), srcClass.getProtectionDomain()).newInstance());
            return proxy;
        } catch (Exception e) {
            throw new JRouterException(e);
        }
    }

    /**
     * 根据底层方法构建<code>CtClass</code>对象。
     *
     * @param method 底层的方法。
     *
     * @return CtClass对象。
     *
     * @throws CannotCompileException when bytecode transformation has failed.
     * @throws NotFoundException when class is not found.
     */
    private CtClass createProxyClass(Method method) throws CannotCompileException, NotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        ClassClassPath classPath = new ClassClassPath(method.getClass());
        classPool.insertClassPath(classPath);
        //import target class package
        classPool.importPackage(method.getClass().getPackage().getName());

        //类前缀 + 16进制计数值
        CtClass clazz = classPool.makeClass(CLASS_PREFIX + Integer.toHexString(COUNTER.getAndIncrement()));
        try {
            //特定接口/抽象类/类的调用
            clazz.setSuperclass(classPool.getCtClass(JavassistProxy.class.getName()));
            //invoke method
            clazz.addMethod(createInovkeMethod(clazz, method));
        } finally {
            classPool.removeClassPath(classPath);
            classPool.clearImportedPackages();
            if (clazz != null) {
                clazz.detach();
            }
        }
        return clazz;
    }

    /**
     * 基于底层方法构建<code>CtMethod</code>方法。
     *
     * @param clazz 代理方法所在的CtClass类。
     * @param method 底层的方法。
     *
     * @return CtMethod方法。
     *
     * @throws CannotCompileException when bytecode transformation has failed.
     */
    private CtMethod createInovkeMethod(CtClass clazz, Method method) throws CannotCompileException {
        StringBuilder body = new StringBuilder("public Object invoke(Method m, Object obj, Object[] params){");
        Class<?> targetClass = method.getDeclaringClass();
        boolean voidMethod = void.class == method.getReturnType();
        if (!voidMethod)
            body.append("return ($w)");
        //static method needs to import class package to invoke by simple class name
        if (Modifier.isStatic(method.getModifiers())) {
            body.append(targetClass.getName());
        } else {
            body.append("((").append(targetClass.getName()).append(")obj)");
        }
        //invoke begin
        body.append(".");
        Class<?>[] parameterTypes = method.getParameterTypes();
        //no parameters method
        if (parameterTypes.length == 0) {
            body.append(method.getName()).append("()").toString();
        } else {
            body.append(method.getName()).append("(");
            for (int i = 0; i < parameterTypes.length - 1; i++) {
                body.append(getClassName(parameterTypes[i], "params[" + i + "]"));
                body.append(",");
            }
            body.append(getClassName(parameterTypes[parameterTypes.length - 1], "params[" + (parameterTypes.length - 1) + "]"));
            body.append(")");
        }
        body.append(";");
        body.append(voidMethod ? "return null;}" : "}");
        return CtNewMethod.make(body.toString(), clazz);
    }

    /**
     * 指定对象类型（包括基本类型）的名称转换。
     *
     * @param clazz 指定的对象类型。
     * @param parameter 参数名称。
     *
     * @return 转换后的名称。
     */
    private String getClassName(Class<?> clazz, String parameter) {
        return void.class != clazz && clazz.isPrimitive()
                ? (clazz == boolean.class
                ? "((Boolean)" + parameter + ").booleanValue()"
                : "((Number)" + parameter + ")." + clazz.getCanonicalName() + "Value()")
                : "(" + clazz.getCanonicalName() + ")" + parameter;
    }
}
