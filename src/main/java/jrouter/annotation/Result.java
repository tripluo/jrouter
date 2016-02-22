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
 * Result，定义于方法上的注解。
 * <p>当Result定义于注解Action中的子项时，表示Action特定的结果对象；</p>
 * <p>
 * 当Result单独定义于方法上时，表示某种特定{@code String}类型全局的结果对象。。
 * 当作为全局结果对象时，type值为空时直接调用后结束；type值不为空调用后再调用相应的{@link ResultType}。
 * </p>
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Result {

    /**
     * 结果对象的名称。
     *
     * @return 结果对象的名称。
     */
    String name();

    /**
     * 结果对象的类型名称。
     *
     * @return 结果对象的类型。
     */
    String type() default "";

    /**
     * 结果对象相关联的资源路径。
     *
     * @return 资源路径。
     */
    String location() default "";
}
