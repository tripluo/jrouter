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

package net.jrouter.impl;

import net.jrouter.ActionFactory;
import net.jrouter.annotation.Result;
import net.jrouter.annotation.ResultType;

import java.lang.reflect.Method;

/**
 * 结果对象的代理类，包括了结果对象及调用参数的状态。
 *
 * @deprecated
 */
@Deprecated
public final class ResultProxy extends DefaultProxy {

    /**
     * 结果对象
     */
    @lombok.Getter
    private final Result result;

    /**
     * 结果对象相应的结果类型 未完成
     */
    private ResultType resultType;

    /**
     * 构造一个结果对象的代理类，包含指定的结果对象及其调用参数的状态。
     * @param actionFactory 指定的ActionFactory。
     * @param result 代理的结果对象。
     * @param method 代理的方法。
     * @param object 代理的方法的对象。
     */
    public ResultProxy(ActionFactory<?> actionFactory, Result result, Method method, Object object) {
        super(method, object, actionFactory);
        this.result = result;
    }

}
