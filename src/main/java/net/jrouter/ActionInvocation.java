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

import java.util.Map;
import net.jrouter.annotation.Action;
import net.jrouter.annotation.Dynamic;
import net.jrouter.annotation.Result;

/**
 * {@link Action}运行时上下文的代理接口。在{@link Action}调用时记录并返回其状态、调用参数、结果对象、ActionFactory等信息。
 *
 * @param <P> Action Path type.
 */
@Dynamic
public interface ActionInvocation<P> {

    /**
     * 返回ActionFactory。
     *
     * @return ActionFactory。
     */
    ActionFactory<P> getActionFactory();

    /**
     * 返回ActionInvocation持有的ActionProxy。
     *
     * @return ActionInvocation持有的ActionProxy。
     */
    ActionProxy<P> getActionProxy();

    /**
     * 返回Aciton调用的路径参数。
     *
     * @return Aciton调用的真实路径参数。
     */
    P getActionPath();

    /**
     * 返回Action是否已调用。
     *
     * @return Action是否已调用。
     */
    boolean isExecuted();

    /**
     * 返回Action的调用参数。
     *
     * @return Action的调用参数。
     */
    Object[] getParameters();

    /**
     * 由指定参数调用Action，并激发下一步操作（通常指拦截器调用）。
     *
     * @param <T> Action调用结果的类型。
     * @param params 调用参数。
     *
     * @return 调用后的结果。
     *
     * @throws JRouterException 如果发生调用错误。
     */
    <T> T invoke(Object... params) throws JRouterException;

    /**
     * 由指定参数调用Action（通常不触发拦截器调用）。
     *
     * @param <T> Action调用结果的类型。
     * @param params 调用参数。
     *
     * @return 调用后的结果。
     *
     * @throws JRouterException 如果发生调用错误。
     */
    <T> T invokeActionOnly(Object... params) throws JRouterException;

    /**
     * 返回Action调用的结果，如果Action尚未被调用或者尚未设置指定的值则返回null。
     *
     * @return Action调用的结果，如果Action尚未被调用则返回null。
     */
    Object getInvokeResult();

    /**
     * 设置Action调用的结果，可在Action调用期间记录结果对象。
     *
     * @param result Action调用的结果对象。
     */
    void setInvokeResult(Object result);

    /**
     * 设置Action调用完成后将执行的结果对象。
     *
     * @param result 指定的结果对象。
     */
    void setResult(Result result);

    /**
     * 返回Action调用完成后将执行的结果对象，若Action尚未调用则返回null。
     *
     * @return 结果对象。
     */
    Result getResult();

    /**
     * 设置Action运行时上下文中的方法参数转换器。
     *
     * @param parameterConverter 方法参数转换器。
     */
    void setParameterConverter(ParameterConverter parameterConverter);

    /**
     * 返回当前Action运行时上下文中的方法参数转换器。
     *
     * @return 方法参数转换器。
     */
    ParameterConverter getParameterConverter();

    /**
     * 设置提供给参数转换器的参数，转换参数可不同于原调用参数。
     *
     * @param params 提供给参数转换器的参数。
     */
    void setConvertParameters(Object... params);

    /**
     * 返回提供给参数转换器的参数，转换参数可不同于原调用参数。
     *
     * @return 提供给参数转换器的参数。
     */
    Object[] getConvertParameters();

    /**
     * 返回Action路径匹配的键值映射，不包含任何匹配的键值则返回空映射。
     *
     * @return Action路径匹配的键值映射。
     */
    Map<String, String> getPathParameters();
}
