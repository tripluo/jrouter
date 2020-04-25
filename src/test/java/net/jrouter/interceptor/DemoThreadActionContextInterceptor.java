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

package net.jrouter.interceptor;

import net.jrouter.ActionInvocation;
import net.jrouter.annotation.Interceptor;
import net.jrouter.annotation.InterceptorStack;

/**
 * 使用{@link ThreadLocal}存储{@link ActionInvocation}的拦截器。
 * Action调用前存储ActionInvocation，默认调用结束后清除。
 */
public class DemoThreadActionContextInterceptor {

    @InterceptorStack(interceptors = {@InterceptorStack.Interceptor("demoThread")})
    public static final String DEMO_THREAD = "demoThread";

    /** ThreadLocal */
    private static final ThreadLocal<ActionInvocation> THREAD_LOCAL = new ThreadLocal<>();

    /** Action调用结束后是否清除线程变量中的ActionInvocation对象，默认清除 */
    private boolean removeActionInvocation = true;

    public DemoThreadActionContextInterceptor() {
    }

    public DemoThreadActionContextInterceptor(boolean removeActionInvocation) {
        this.removeActionInvocation = removeActionInvocation;
    }

    /**
     * 拦截器。
     */
    @Interceptor(name = DEMO_THREAD)
    public Object test(ActionInvocation invocation) {
        try {
            THREAD_LOCAL.set(invocation);
            return invocation.invoke();
        } finally {
            //just keep thread local ActionInvocation for test
            if (removeActionInvocation)
                THREAD_LOCAL.remove();
        }
    }

    /**
     * 返回线程变量中的ActionInvocation对象。
     *
     * @return ActionInvocation对象。
     */
    public static ActionInvocation get() {
        return THREAD_LOCAL.get();
    }
}
