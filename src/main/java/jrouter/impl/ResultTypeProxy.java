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
import jrouter.annotation.ResultType;

/**
 * 结果类型的代理类，包括了结果类型的名称及在对Action做结果调用时是否将Action的运行时状态作为参数传递。
 */
public final class ResultTypeProxy extends DefaultProxy {

    /**
     * 结果类型的名称
     */
    private String type;

    /**
     * 结果类型
     */
    private ResultType resultType;

    /**
     * 是否传递ActionInvocation参数
     */
    private boolean requireAction;

    /**
     * 构造一个结果类型的代理类，包含指定的结果类型、代理的方法及方法的对象。
     *
     * @param resultType 所代理的结果类型。
     * @param method 代理的方法。
     * @param object 代理的方法的对象。
     * @param requireAction 结果类型调用是否传递ActionInvocation参数。
     */
    public ResultTypeProxy(ResultType resultType, Method method, Object object,
            boolean requireAction) {
        super(method, object);
        this.resultType = resultType;
        this.type = resultType.type().trim();
        this.requireAction = requireAction;
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

    /**
     * 结果类型调用是否传递ActionInvocation参数。
     *
     * @return 结果类型的调用是否传递ActionInvocation参数。
     */
    public boolean isRequireAction() {
        return requireAction;
    }
}
