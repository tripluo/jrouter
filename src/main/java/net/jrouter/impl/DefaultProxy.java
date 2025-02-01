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

package net.jrouter.impl;

import net.jrouter.AbstractProxy;
import net.jrouter.ActionFactory;
import net.jrouter.Invoker;
import net.jrouter.util.MethodUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 默认方法代理类实现，封装了调用代理方法时及异常的处理。
 */
public class DefaultProxy extends AbstractProxy {

    /**
     * 底层方法的调用对象
     */
    private Invoker invoker;

    /**
     * 指定方法及其对象的构造方法。
     * @param method 指定的方法。
     * @param object 指定的对象。
     * @param actionFactory 指定的ActionFactory。
     */
    public DefaultProxy(Method method, Object object, ActionFactory<?> actionFactory) {
        super(method, object);
        if (actionFactory != null && actionFactory.getMethodInvokerFactory() != null) {
            Class<?> targetClass = ((object == null || actionFactory.getObjectFactory() == null)
                    ? method.getDeclaringClass() : actionFactory.getObjectFactory().getClass(object));
            this.invoker = actionFactory.getMethodInvokerFactory().newInstance(targetClass, method);
        }
    }

    @Override
    public Object invoke(Object... params) {
        return invoke(method, object, params);
    }

    /**
     * 使用Java反射或调用对象调用底层方法。
     * @param method 底层方法。
     * @param obj 调用底层方法的对象。
     * @param params 用于方法调用的参数。
     * @return 方法调用后的结果。
     */
    private Object invoke(Method method, Object obj, Object... params) {
        try {
            return invoker == null ? method.invoke(obj, params) : invoker.invoke(method, obj, params);
        }
        catch (IllegalAccessException e) {
            throw new InvocationProxyException(e, this);
        }
        catch (InvocationTargetException e) {
            throw new InvocationProxyException(e.getTargetException(), this);// NOPMD
            // PreserveStackTrace
        } // convert Exception to InvocationProxyException
        catch (Exception e) { // NOPMD IdenticalCatchBranches
            throw new InvocationProxyException(e, this);
        }
    }

    /**
     * 返回调用方法的描述信息。
     * @return 调用方法的描述信息。
     */
    public String getMethodInfo() {
        return MethodUtil.getMethod(method);
    }

}
