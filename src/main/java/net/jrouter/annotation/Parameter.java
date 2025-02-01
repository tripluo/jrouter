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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数。名称/值（多值）。
 *
 * @see Action#parameters()
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Parameter {

    /**
     * 参数名称。
     * @return 参数名称。
     */
    String name();

    /**
     * 参数名称对应的值/多值。
     * @return 参数名称对应的值/多值。
     */
    String[] value();

}
