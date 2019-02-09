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

import net.jrouter.ActionInvocation;
import net.jrouter.annotation.ResultType;
import net.jrouter.impl.PathActionFactory.ColonString;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试PathActionFactory.ColonString正确性。
 */
public class PathActionFactory1Test {

    private PathActionFactory.ColonString factory;

    //public for javassist
    public static class TestResult {

        private final String prefix;

        /** 结果类型名称 */
        private static final String DEMO = "demo";

        public TestResult(String prefix) {
            this.prefix = prefix;
        }

        @ResultType(type = DEMO)
        public Object result(ActionInvocation invocation) {
            return prefix + invocation.getInvokeResult();
        }
    }

    @Before
    public void init() {
        ColonString.Properties properties = new ColonString.Properties();
        properties.setExtension("");
        properties.setDefaultResultType(TestResult.DEMO);
        factory = new ColonString(properties);

        //path aciotn
        factory.addActions(net.jrouter.URLTestAction.class);
    }

    /**
     * 测试Action路径的正确性。
     *
     * @see net.jrouter.URLTestAction
     */
    @Test
    public void test_invoke() {
        String prefix = "prefix:";
        //result
        factory.addResultTypes(new TestResult(prefix));
        assertEquals(prefix + "/test100", factory.invokeAction("/test100"));
        assertEquals(prefix + "/test101", factory.invokeAction("/test101"));
        assertEquals(prefix + "/test102", factory.invokeAction("/test102"));
        assertEquals(prefix + "/test103", factory.invokeAction("/test103"));

        assertEquals(prefix + "/", factory.invokeAction("/"));
        assertEquals(prefix + "/a", factory.invokeAction("/a"));
        assertEquals(prefix + "/b", factory.invokeAction("/b"));
        assertEquals(prefix + "/c", factory.invokeAction("/c"));
        assertEquals(prefix + "/d", factory.invokeAction("/d"));
        assertEquals(prefix + "/e", factory.invokeAction("/e"));

        assertEquals(prefix + "/test", factory.invokeAction("/test"));
        assertEquals(prefix + "/test1", factory.invokeAction("/test1"));
        assertEquals(prefix + "/test2", factory.invokeAction("/test2"));
        assertEquals(prefix + "/test3", factory.invokeAction("/test3"));
        assertEquals(prefix + "/test4", factory.invokeAction("/test4"));

        assertEquals(prefix + "/test/abc", factory.invokeAction("/test/abc"));
        assertEquals(prefix + "/test1/abc", factory.invokeAction("/test1/abc"));
        assertEquals(prefix + "/test2/abc", factory.invokeAction("/test2/abc"));
        assertEquals(prefix + "/test3/abc", factory.invokeAction("/test3/abc"));
        assertEquals(prefix + "/test4/abc", factory.invokeAction("/test4/abc"));
    }

    @After
    public void tearDown() {
        factory.clear();
    }
}
