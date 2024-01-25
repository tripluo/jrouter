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

import net.jrouter.JRouterException;
import net.jrouter.TestDuplicate;
import net.jrouter.interceptor.DefaultInterceptorStack;
import net.jrouter.interceptor.DemoInterceptor;
import net.jrouter.interceptor.SampleInterceptor;
import net.jrouter.result.DefaultResult;
import net.jrouter.result.DemoResult;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试无后缀的Action路径映射的正确性。
 */
public class ActionFactory2Test {

    private PathActionFactory factory;

    @Before
    public void init() {
        factory = new PathActionFactory.ColonString(new PathActionFactory.ColonString.Properties());

        // interceptor
        factory.addInterceptors(SampleInterceptor.class);

        // interceptor stack
        factory.addInterceptorStacks(DefaultInterceptorStack.class);

        // result
        factory.addResultTypes(DefaultResult.class);

        // action
        // class
        factory.addActions(net.jrouter.URLTestAction.class);
        // object
        factory.addActions(new net.jrouter.URLTestAction2());
    }

    @After
    public void tearDown() {
        factory.clear();
    }

    @Test
    public void test_factory() {
        assertSame(null, factory.getDefaultInterceptorStack());
        assertSame(null, factory.getDefaultResultType());
        assertNotNull(factory.getInterceptors().get(SampleInterceptor.LOGGING));
        assertNotNull(factory.getInterceptors().get(SampleInterceptor.TIMER));
    }

    /**
     * 测试Action路径的正确性。
     *
     * @see net.jrouter.URLTestAction
     */
    @Test
    public void testURL() {
        try {
            factory.invokeAction("");
            fail("no exception");
        } catch (JRouterException e) {
            assertNotNull(e);
        }

        assertEquals("/test100", factory.invokeAction("/test100"));
        assertEquals("/test101", factory.invokeAction("/test101"));
        assertEquals("/test102", factory.invokeAction("/test102"));
        assertEquals("/test103", factory.invokeAction("/test103"));

        assertEquals("/", factory.invokeAction("/"));
        assertEquals("/a", factory.invokeAction("/a"));
        assertEquals("/b", factory.invokeAction("/b"));
        assertEquals("/c", factory.invokeAction("/c"));
        assertEquals("/d", factory.invokeAction("/d"));
        assertEquals("/e", factory.invokeAction("/e"));

        assertEquals("/test", factory.invokeAction("/test"));
        assertEquals("/test1", factory.invokeAction("/test1"));
        assertEquals("/test2", factory.invokeAction("/test2"));
        assertEquals("/test3", factory.invokeAction("/test3"));
        assertEquals("/test4", factory.invokeAction("/test4"));

        assertEquals("/test/abc", factory.invokeAction("/test/abc"));
        assertEquals("/test1/abc", factory.invokeAction("/test1/abc"));
        assertEquals("/test2/abc", factory.invokeAction("/test2/abc"));
        assertEquals("/test3/abc", factory.invokeAction("/test3/abc"));
        assertEquals("/test4/abc", factory.invokeAction("/test4/abc"));
    }

    /**
     * 测试Action路径的正确性。
     *
     * @see net.jrouter.URLTestAction2
     */
    @Test
    public void testURL2() {

        try {
            factory.invokeAction("");
            fail("no exception");
        } catch (JRouterException e) {
            assertNotNull(e);
        }
        assertEquals("/url/test100", factory.invokeAction("/url/test100"));
        assertEquals("/url/test101", factory.invokeAction("/url/test101"));

        assertEquals("/url/a2", factory.invokeAction("/url/a2"));
        assertEquals("/url/b2", factory.invokeAction("/url/b2"));
        assertEquals("/url/c2", factory.invokeAction("/url/c2"));
        assertEquals("/url_d2", factory.invokeAction("/url_d2"));
        assertEquals("/url_e2", factory.invokeAction("/url_e2"));

        assertEquals("/url/test", factory.invokeAction("/url/test"));
        assertEquals("/url_test1", factory.invokeAction("/url_test1"));
        assertEquals("/url/test2", factory.invokeAction("/url/test2"));
        assertEquals("/url_test3", factory.invokeAction("/url_test3"));
        assertEquals("/url_test4", factory.invokeAction("/url_test4"));

        assertEquals("/url/test/abc", factory.invokeAction("/url/test/abc"));
        assertEquals("/url_test1/abc", factory.invokeAction("/url_test1/abc"));
        assertEquals("/url_test2/abc", factory.invokeAction("/url_test2/abc"));
        assertEquals("/url_test3/abc", factory.invokeAction("/url_test3/abc"));
        assertEquals("/url_test4/abc", factory.invokeAction("/url_test4/abc"));
    }

    /**
     * 测试添加重复Action时抛出异常。
     */
    @Test
    public void test_duplicateError() {
        try {
            factory.addActions(TestDuplicate.DuplicateAction1.class);
            fail("no exception");
        } catch (JRouterException e) {
            assertTrue(e.getMessage().startsWith("Duplicate path Action "));
        }
        try {
            factory.addActions(TestDuplicate.DuplicateAction2.class);
            fail("no exception");
        } catch (JRouterException e) {
            assertTrue(e.getMessage().startsWith("Duplicate path Action "));
        }
        try {
            factory.addActions(TestDuplicate.DuplicateAction3.class);
            fail("no exception");
        } catch (JRouterException e) {
            assertTrue(e.getMessage().startsWith("Duplicate path Action "));
        }
////////////////////////////////////////////////////////////////////////////////
        factory.addInterceptors(DemoInterceptor.class);
        try {
            factory.addInterceptors(TestDuplicate.DuplicateInterceptor1.class);
            fail("no exception");
        } catch (JRouterException e) {
            assertTrue(e.getMessage().startsWith("Duplicate Interceptor "));
        }

        factory.addInterceptorStacks(DemoInterceptor.class);
        try {
            factory.addInterceptorStacks(TestDuplicate.DuplicateInterceptor1.class);
            fail("no exception");
        } catch (JRouterException e) {
            assertTrue(e.getMessage().startsWith("Duplicate InterceptorStack "));
        }
////////////////////////////////////////////////////////////////////////////////

        factory.addResultTypes(DemoResult.class);
        try {
            factory.addResultTypes(TestDuplicate.DuplicateResult1.class);
            fail("no exception");
        } catch (JRouterException e) {
            assertTrue(e.getMessage().startsWith("Duplicate ResultType "));
        }
        factory.addResults(DemoResult.class);
        try {
            factory.addResults(TestDuplicate.DuplicateResult1.class);
            fail("no exception");
        } catch (JRouterException e) {
            assertTrue(e.getMessage().startsWith("Duplicate Result "));
        }
    }
}
