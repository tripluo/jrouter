/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jrouter.impl;

import lombok.Getter;
import net.jrouter.JRouterException;

/**
 * 标识代理的方法调用发生错误时的异常。 此异常类型限制外部构造生成，避免用户直接抛出此异常引发错误的异常吞噬。
 */
@Getter
public class InvocationProxyException extends JRouterException {

    private static final long serialVersionUID = 1L;

    /**
     * 异常发生的对象。
     */
    private final Object target;

    /**
     * 构造一个包含指定原因和异常发生对象的InvocationException。
     * @param cause 异常原因。
     * @param target 异常发生的对象。
     * @see #getSource()
     */
    InvocationProxyException(Throwable cause, Object target) {
        super(cause);
        this.target = target;
    }

    /**
     * 构造一个包含指定详细消息、原因和异常发生对象的InvocationException。
     * @param cause 异常原因。
     * @param target 异常发生的对象。
     * @see #getSource()
     */
    InvocationProxyException(String message, Throwable cause, Object target) {
        super(message, cause);
        this.target = target;
    }

    /**
     * 返回首个非InvocationException类型的cause。
     * @return 首个非InvocationException的cause，如果 cause 不存在或是未知的，则返回 null。
     */
    public Throwable getSource() {
        Throwable cur = this.getCause();
        while (cur instanceof InvocationProxyException && (cur = cur.getCause()) != null) { // NOPMD
            // for
            // EmptyControlStatement
        }
        return cur;
    }

    /**
     * 返回最初抛出的InvocationException。
     * @return 最初抛出的InvocationException。
     */
    public InvocationProxyException getSourceInvocationException() {
        Throwable cur = this;
        Throwable cause;
        while ((cause = cur.getCause()) instanceof InvocationProxyException) {
            cur = cause;
        }
        return (InvocationProxyException) cur;
    }

}
