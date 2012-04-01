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
package jrouter.spring;

import jrouter.ActionFactory;
import jrouter.impl.DefaultActionFactory;
import jrouter.interceptor.DefaultInterceptorStack;
import jrouter.interceptor.DemoInterceptor;
import jrouter.interceptor.SampleInterceptor;
import jrouter.result.DefaultResult;
import jrouter.result.DemoResult;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 测试与 springframework 集成自动扫描类并添加组件。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:jrouter-spring_autoscan.xml"})
public class DefaultActionFactoryBeanScanComponentTest {

    //singleton ActionFactory
    @Autowired
    @Qualifier("actionFactoryTest")
    private ActionFactory factory;

    @Before
    public void setUp() {
        assertNotNull(factory);
    }

    /**
     * Test of scanning components。
     */
    @Test
    public void test_scanComponent() {

        assertSame(DefaultActionFactory.class, factory.getClass());
        assertSame(SpringObjectFactory.class, factory.getObjectFactory().getClass());

        assertEquals("empty", factory.getDefaultInterceptorStack());
        assertEquals("empty", factory.getDefaultResultType());
        assertEquals(100000, ((DefaultActionFactory) factory).getActionCacheNumber());
        assertEquals(".", ((DefaultActionFactory) factory).getExtension());

        assertNotNull(factory.getInterceptors().get(SampleInterceptor.LOGGING));
        assertNotNull(factory.getInterceptors().get(SampleInterceptor.TIMER));
        assertNotNull(factory.getInterceptors().get(DemoInterceptor.SPRING_DEMO));

        assertNotNull(factory.getInterceptorStacks().get(DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK));
        assertNotNull(factory.getInterceptorStacks().get(DemoInterceptor.DEMO));

        assertNotNull(factory.getResultTypes().get(DefaultResult.EMPTY));
        assertNotNull(factory.getResultTypes().get(DefaultResult.FORWARD));

        //exculde jrouter.result
        assertNull(factory.getResultTypes().get(DemoResult.DEMO_RESULT_TYPE));
        assertNull(factory.getResults().get(DefaultResult.RESULT_NOT_FOUND));
        assertNull(factory.getResults().get(DemoResult.DEMO_RESULT_NOT_FOUND));
        assertNull(factory.getResults().get(DemoResult.DEMO_RESULT_EXCEPTION));

    }
}