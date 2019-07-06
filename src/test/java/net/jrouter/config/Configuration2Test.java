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

package net.jrouter.config;

import net.jrouter.impl.MultiParameterConverterFactory;
import net.jrouter.impl.PathActionFactory;
import net.jrouter.interceptor.DefaultInterceptorStack;
import net.jrouter.interceptor.DemoInterceptor;
import net.jrouter.interceptor.SampleInterceptor;
import net.jrouter.result.DefaultResult;
import net.jrouter.result.DemoResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Configuration2Test。
 */
public class Configuration2Test extends Assert {

    private Configuration config = null;

    private PathActionFactory.ColonString factory;

    @After
    public void tearDown() {
        if (factory != null) {
            factory.clear();
        }
    }

    /**
     * 测试加载配置文件。
     */
    @Test
    public void test_load() {
        config = new Configuration();
        factory = config.load("/jrouter_test.xml").buildActionFactory();
        assertNotNull(factory);
        assertEquals("empty", factory.getDefaultInterceptorStack());
        assertEquals("empty", factory.getDefaultResultType());
        assertEquals("empty", factory.getDefaultStringResultType());
        assertEquals(100000, factory.getActionCacheNumber());
        assertEquals(".", factory.getExtension());
        assertEquals('/', factory.getPathSeparator());

        assertEquals(MultiParameterConverterFactory.NoFixedOrder.class, factory.getConverterFactory().getClass());

        assertNotNull(factory.getInterceptors().get(SampleInterceptor.LOGGING));
        assertNotNull(factory.getInterceptors().get(SampleInterceptor.TIMER));
        assertNotNull(factory.getInterceptors().get(DemoInterceptor.SPRING_DEMO));

        assertNotNull(factory.getInterceptorStacks().get(DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK));
        assertNotNull(factory.getInterceptorStacks().get(DemoInterceptor.DEMO));

        assertNotNull(factory.getResultTypes().get(DefaultResult.EMPTY));
        assertNotNull(factory.getResultTypes().get(DefaultResult.FORWARD));
        assertNotNull(factory.getResultTypes().get(DemoResult.DEMO_RESULT_TYPE));

        assertNotNull(factory.getResults().get(DefaultResult.RESULT_NOT_FOUND));
        assertNotNull(factory.getResults().get(DemoResult.DEMO_RESULT_NOT_FOUND));
        assertNotNull(factory.getResults().get(DemoResult.DEMO_RESULT_EXCEPTION));

    }

    /**
     * 测试循环引用异常。
     */
    @Test(expected = ConfigurationException.class)
    public void test_loadError() {
        config = new Configuration();
        config.load("/jrouter_error.xml");
    }
}
