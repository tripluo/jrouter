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
 * Action，定义于方法上的注解。
 * <p>
 * Action上拦截器集合：</p>
 * <p>
 * Action指定的拦截器集合 = 存在Action指定的拦截栈 ? 指定拦截栈的拦截器集合 + 指定的拦截器集合 : 指定的拦截器集合。
 * </p>
 * <p>
 * Action最终的拦截器集合 = Action指定拦截器集合 > 命名空间拦截器集合 > 全局默认拦截器集合。
 * </p>
 *
 * <p>
 * Action上拦截栈优先级：</p>
 * <p>
 * Action指定拦截栈 > 命名空间拦截栈 > 全局默认拦截栈。
 * </p>
 *
 * 若需表明Action不包含任何拦截器，应该指定其拦截栈为空拦截栈；直接指定其拦截器集合为空集合不产生任何效果。
 *
 * @see jrouter.interceptor.DefaultInterceptorStack#EMPTY_INTERCEPTOR_STACK
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Action {

    /**
     * Action名称，可多个路径映射同一个Aciton。
     * 等同于{@link #name()}，name属性非空时优先选取name值，当仅需要name属性时提供便捷的注解方式。
     *
     * @return Action名称。
     *
     * @see #name()
     *
     * @since 1.7.1
     */
    String[] value() default {};

    /**
     * Action名称，可多个路径映射同一个Aciton，需保证其最终生成路径的唯一性。
     * Action名称为空时，默认取其所在的方法名称（区分大小写）。
     *
     * @return Action名称。
     *
     * @see #value()
     */
    String[] name() default {};

    /**
     * Action指定的拦截栈名称。
     *
     * @return Action指定的拦截栈名称。
     */
    String interceptorStack() default "";

    /**
     * Action指定的拦截器集合的名称集合，指定空集合无效。
     *
     * @return Action指定的拦截器集合的名称集合。
     */
    String[] interceptors() default {};

    /**
     * 结果对象集合。
     *
     * @return 结果对象集合。
     */
    Result[] results() default {};

    /**
     * 调用的范围，Action默认为单例。
     *
     * @return Action单例或非单例。
     */
    Scope scope() default Scope.SINGLETON;

    /**
     * Action初始化参数的键/值（多值）集合。
     *
     * @return Action初始化参数的键/值（多值）集合。
     */
    Parameter[] parameters() default {};
}
