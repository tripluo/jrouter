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

import jrouter.JRouterException;
import jrouter.interceptor.DefaultInterceptorStack;
import jrouter.interceptor.SampleInterceptor;
import jrouter.result.DefaultResult;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试无后缀的Action路径映射的正确性。
 */
public class ActionFactory2Test {

    private DefaultActionFactory factory;

    @Before
    public void init() {
        factory = new DefaultActionFactory();

        //interceptor
        factory.addInterceptors(SampleInterceptor.class);

        //interceptor stack
        factory.addInterceptorStacks(DefaultInterceptorStack.class);

        //result
        factory.addResultTypes(DefaultResult.class);

        //aciotn
        //class
        factory.addActions(jrouter.URLTestAction.class);
        //object
        factory.addActions(new jrouter.URLTestAction2());
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
     * @see jrouter.URLTestAction
     */
    @Test
    public void testURL() {

        try {
            factory.invokeAction("");
        } catch (JRouterException e) {
            assertNotNull(e);
        }

        assertEquals("/", factory.invokeAction("/test100"));
        assertEquals("/", factory.invokeAction("/test101"));
        assertEquals("/", factory.invokeAction("/test102"));
        assertEquals("/", factory.invokeAction("/test103"));

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
     * @see jrouter.URLTestAction2
     */
    @Test
    public void testURL2() {

        try {
            factory.invokeAction("");
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
}
