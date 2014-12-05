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

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

/**
 * 拦截栈的代理类，包括了拦截栈的名称、字段来源及其所包含的拦截器集合。
 */
public final class InterceptorStackProxy {

    /** 拦截栈名称 */
    private final String name;

    /** 拦截栈取名字段 */
    private final Field field;

    /** 包含的拦截器集合 */
    private final List<InterceptorProxy> interceptors;

    /**
     * 构造一个拦截栈的代理类，包含拦截栈的名称、字段来源及其所包含的拦截器集合。
     *
     * @param name 拦截栈的名称。
     * @param field 拦截栈字段来源。
     * @param interceptors 包含的拦截器集合。
     */
    public InterceptorStackProxy(String name, Field field, List<InterceptorProxy> interceptors) {
        this.name = name;
        this.field = field;
        this.interceptors = interceptors;
    }

    /**
     * 返回拦截栈所包含的拦截器集合，不包含任何拦截器则返回 null。
     *
     * @return 包含的拦截器集合。
     */
    public List<InterceptorProxy> getInterceptors() {
        return interceptors;
    }

    /**
     * 返回拦截栈名称。
     *
     * @return 拦截栈名称。
     */
    public String getName() {
        return name;
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
        if (interceptors != null) {
            Iterator<InterceptorProxy> it = interceptors.iterator();
            while (it.hasNext()) {
                msg.append(it.next().getName());
                if (it.hasNext())
                    msg.append(", ");
            }
        }
        msg.append(']');
        return msg.toString();
    }
}
