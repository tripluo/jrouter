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
 * 拦截栈，定义于字符串{@code String}变量上的注解。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InterceptorStack {

    /**
     * 拦截栈名称，未指定则默认取字符串变量的值。
     *
     * @return 拦截栈名称。
     */
    String name() default "";

    /**
     * TODO
     * 父拦截栈名称。
     *
     * @return 父拦截栈名称。
     */
    //String parent() default "";

    /**
     * 包含的拦截器集合。
     *
     * @return 所包含的拦截器集合。
     */
    Interceptor[] interceptors() default {};

    /**
     * 匹配的路径集合（默认空不包含任何）。
     *
     * @return 匹配的路径集合。
     *
     * @since 1.8.1
     */
    String[] include() default {};

    /**
     * 不匹配的路径集合（默认空不排除任何）。
     *
     * @return 不匹配的路径集合。
     *
     * @since 1.8.1
     */
    String[] exclude() default {};

    /**
     * 排序值，默认0。多匹配后者覆盖前者。
     *
     * @return 排序值。
     */
    int order() default 0;

    /**
     * 包含于拦截栈的拦截器配置。
     */
    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface Interceptor {

        /**
         * 拦截器名称。
         *
         * @return 拦截器名称。
         */
        String value();

        /**
         * 不匹配的路径集合（默认空不排除任何）。
         *
         * @return 不匹配的路径集合。
         *
         * @since 1.8.1
         */
        String[] exclude() default {};
    }
}
