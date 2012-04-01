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

/**
 * 默认方法代理类实现，封装了调用代理方法时及异常的处理。
 */
public class DefaultProxy extends AbstractProxy {

    //是否为带有可变数量参数的方法
    private boolean varArgs = false;

    //varArg argument's index, start at 0 if is varArgs.
    private int varArgIndex = -1;

    //可变数量参数的类型
    private Class<?> varArgClass = null;

    //变长方法的空值
    private Object[] empty;

    /**
     * 未指定方法及其对象的构造方法。
     */
    public DefaultProxy() {
    }

    /**
     * 指定方法及其对象的构造方法。
     *
     * @param method 指定的方法。
     * @param object 指定的对象。
     */
    public DefaultProxy(Method method, Object object) {
        super(method, object);
        this.varArgs = method.isVarArgs();
        if (varArgs) {
            this.varArgIndex = method.getParameterTypes().length - 1;
            varArgClass = method.getParameterTypes()[varArgIndex].getComponentType();
            empty = (Object[]) Array.newInstance(varArgClass, 0);
        }
    }

    /**
     * 指定对象、方法及对象状态（是否单例）的构造方法。
     *
     * @param method 指定的方法。
     * @param object 指定的对象。
     * @param singleton 对象是否单例。
     */
    public DefaultProxy(Method method, Object object, boolean singleton) {
        super(method, object, singleton);
        this.varArgs = method.isVarArgs();
        if (varArgs) {
            this.varArgIndex = method.getParameterTypes().length - 1;
            varArgClass = method.getParameterTypes()[varArgIndex].getComponentType();
            empty = (Object[]) Array.newInstance(varArgClass, 0);
        }
    }

    @Override
    public Object invoke(Object... params) {
        try {
            //invoke varArgs
            if (varArgs) {
                //pass varArgs as null
                if (params == null) {
                    //have only varArgs
                    return varArgIndex == 0
                            ? method.invoke(object, new Object[]{null})
                            : method.invoke(object, new Object[]{null, empty});
                } //pass no varArgs
                else if (params.length == varArgIndex) {
                    //have only varArgs
                    if (varArgIndex == 0) {
                        return method.invoke(object, new Object[]{empty});
                    } //have both normal arguments and varArgs
                    else {
                        Object[] _varArgs = new Object[params.length + 1];
                        System.arraycopy(params, 0, _varArgs, 0, params.length);
                        _varArgs[params.length] = empty;
                        return method.invoke(object, _varArgs);
                    }
                } //pass varArgs not as array
                else if (params.length > varArgIndex) {
                    if (params[varArgIndex] == null || !params[varArgIndex].getClass().isArray()) {
                        Object[] _params = new Object[varArgIndex + 1];
                        if (varArgIndex > 0)
                            System.arraycopy(params, 0, _params, 0, varArgIndex);
                        Object[] _varArgs = (Object[]) Array.newInstance(varArgClass, params.length - varArgIndex);
                        System.arraycopy(params, varArgIndex, _varArgs, 0, params.length - varArgIndex);
                        _params[varArgIndex] = _varArgs;
                        return method.invoke(object, _params);
                    }
                }
            }
            return method.invoke(object, params);
        } catch (IllegalAccessException e) {
            throw new InvocationProxyException(e, this);
        } catch (InvocationTargetException e) {
            throw new InvocationProxyException(e.getTargetException(), this);
        }
    }
}
