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

import java.util.HashMap;
import java.util.Map;
import jrouter.InterceptorTestAction;
import jrouter.interceptor.DefaultInterceptorStack;
import jrouter.interceptor.SampleInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试Namespace和Action上的拦截器集合。
 */
public class ActionFactory4Test {

    private DefaultActionFactory factory;

    @Before
    public void init() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("defaultInterceptorStack", DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK);

        factory = new DefaultActionFactory(props);

        assertEquals(DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK, factory.getDefaultInterceptorStack());

        factory.addInterceptors(SampleInterceptor.class);
        factory.addInterceptorStacks(DefaultInterceptorStack.class);

        factory.addActions(InterceptorTestAction.Action1.class);
        factory.addActions(InterceptorTestAction.Action2.class);
        factory.addActions(InterceptorTestAction.Action3.class);

    }

    @After
    public void tearDown() {
        factory.clear();
    }

    /**
     * 测试Action上的拦截器集合。
     */
    @Test
    public void testActionInterceptors() {
        DefaultActionProxy ap11 = factory.getActions().get("/test1/1");
        DefaultActionProxy ap12 = factory.getActions().get("/test1/2");
        DefaultActionProxy ap13 = factory.getActions().get("/test1/3");

        assertEquals(ap11.getInterceptorProxies(), factory.getInterceptorStacks().get(factory.getDefaultInterceptorStack()).getInterceptors());
        assertEquals(ap12.getInterceptorProxies(), factory.getInterceptorStacks().get(DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK).getInterceptors());
        assertEquals(1, ap13.getInterceptorProxies().size());
        assertEquals(ap13.getInterceptorProxies().get(0).getName(), SampleInterceptor.TIMER);

        DefaultActionProxy ap21 = factory.getActions().get("/test2/1");
        DefaultActionProxy ap22 = factory.getActions().get("/test2/2");
        DefaultActionProxy ap23 = factory.getActions().get("/test2/3");

        assertEquals(0, ap21.getInterceptorProxies().size());
        assertEquals(ap21.getInterceptorProxies(), factory.getInterceptorStacks().get(DefaultInterceptorStack.EMPTY_INTERCEPTOR_STACK).getInterceptors());
        assertEquals(ap22.getInterceptorProxies(), factory.getInterceptorStacks().get(DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK).getInterceptors());
        assertEquals(1, ap23.getInterceptorProxies().size());
        assertEquals(ap23.getInterceptorProxies().get(0).getName(), SampleInterceptor.TIMER);


        DefaultActionProxy ap31 = factory.getActions().get("/test3/1");
        DefaultActionProxy ap32 = factory.getActions().get("/test3/2");
        DefaultActionProxy ap33 = factory.getActions().get("/test3/3");

        assertEquals(1, ap31.getInterceptorProxies().size());
        assertEquals(ap31.getInterceptorProxies().get(0).getName(), SampleInterceptor.LOGGING);
        assertEquals(ap32.getInterceptorProxies(), factory.getInterceptorStacks().get(DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK).getInterceptors());
        assertEquals(1, ap33.getInterceptorProxies().size());
        assertEquals(ap33.getInterceptorProxies().get(0).getName(), SampleInterceptor.TIMER);
    }
}
