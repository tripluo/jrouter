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

package net.jrouter.annotation;

import java.lang.annotation.*;

/**
 * Namespace，定义于类{@code Class}上的注解。
 * <p>
 * Namespace上拦截器集合：
 * </p>
 * <p>
 * Namespace的拦截器集合 = 存在Namespace指定的拦截栈 ? 指定拦截栈的拦截器集合 + 指定的拦截器集合 : 指定的拦截器集合。
 * </p>
 * <p>
 * 若需表明Namespace不包含任何拦截器，应该指定其拦截栈为空拦截栈；直接指定其拦截器集合为空集合不产生任何效果。
 *
 * @see net.jrouter.interceptor.DefaultInterceptorStack#EMPTY_INTERCEPTOR_STACK
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Namespace {

    /**
     * 命名空间名称。
     * @return 命名空间名称。
     */
    String name();

    /**
     * 命名空间的拦截栈名称。
     * @return 命名空间的默认拦截栈名称。
     */
    String interceptorStack() default "";

    /**
     * 指定的拦截器集合的名称集合，指定空集合无效。
     * @return 指定拦截器集合的名称集合。
     * @see net.jrouter.interceptor.DefaultInterceptorStack#EMPTY_INTERCEPTOR_STACK
     * @deprecated 1.8.1
     */
    @Deprecated
    String[] interceptors() default {};

    /**
     * 是否自动加载定义类所有public/protected方法，默认不自动加载。
     * @return 是否自动加载所定义类的public/protected方法，默认不自动加载。
     */
    boolean autoIncluded() default false;

}
