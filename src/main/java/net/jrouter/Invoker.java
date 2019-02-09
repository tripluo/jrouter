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
package net.jrouter;

import java.lang.reflect.Method;

/**
 * 调用底层方法的接口。
 */
public interface Invoker {

    /**
     * 由指定对象和参数调用方法。
     *
     * @param <T> 方法调用结果的类型。
     * @param method {@code Method}对象。
     * @param obj 调用方法的对象。
     * @param params 用于方法调用的参数。
     *
     * @return 方法调用后的结果。
     *
     * @throws JRouterException 如果发生调用异常。
     */
    <T> T invoke(Method method, Object obj, Object... params) throws JRouterException;
}
