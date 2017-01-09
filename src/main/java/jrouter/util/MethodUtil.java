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
package jrouter.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import jrouter.AbstractProxy;
import jrouter.ActionInvocation;
import jrouter.ParameterConverter;

/**
 * Method工具类。
 */
public class MethodUtil {

    /** The CGLIB class separator character "$$" */
    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    /** @see java.lang.reflect.Method#LANGUAGE_MODIFIERS */
    private static final int LANGUAGE_MODIFIERS
            = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE
            | Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL
            | Modifier.SYNCHRONIZED | Modifier.NATIVE;

    /**
     * 返回方法名和参数类型, 不包含返回类型及异常描述。
     *
     * @param method 指定的方法。
     *
     * @return 方法名和参数类型, 不包含返回类型及异常描述。
     *
     * @see java.lang.reflect.Method#toString()
     */
    public static String getSimpleMethod(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName()).append("(");
        Class[] params = method.getParameterTypes(); // avoid clone
        for (int j = 0; j < params.length; j++) {
            sb.append(getTypeName(params[j]));
            if (j < (params.length - 1))
                sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * 返回方法类名、方法名和参数类型, 不包含返回类型及异常描述。
     *
     * @param method 指定的方法。
     *
     * @return 方法类名、方法名和参数类型, 不包含返回类型及异常描述。
     *
     * @see java.lang.reflect.Method#toString()
     */
    public static String getMethod(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTypeName(method.getDeclaringClass())).append(".");
        sb.append(method.getName()).append("(");
        Class[] params = method.getParameterTypes(); // avoid clone
        for (int j = 0; j < params.length; j++) {
            sb.append(getTypeName(params[j]));
            if (j < (params.length - 1))
                sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * 返回此Class对象所表示的实体名称。
     *
     * @param type Class对象。
     *
     * @return Class对象所表示的实体名称。
     *
     * @see java.lang.reflect.Field#getTypeName(java.lang.Class)
     */
    public static String getTypeName(Class type) {
        if (type.isArray()) {
            try {
                Class cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) {
                /*FALLTHRU*/ }
        }
        String name = type.getName();
        int i = 0;
        if ((i = name.indexOf(CGLIB_CLASS_SEPARATOR)) > 0) {
            return name.substring(0, i);
        }
        return name;
    }

    /**
     * 返回方法信息, 包含类型参数、返回值及异常。
     *
     * @param method 指定的方法。
     *
     * @return 方法的描述信息, 包含类型参数、返回值及异常。
     *
     * @see java.lang.reflect.Method#toGenericString()
     */
    public static String getFullMethod(Method method) {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = method.getModifiers() & LANGUAGE_MODIFIERS;
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(" ");
            }
            Type[] typeparams = method.getTypeParameters();
            if (typeparams.length > 0) {
                boolean first = true;
                sb.append("<");
                for (Type typeparam : typeparams) {
                    if (!first)
                        sb.append(",");
                    if (typeparam instanceof Class) {
                        sb.append(((Class) typeparam).getName());
                    } else {
                        sb.append(typeparam.toString());
                    }
                    first = false;
                }
                sb.append("> ");
            }

            Type genRetType = method.getGenericReturnType();

            sb.append(((genRetType instanceof Class)
                    ? getTypeName((Class) genRetType) : genRetType.toString()) + " ");

            sb.append(getTypeName(method.getDeclaringClass())).append(".");

            sb.append(method.getName()).append("(");
            Type[] params = method.getGenericParameterTypes();
            for (int j = 0; j < params.length; j++) {
                sb.append((params[j] instanceof Class)
                        ? getTypeName((Class) params[j])
                        : (params[j].toString()));
                if (j < (params.length - 1))
                    sb.append(",");
            }
            sb.append(")");
            Type[] exceptions = method.getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append((exceptions[k] instanceof Class)
                            ? ((Class) exceptions[k]).getName()
                            : exceptions[k].toString());
                    if (k < (exceptions.length - 1))
                        sb.append(",");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 提供AbstractProxy子对象通过参数转换器调用底层方法。
     *
     * @param proxy AbstractProxy子对象。
     * @param converter 转换底层方法调用参数的转换器。
     * @param invokeParams 用于方法调用的原始参数。
     * @param additionalParams 提供给转换器的参数。
     *
     * @return AbstractProxy子对象底层方法调用后的结果。
     *
     * @see AbstractProxy#invoke(java.lang.Object...)
     */
    public static Object invoke(AbstractProxy proxy, ParameterConverter converter,
            Object[] invokeParams, Object[] additionalParams) {
        return converter == null
                ? proxy.invoke(invokeParams)
                : proxy.invoke(converter.convert(proxy.getMethod(), proxy, invokeParams, additionalParams));
    }

    /**
     * 提供AbstractProxy子对象通过Action运行时上下文的参数转换器调用底层方法。
     *
     * @param proxy AbstractProxy子对象。
     * @param invocation Action运行时上下文对象。
     *
     * @return AbstractProxy子对象底层方法调用后的结果。
     *
     * @see #invoke(jrouter.AbstractProxy, jrouter.ParameterConverter, java.lang.Object[], java.lang.Object[])
     */
    public static Object invoke(AbstractProxy proxy, ActionInvocation invocation) {
        return invoke(proxy, invocation.getParameterConverter(), null, invocation.getConvertParameters());
    }
}
