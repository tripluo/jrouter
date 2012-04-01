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
package jrouter.config;

import jrouter.ActionFactory;
import jrouter.impl.DefaultActionFactory;
import jrouter.interceptor.DefaultInterceptorStack;
import jrouter.interceptor.DemoInterceptor;
import jrouter.interceptor.SampleInterceptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试自动扫描类并添加组件。
 */
public class ConfigurationScanComponentTest extends Assert {

    private Configuration config = new Configuration().load("/jrouter_autoscan.xml");

    private ActionFactory factory = config.buildActionFactory();

    @Before
    public void setUp() {
        assertNotNull(factory);
    }

    @After
    public void tearDown() {
        factory.clear();
    }

    /**
     * Test of scanning components。
     */
    @Test
    public void test_scanComponent() {
        assertEquals("empty", factory.getDefaultInterceptorStack());
        assertEquals("empty", factory.getDefaultResultType());

        assertSame(DefaultActionFactory.class, factory.getClass());

        assertEquals(100000, ((DefaultActionFactory) factory).getActionCacheNumber());
        assertEquals(".", ((DefaultActionFactory) factory).getExtension());

        assertNotNull(factory.getInterceptorStacks().get(DefaultInterceptorStack.EMPTY_INTERCEPTOR_STACK));
        assertNotNull(factory.getInterceptorStacks().get(DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK));
        assertNotNull(factory.getInterceptorStacks().get(DemoInterceptor.DEMO));

        assertNotNull(factory.getInterceptors().get(SampleInterceptor.LOGGING));
        assertNotNull(factory.getInterceptors().get(SampleInterceptor.TIMER));
        assertNotNull(factory.getInterceptors().get(DemoInterceptor.SPRING_DEMO));
    }
}
