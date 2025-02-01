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

/**
 * ObjectFactory接口。负责创建新的对象实例。
 */
public interface ObjectFactory {

    /**
     * 根据指定的{@code Class}对象所表示的类生成一个新的对象实例。
     * @param <T> 生成对象实例的类型。
     * @param clazz 指定的{@code Class}对象。
     * @return 新的对象实例。
     */
    <T> T newInstance(Class<T> clazz);

    /**
     * 根据指定的{@code Object}对象（或代理）获取其所属的{@code Class}类型。
     * @param obj {@code Object}对象。
     * @return 指定{@code Object}对象所属的{@code Class}类型。
     */
    Class<?> getClass(Object obj);

}
