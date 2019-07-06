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
 * Path generator.
 *
 * @param <P> path type.
 *
 * @since 1.7.7
 */
//@FunctionalInterface
public interface PathGenerator<P> {

    /**
     * Generate the path(s).
     *
     * @param targetClass 底层方法所表示的 {@code Class} 对象。
     * @param method 底层方法。
     *
     * @return the generated Path(s).
     */
    P[] generatePath(Class<?> targetClass, Method method);
}
