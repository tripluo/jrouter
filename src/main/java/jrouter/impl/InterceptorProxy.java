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

import java.lang.reflect.Method;
import jrouter.ActionFactory;
import jrouter.annotation.Interceptor;

/**
 * 拦截器的代理类，包括了拦截器的名称及在对Action做拦截调用时是否将Action的运行时状态作为参数传递等信息。
 */
public final class InterceptorProxy extends DefaultProxy {

    /**
     * 拦截器的名称
     */
    private final String name;

    /**
     * 拦截器
     */
    private final Interceptor interceptor;

    /**
     * 是否传递ActionInvocation参数
     */
    @Deprecated
    private final boolean requireAction;

    /**
     * 构造一个拦截器的代理类，包含指定的拦截器名称、拦截器调用参数的状态。
     *
     * @param actionFactory 指定的ActionFactory。
     * @param interceptor 所代理的拦截器。
     * @param method 代理的方法。
     * @param object 代理的方法的对象。
     * @param requireAction 拦截器调用是否传递ActionInvocation参数。
     */
    public InterceptorProxy(ActionFactory actionFactory, Interceptor interceptor, Method method,
            Object object, boolean requireAction) {
        super(method, object, actionFactory.getProxyFactory());
        this.interceptor = interceptor;
        this.name = interceptor.name().trim();
        this.requireAction = requireAction;
    }

    /**
     * 返回拦截器的名称。
     *
     * @return 拦截器的名称。
     */
    public String getName() {
        return name;
    }

    /**
     * 返回所代理的拦截器。
     *
     * @return 所代理的拦截器。
     */
    public Interceptor getInterceptor() {
        return interceptor;
    }

    /**
     * 判断拦截器调用是否传递ActionInvocation参数。
     *
     * @return 拦截器调用是否传递ActionInvocation参数。
     *
     * @deprecated since 1.6.6
     */
    public boolean isRequireAction() {
        return requireAction;
    }
}
