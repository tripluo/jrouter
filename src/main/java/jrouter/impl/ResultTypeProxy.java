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
package jrouter.impl;

import java.lang.reflect.Method;
import jrouter.ActionFactory;
import jrouter.annotation.Action;
import jrouter.annotation.ResultType;

/**
 * 结果类型的代理类，包括了结果类型的名称及在对{@link Action}做结果调用时是否将{@link Action}的运行时状态作为参数传递。
 */
public final class ResultTypeProxy extends DefaultProxy {

    /** 结果类型的名称 */
    private final String type;

    /** 结果类型 */
    private final ResultType resultType;

    /**
     * 构造一个结果类型的代理类，包含指定的结果类型、代理的方法及方法的对象。
     *
     * @param actionFactory 指定的ActionFactory。
     * @param resultType 所代理的结果类型。
     * @param method 代理的方法。
     * @param object 代理的方法的对象。
     */
    public ResultTypeProxy(ActionFactory<?> actionFactory, ResultType resultType, Method method,
            Object object) {
        super(method, object, actionFactory.getMethodInvokerFactory());
        this.resultType = resultType;
        this.type = resultType.type().trim();
    }

    /**
     * 返回结果类型的名称。
     *
     * @return 结果类型的名称。
     */
    public String getType() {
        return type;
    }

    /**
     * 返回结果类型。
     *
     * @return 结果类型。
     */
    public ResultType getResultType() {
        return resultType;
    }
}
