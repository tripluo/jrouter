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
package net.jrouter.spring;

import net.jrouter.ActionFactory;
import net.jrouter.JRouterException;
import net.jrouter.impl.PathActionFactory;
import net.jrouter.interceptor.DefaultInterceptorStack;
import net.jrouter.interceptor.DemoInterceptor;
import net.jrouter.interceptor.SampleInterceptor;
import net.jrouter.result.DefaultResult;
import net.jrouter.result.DemoResult;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * DefaultActionFactoryBeanTest。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:jrouter-spring_test.xml"})
public class DefaultActionFactoryBeanTest {

    //singleton ActionFactory
    @Autowired
    @Qualifier("actionFactoryTest")
    private ActionFactory factory;

    @Before
    public void setUp() {
        assertNotNull(factory);
    }

    /**
     * Test of getActionFactory method, of class DefaultActionFactoryBean.
     */
    @Test
    public void testGetActionFactory() {
        assertSame(PathActionFactory.class, factory.getClass());
        assertSame(SpringObjectFactory.class, factory.getObjectFactory().getClass());
        assertEquals("empty", factory.getDefaultInterceptorStack());
        assertEquals("empty", factory.getDefaultResultType());
        assertEquals(100000, ((PathActionFactory) factory).getActionCacheNumber());
        assertEquals("", ((PathActionFactory) factory).getExtension());
        assertEquals('/', ((PathActionFactory) factory).getPathSeparator());

        assertNotNull(factory.getInterceptors().get(SampleInterceptor.LOGGING));
        assertNotNull(factory.getInterceptors().get(SampleInterceptor.TIMER));
        assertNotNull(factory.getInterceptors().get(DemoInterceptor.SPRING_DEMO));

        assertNotNull(factory.getInterceptorStacks().get(DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK));
        assertNotNull(factory.getInterceptorStacks().get(DemoInterceptor.DEMO));

        assertNotNull(factory.getResultTypes().get(DefaultResult.EMPTY));
        assertNotNull(factory.getResultTypes().get(DefaultResult.FORWARD));
        assertNotNull(factory.getResultTypes().get(DemoResult.DEMO_RESULT_TYPE));

        assertNotNull(factory.getResults().get(DemoResult.DEMO_RESULT_NOT_FOUND));
        assertNotNull(factory.getResults().get(DemoResult.DEMO_RESULT_EXCEPTION));
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