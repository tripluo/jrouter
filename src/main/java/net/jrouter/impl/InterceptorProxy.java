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

import java.lang.reflect.Method;
import net.jrouter.ActionFactory;
import net.jrouter.annotation.Action;
import net.jrouter.annotation.Interceptor;

/**
 * 拦截器的代理类，包括了拦截器的名称及在对{@link Action}做拦截调用时是否将{@link Action}的运行时状态作为参数传递等信息。
 */
public final class InterceptorProxy extends DefaultProxy {

    /**
     * 拦截器的名称。
     */
    @lombok.Getter
    private final String name;

    /**
     * 拦截器。
     */
    @lombok.Getter
    private final Interceptor interceptor;

    /**
     * 构造一个拦截器的代理类，包含指定的拦截器名称、拦截器调用参数的状态。
     *
     * @param actionFactory 指定的ActionFactory。
     * @param interceptor 所代理的拦截器。
     * @param method 代理的方法。
     * @param object 代理的方法的对象。
     */
    public InterceptorProxy(ActionFactory<?> actionFactory, Interceptor interceptor, Method method, Object object) {
        super(method, object, actionFactory);
        this.interceptor = interceptor;
        this.name = interceptor.name().trim();
    }
}
