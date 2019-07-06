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

import java.lang.reflect.Field;
import java.util.*;
import net.jrouter.annotation.InterceptorStack;
import net.jrouter.util.CollectionUtil;

/**
 * 拦截栈的代理类，包括了拦截栈的名称、字段来源及其所包含的拦截器集合。
 */
public final class InterceptorStackProxy {

    /** 拦截栈名称 */
    @lombok.Getter
    private final String name;

    /** 拦截栈取名字段 */
    private final Field field;

    /** 拦截栈 */
    @lombok.Getter
    private final InterceptorStack interceptorStack;

    /** 包含的拦截器集合 */
    @lombok.Getter(lombok.AccessLevel.PACKAGE)
    private final List<InterceptorDelegate> interceptorDelegates;

    /**
     * 构造一个拦截栈的代理类，包含拦截栈的名称、字段来源及其所包含的拦截器集合。
     *
     * @param name 拦截栈的名称。
     * @param field 拦截栈字段来源。
     * @param interceptorDelegates 包含的拦截器集合。
     */
    public InterceptorStackProxy(String name, Field field, InterceptorStack interceptorStack, List<InterceptorDelegate> interceptorDelegates) {
        this.name = name;
        this.field = field;
        this.interceptorStack = interceptorStack;
        this.interceptorDelegates = interceptorDelegates;
    }

    /**
     * 返回拦截栈所包含的拦截器的代理集合。
     *
     * @return 拦截栈所包含的拦截器的代理集合。
     */
    public List<InterceptorProxy> getInterceptors() {
        if (CollectionUtil.isNotEmpty(interceptorDelegates)) {
            List<InterceptorProxy> interceptors = new ArrayList<>(interceptorDelegates.size());
            for (InterceptorDelegate delegate : interceptorDelegates) {
                interceptors.add(delegate.interceptorProxy);
            }
            return interceptors;
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 返回拦截栈取名字段的信息。
     *
     * @return 拦截栈取名字段的信息。
     */
    public String getFieldName() {
        return field.toString();
    }

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append('[');
        if (interceptorDelegates != null) {
            Iterator<InterceptorDelegate> it = interceptorDelegates.iterator();
            while (it.hasNext()) {
                msg.append(it.next().interceptorProxy.getName());
                if (it.hasNext()) {
                    msg.append(", ");
                }
            }
        }
        msg.append(']');
        return msg.toString();
    }

    /**
     * Extended InterceptorProxy.
     */
    @lombok.Getter
    public final static class InterceptorDelegate {

        /**
         * InterceptorStack.Interceptor.
         */
        private final InterceptorStack.Interceptor interceptor;

        /**
         * Interceptor Proxy.
         */
        private final InterceptorProxy interceptorProxy;

        /**
         * Constructor.
         */
        public InterceptorDelegate(InterceptorStack.Interceptor interceptor, InterceptorProxy interceptorProxy) {
            Objects.requireNonNull(interceptor);
            Objects.requireNonNull(interceptorProxy);
            this.interceptor = interceptor;
            this.interceptorProxy = interceptorProxy;
        }
    }
}
