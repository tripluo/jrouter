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
package jrouter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
     * 包含的拦截器名称集合。
     *
     * @return 所包含的拦截器名称集合。
     */
    String[] interceptors() default {};
}
