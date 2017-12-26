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
package jrouter;

import java.lang.reflect.Method;
import jrouter.util.MethodUtil;

/**
 * 提供方法代理的一个抽象类，包括了方法所属的对象、方法对象本身及调用方法是否单例。
 */
public abstract class AbstractProxy {

    /** 方法对象 */
    @lombok.Getter
    protected Method method;

    /** 方法所在的对象 */
    @lombok.Getter
    protected Object object;

    /**
     * 指定方法及其对象的构造方法。
     *
     * @param method 指定的方法。
     * @param object 指定的对象。
     */
    public AbstractProxy(Method method, Object object) {
        this.method = method;
        this.object = object;
    }

    /**
     * 调用所代理的方法。
     *
     * @param params 用于方法调用的参数。
     *
     * @return 方法调用后的结果。
     *
     * @throws JRouterException 如果发生方法调用错误。
     *
     * @see Method#invoke(java.lang.Object, java.lang.Object[])
     */
    public abstract Object invoke(Object... params) throws JRouterException;

    /**
     * 返回调用方法的描述信息。
     *
     * @return 调用方法的描述信息。
     */
    public String getMethodInfo() {
        return MethodUtil.getMethod(method);
    }
}
