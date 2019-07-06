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

package net.jrouter;

import net.jrouter.annotation.*;
import net.jrouter.interceptor.DemoInterceptor;
import net.jrouter.result.DemoResult;

/**
 * 测试重复Action、Interceptor、InterceptorStack、ResultType、Result。
 */
public class TestDuplicate {

    public static class DuplicateAction1 {

        /**
         * @see URLTestAction#test104()
         */
        @Action(name = "//")
        public String test104_1() {
            return "/";
        }
    }

    public static class DuplicateAction2 {

        /**
         * @see URLTestAction#test104()
         */
        @Action(name = "//////a")
        public String test200_1() {
            return "/a";
        }
    }

    public static class DuplicateAction3 {

        /**
         * @see URLTestAction#test404()
         */
        @Action(name = "/test4/abc")
        public String test404_1() {
            return "/test4/abc";
        }
    }
////////////////////////////////////////////////////////////////////////////////

    public static class DuplicateInterceptor1 {

        @InterceptorStack(interceptors = {@InterceptorStack.Interceptor("demo")})
        public static final String DEMO = DemoInterceptor.DEMO;

        @Interceptor(name = DemoInterceptor.SPRING_DEMO)
        public Object test(ActionInvocation invocation) {
            return invocation.invoke();
        }
    }
////////////////////////////////////////////////////////////////////////////////

    public static class DuplicateResult1 {

        public static final String DEMO_RESULT_TYPE = DemoResult.DEMO_RESULT_TYPE;

        @ResultType(type = DEMO_RESULT_TYPE)
        public Object test(ActionInvocation invocation) {
            return invocation.getResult().location();
        }

        @Result(name = DemoResult.DEMO_RESULT_EXCEPTION)
        public static Object resultException(ActionInvocation invocation) {
            return null;
        }
    }
////////////////////////////////////////////////////////////////////////////////
}
