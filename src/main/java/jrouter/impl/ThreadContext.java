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

import jrouter.ActionInvocation;

/**
 * ThreadContext是一个线程变量，使用了一个公共的{@link ThreadLocal}。
 * ThreadContext的初始化首先调用{@link #set(jrouter.impl.ThreadContext)}将其对象自身设置为线程变量。
 * ThreadContext默认包含一个{@link ActionInvocation}，存储线程安全的Action运行时上下文。
 */
public class ThreadContext {

    /** Thread Safe */
    protected static final ThreadLocal<ThreadContext> threadContext = new ThreadLocal<ThreadContext>();

    /** Action运行时上下文 */
    private ActionInvocation<?> actionInvocation;

    /**
     * 构造一个ThreadContext。
     */
    public ThreadContext() {
    }

    /**
     * 设置当前线程副本中的ThreadContext。
     *
     * @param <T> 存储在当前线程副本中变量的类型。
     * @param context 存储在当前线程副本中的ThreadContext。
     */
    public static <T extends ThreadContext> void set(T context) {
        threadContext.set(context);
    }

    /**
     * 返回当前线程副本中的ThreadContext。
     *
     * @param <T> 存储在当前线程副本中变量的类型。
     *
     * @return 前线程副本中的ThreadContext。
     */
    public static <T extends ThreadContext> T get() {
        return (T) threadContext.get();
    }

    /**
     * 移除前线程副本中的ThreadContext。
     */
    public static void remove() {
        threadContext.remove();
    }

    /**
     * 返回Action运行时上下文。
     *
     * @param <T> Action运行时上下文类型。
     *
     * @return Action运行时上下文。
     */
    public static <T extends ActionInvocation> T getActionInvocation() {
        return (T) get().actionInvocation;
    }

    /**
     * 设置Action运行时上下文。
     *
     * @param actionInvocation Action运行时上下文。
     */
    public static void setActionInvocation(ActionInvocation<?> actionInvocation) {
        get().actionInvocation = actionInvocation;
    }
}
