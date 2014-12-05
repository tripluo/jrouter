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
package jrouter;

import java.util.List;
import java.util.Map;
import jrouter.annotation.Action;
import jrouter.annotation.Interceptor;
import jrouter.annotation.Result;

/**
 * Action代理类接口。
 */
public interface ActionProxy {

    /**
     * 返回所代理的Action对象。
     *
     * @return 代理的Action对象。
     */
    Action getAction();

    /**
     * 返回Action所对应的全路径。
     *
     * @return Action所对应的全路径。
     */
    String getPath();

    /**
     * 返回Action的命名空间。
     *
     * @return Action的命名空间。
     */
    String getNamespace();

    /**
     * 返回Action初始化参数键/值（多值）映射，不包含任何参数映射则返回长度为 0 的映射。
     *
     * @return Action初始化参数键/值（多值）映射。
     *
     * @see Action#parameters()
     */
    Map<String, String[]> getActionParameters();

    /**
     * 由指定名称返回Action初始化参数中字符串形式的值。
     * <p>
     * 如果值不存在则返回 null；
     * 如果为多值，请使用{@link #getActionParameterValues}。
     * 如果为多值，请使用返回多值数组中的第一个。
     * </p>
     *
     * @param name 指定的名称。
     *
     * @return Action初始化参数中字符串形式的值。
     *
     * @see #getActionParameterValues(java.lang.String)
     */
    String getActionParameter(String name);

    /**
     * 由指定名称返回Action初始化参数中多值字符串数组。
     * <p>
     * 如果值不存在，则返回 null。如果值为空，则返回长宽为 0 的数组。
     * </p>
     *
     * @param name 指定的名称。
     *
     * @return Action初始化参数中多值字符串数组。
     *
     * @see #getActionParameter(java.lang.String)
     */
    String[] getActionParameterValues(String name);

    /**
     * 返回Action所配置的拦截器集合，不包含任何拦截器则返回长度为 0 的集合。
     *
     * @return Action所配置的拦截器集合。
     *
     * @see Action#interceptors()
     */
    List<Interceptor> getInterceptors();

    /**
     * 返回Action的结果对象集合，不包含任何结果对象则返回长度为 0 的集合。
     *
     * @return Action的结果对象集合。
     *
     * @see Action#results()
     */
    Map<String, Result> getResults();

    /**
     * 返回调用方法的描述信息。
     *
     * @return 调用方法的描述信息。
     */
    String getMethodInfo();
}
