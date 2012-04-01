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
package jrouter.interceptor;

import jrouter.annotation.InterceptorStack;

/**
 * 内置拦截栈。
 */
public class DefaultInterceptorStack {

    /**
     * 空拦截栈名称，不包含任何拦截器。
     */
    @InterceptorStack(interceptors = {})
    public static final String EMPTY_INTERCEPTOR_STACK = "empty";

    /**
     * 示例拦截栈名称，包含日志拦截器和计时拦截器。
     *
     * @see SampleInterceptor#logging
     * @see SampleInterceptor#timer
     */
    @InterceptorStack(interceptors = {SampleInterceptor.LOGGING, SampleInterceptor.TIMER})
    public static final String SAMPLE_INTERCEPTOR_STACK = "sample";

}
