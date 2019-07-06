package net.jrouter.interceptor;

import net.jrouter.InterceptorTestAction;
import net.jrouter.annotation.InterceptorStack;

/**
 * DemoInterceptorStack。
 *
 * @see InterceptorTestAction.Action4
 */
public class DemoInterceptorStack {

    /**
     * 测试拦截栈名称，指定匹配的路径。
     *
     * @see SampleInterceptor#logging
     */
    @InterceptorStack(
            interceptors = {
                    @InterceptorStack.Interceptor(SampleInterceptor.LOGGING)
            },
            include = {"/*/matched"},
            order = 1
    )
    public static final String MATCHED_INTERCEPTOR_STACK = "matched";
}
