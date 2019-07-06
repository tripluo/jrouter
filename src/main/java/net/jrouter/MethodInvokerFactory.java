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
 * 根据底层方法生成调用对象的工厂接口。
 */
public interface MethodInvokerFactory {

    /**
     * 由指定的底层方法及所表示的 {@code Class} 对象生成调用对象。
     *
     * @param targetClass 底层方法所表示的 {@code Class} 对象。
     * @param method 底层方法。
     *
     * @return Invoker调用对象。
     */
    Invoker newInstance(Class<?> targetClass, Method method);
}
