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

import java.util.Map;
import jrouter.annotation.Action;

/**
 * ActionFactory接口。
 *
 * @param <K> 调用{@link Action}的标识。
 */
public interface ActionFactory<K> {

    /**
     * 返回创建对象的工厂对象。
     *
     * @return 创建对象的工厂对象。
     */
    ObjectFactory getObjectFactory();

    /**
     * 返回创建方法调用对象的工厂对象。
     *
     * @return 创建方法调用对象的工厂对象。
     */
    MethodInvokerFactory getMethodInvokerFactory();

    /**
     * 返回创建底层方法转换器的工厂对象。
     *
     * @return 创建底层方法转换器的工厂对象。
     */
    <T extends ActionInvocation<?>> ConverterFactory<T> getConverterFactory();

    /**
     * 通过路径调用相应的Action，可以传递Action代理方法相应的参数。
     *
     * @param <T> 调用Action的结果类型。
     * @param key Action的映射标识。
     * @param params 用于Action的调用参数。
     *
     * @return 调用后的结果。
     *
     * @throws JRouterException 如果发生调用错误。
     */
    <T> T invokeAction(K key, Object... params) throws JRouterException;

    /**
     * 移除ActionFactory中所有关联关系。
     *
     * @throws JRouterException 如果发生错误。
     */
    void clear() throws JRouterException;

    /**
     * 返回Action集合。
     *
     * @return Action集合。
     */
    Map<String, ?> getActions();

    /**
     * 返回拦截器集合。
     *
     * @return 拦截器集合。
     */
    Map<String, ? extends AbstractProxy> getInterceptors();

    /**
     * 返回拦截栈集合。
     *
     * @return 拦截栈集合。
     */
    Map<String, ?> getInterceptorStacks();

    /**
     * 返回结果类型集合。
     *
     * @return 结果类型集合。
     */
    Map<String, ? extends AbstractProxy> getResultTypes();

    /**
     * 返回结果对象集合。
     *
     * @return 结果对象集合。
     */
    Map<String, ? extends AbstractProxy> getResults();

    /**
     * 返回默认拦截栈名称。
     *
     * @return 默认拦截栈名称。
     */
    String getDefaultInterceptorStack();

    /**
     * 设置默认视图类型。
     *
     * @return 默认视图类型。
     */
    String getDefaultResultType();
}
