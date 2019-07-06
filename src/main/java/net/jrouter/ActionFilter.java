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
import net.jrouter.annotation.Action;
import net.jrouter.annotation.Namespace;

/**
 * ActionFilter接口。提供过滤{@code Method}和转换自定义注解类至{@link Action}。
 *
 * @since 1.7.4
 */
public interface ActionFilter {

    /**
     * 是否接受指定的方法。
     *
     * @param obj 调用方法的对象。
     * @param method 指定的方法。
     *
     * @return 是否接受指定的方法。
     */
    boolean accept(Object obj, Method method);

    /**
     * 根据指定的方法获取{@code Action}对象。
     *
     * @param obj 调用方法的对象。
     * @param method 指定的方法。
     *
     * @return {@code Action}对象。
     */
    Action getAction(Object obj, Method method);

    /**
     * 根据指定的方法获取{@code Namespace}对象。
     *
     * @param obj 调用方法的对象。
     * @param method 指定的方法。
     *
     * @return {@code Namespace}对象。
     */
    Namespace getNamespace(Object obj, Method method);
}
