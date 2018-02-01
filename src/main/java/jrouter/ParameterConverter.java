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

import java.lang.reflect.Method;
import jrouter.annotation.Dynamic;

/**
 * 转换底层方法调用参数的转换器。
 */
@Dynamic
public interface ParameterConverter {

    /**
     * 传递底层方法、调用对象及原有的参数，返回转换处理后的调用参数。
     * 原调用参数由ActionFactory的invokeAction方法指定传递。
     *
     * @param method 底层方法。
     * @param obj 从中调用底层方法的对象。
     * @param invokeParams 直接传递于方法调用的参数。
     * @param convertParams 提供给转换器的参数。
     *
     * @return 转换处理后的调用参数。
     *
     * @throws JRouterException 如果发生转换异常。
     *
     * @see ActionFactory#invokeAction(java.lang.Object, java.lang.Object...)
     */
    Object[] convert(Method method, Object obj, Object[] invokeParams, Object[] convertParams) throws JRouterException;
}
