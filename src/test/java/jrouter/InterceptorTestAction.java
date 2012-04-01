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

import jrouter.annotation.Action;
import jrouter.annotation.Namespace;
import jrouter.interceptor.DefaultInterceptorStack;
import jrouter.interceptor.SampleInterceptor;

/**
 * 测试Namespace和Action上的拦截器集合。
 *
 * @see jrouter.impl.ActionFactory4Test
 */
public class InterceptorTestAction {

    /**
     * Namespace, default interceptorStack。
     */
    @Namespace(name = "/test1")
    public static class Action1 {

        /**
         * no interceptor。
         */
        @Action(name = "1")
        public void test1() {
        }

        /**
         * interceptorStack。
         */
        @Action(name = "2", interceptorStack = DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK)
        public void test2() {
        }

        @Action(name = "3", interceptors = {SampleInterceptor.TIMER})
        public void test3() {
        }
    }

    /**
     * Namespace with interceptorStack。
     */
    @Namespace(name = "/test2", interceptorStack = DefaultInterceptorStack.EMPTY_INTERCEPTOR_STACK)
    public static class Action2 {

        /**
         * no interceptor。
         */
        @Action(name = "1")
        public void test1() {
        }

        /**
         * interceptorStack。
         */
        @Action(name = "2", interceptorStack = DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK)
        public void test2() {
        }

        @Action(name = "3", interceptors = {SampleInterceptor.TIMER})
        public void test3() {
        }
    }

    /**
     * Namespace with interceptors。
     */
    @Namespace(name = "/test3", interceptors = {SampleInterceptor.LOGGING})
    public static class Action3 {

        /**
         * no interceptor。
         */
        @Action(name = "1")
        public void test1() {
        }

        /**
         * interceptorStack。
         */
        @Action(name = "2", interceptorStack = DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK)
        public void test2() {
        }

        @Action(name = "3", interceptors = {SampleInterceptor.TIMER})
        public void test3() {
        }
    }
}
