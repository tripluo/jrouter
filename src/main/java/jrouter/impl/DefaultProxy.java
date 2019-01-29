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
package jrouter.impl;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import jrouter.AbstractProxy;
import jrouter.ActionFactory;
import jrouter.Invoker;
import jrouter.util.MethodUtil;

/**
 * 默认方法代理类实现，封装了调用代理方法时及异常的处理。
 */
public class DefaultProxy extends AbstractProxy {

    /** 是否为带有可变数量参数的方法 */
    private boolean varArgs = false;

    /**
     * varArg argument's index, start at 0 if is varArgs.
     */
    private int varArgIndex = -1;

    /** 可变数量参数的类型 */
    private Class<?> varArgClass = null;

    /** 变长方法的空值 */
    private Object[] empty;

    /** 底层方法的调用对象 */
    private Invoker invoker;

    /**
     * 指定方法及其对象的构造方法。
     *
     * @param method 指定的方法。
     * @param object 指定的对象。
     * @param actionFactory 指定的ActionFactory。
     */
    public DefaultProxy(Method method, Object object, ActionFactory actionFactory) {
        super(method, object);
        this.varArgs = method.isVarArgs();
        if (varArgs) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            this.varArgIndex = parameterTypes.length - 1;
            varArgClass = parameterTypes[varArgIndex].getComponentType();
            empty = (Object[]) Array.newInstance(varArgClass, 0);
        }
        if (actionFactory != null && actionFactory.getMethodInvokerFactory() != null) {
            Class<?> targetClass = ((object == null || actionFactory.getObjectFactory() == null)
                    ? method.getDeclaringClass() : actionFactory.getObjectFactory().getClass(object));
            this.invoker = actionFactory.getMethodInvokerFactory().newInstance(targetClass, method);
        }
    }

    @Override
    public Object invoke(Object... params) {
        //invoke varArgs
        if (varArgs) {
            //pass varArgs as null
            if (params == null) {
                //have only varArgs
                return varArgIndex == 0
                        ? invoke(method, object, new Object[]{null})
                        //varArgs前有多参数仅补足第一个参数为null，否则调用抛出InvocationTargetException异常
                        : invoke(method, object, new Object[]{null, empty});
            } //pass no varArgs
            else if (params.length == varArgIndex) {
                //have only varArgs
                if (varArgIndex == 0) {
                    return invoke(method, object, new Object[]{empty});
                } //have both normal arguments and varArgs
                else {
                    Object[] tmpArgs = new Object[params.length + 1];
                    System.arraycopy(params, 0, tmpArgs, 0, params.length);
                    tmpArgs[params.length] = empty;
                    return invoke(method, object, tmpArgs);
                }
            } //pass varArgs not as array
            else if (params.length > varArgIndex) {
                if (params[varArgIndex] == null || !params[varArgIndex].getClass().isArray()) {
                    Object[] tmpParams = new Object[varArgIndex + 1];
                    if (varArgIndex > 0) {
                        System.arraycopy(params, 0, tmpParams, 0, varArgIndex);
                    }
                    Object[] tmpArgs = (Object[]) Array.newInstance(varArgClass, params.length - varArgIndex);
                    System.arraycopy(params, varArgIndex, tmpArgs, 0, params.length - varArgIndex);
                    tmpParams[varArgIndex] = tmpArgs;
                    return invoke(method, object, tmpParams);
                }
            }
        }
        return invoke(method, object, params);
    }

    /**
     * 使用Java反射或调用对象调用底层方法。
     *
     * @param <T> 方法调用后结果的类型。
     * @param method 底层方法。
     * @param obj 调用底层方法的对象。
     * @param params 用于方法调用的参数。
     *
     * @return 方法调用后的结果。
     */
    private Object invoke(Method method, Object obj, Object... params) {
        try {
            return invoker == null ? method.invoke(obj, params) : invoker.invoke(method, obj, params);
        } catch (IllegalAccessException e) {
            throw new InvocationProxyException(e, this);
        } catch (InvocationTargetException e) {
            throw new InvocationProxyException(e.getTargetException(), this);//NOPMD PreserveStackTrace
        } //convert Exception to InvocationProxyException
        catch (Exception e) {//NOPMD IdenticalCatchBranches
            throw new InvocationProxyException(e, this);
        }
    }

    /**
     * 返回调用方法的描述信息。
     *
     * @return 调用方法的描述信息。
     */
    public String getMethodInfo() {
        return MethodUtil.getMethod(method);
    }
}
