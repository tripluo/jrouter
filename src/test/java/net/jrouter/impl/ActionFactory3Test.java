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
import net.jrouter.interceptor.DefaultInterceptorStack;
import net.jrouter.interceptor.SampleInterceptor;
import net.jrouter.result.DefaultResult;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试有后缀Action路径的映射的正确性。
 */
public class ActionFactory3Test {

    //factory1
    private PathActionFactory factory1;

    //factory2
    private PathActionFactory factory2;

    //extension for factory1
    private final String extension1 = ".do";

    //extension for factory2
    private final String extension2 = ".action";

    @Before
    public void init() {
        PathActionFactory.Properties prop = new PathActionFactory.Properties();
        prop.setExtension(".");
        factory1 = new PathActionFactory(prop);

        //interceptor
        factory1.addInterceptors(SampleInterceptor.class);

        //interceptor stack
        factory1.addInterceptorStacks(DefaultInterceptorStack.class);

        //result
        factory1.addResultTypes(DefaultResult.class);

        //aciotn
        factory1.addActions(net.jrouter.URLTestAction.class);

////////////////////////////////////////////////////////////////////////////////////////////////////
        PathActionFactory.Properties prop2 = new PathActionFactory.Properties();
        prop2.setExtension(extension2);
        factory2 = new PathActionFactory(prop2);

        //interceptor
        factory2.addInterceptors(SampleInterceptor.class);

        //interceptor stack
        factory2.addInterceptorStacks(DefaultInterceptorStack.class);

        //result
        factory2.addResultTypes(DefaultResult.class);

        //aciotn
        factory2.addActions(net.jrouter.URLTestAction2.class);
    }

    @After
    public void tearDown() {
        factory1.clear();
        factory2.clear();
    }

    /**
     * 测试Action路径的正确性。
     *
     * @see net.jrouter.URLTestAction
     */
    @Test
    public void testURL() {

        try {
            factory1.invokeAction("");
            fail("no exception");
        } catch (JRouterException e) {
            assertNotNull(e);
        }

        assertEquals("/test100", factory1.invokeAction("/test100" + extension1));
        assertEquals("/test101", factory1.invokeAction("/test101" + extension1));
        assertEquals("/test102", factory1.invokeAction("/test102" + extension1));
        assertEquals("/test103", factory1.invokeAction("/test103" + extension1));

        assertEquals("/", factory1.invokeAction("/"));
        assertEquals("/a", factory1.invokeAction("/a" + extension1));
        assertEquals("/b", factory1.invokeAction("/b" + extension1));
        assertEquals("/c", factory1.invokeAction("/c" + extension1));
        assertEquals("/d", factory1.invokeAction("/d" + extension1));
        assertEquals("/e", factory1.invokeAction("/e" + extension1));

        assertEquals("/test", factory1.invokeAction("/test" + extension1));
        assertEquals("/test1", factory1.invokeAction("/test1" + extension1));
        assertEquals("/test2", factory1.invokeAction("/test2" + extension1));
        assertEquals("/test3", factory1.invokeAction("/test3" + extension1));
        assertEquals("/test4", factory1.invokeAction("/test4" + extension1));

        assertEquals("/test/abc", factory1.invokeAction("/test/abc" + extension1));
        assertEquals("/test1/abc", factory1.invokeAction("/test1/abc" + extension1));
        assertEquals("/test2/abc", factory1.invokeAction("/test2/abc" + extension1));
        assertEquals("/test3/abc", factory1.invokeAction("/test3/abc" + extension1));
        assertEquals("/test4/abc", factory1.invokeAction("/test4/abc" + extension1));
    }

    /**
     * 测试Action路径的正确性。
     *
     * @see net.jrouter.URLTestAction2
     */
    @Test
    public void testURL2() {
        try {
            factory2.invokeAction("");
            fail("no exception");
        } catch (JRouterException e) {
            assertNotNull(e);
        }

        assertEquals("/url/test100", factory2.invokeAction("/url/test100" + extension2));
        assertEquals("/url/test101", factory2.invokeAction("/url/test101" + extension2));

        assertEquals("/url/a2", factory2.invokeAction("/url/a2" + extension2));
        assertEquals("/url/b2", factory2.invokeAction("/url/b2" + extension2));
        assertEquals("/url/c2", factory2.invokeAction("/url/c2" + extension2));
        assertEquals("/url_d2", factory2.invokeAction("/url_d2" + extension2));
        assertEquals("/url_e2", factory2.invokeAction("/url_e2" + extension2));

        assertEquals("/url/test", factory2.invokeAction("/url/test" + extension2));
        assertEquals("/url_test1", factory2.invokeAction("/url_test1" + extension2));
        assertEquals("/url/test2", factory2.invokeAction("/url/test2" + extension2));
        assertEquals("/url_test3", factory2.invokeAction("/url_test3" + extension2));
        assertEquals("/url_test4", factory2.invokeAction("/url_test4" + extension2));

        assertEquals("/url/test/abc", factory2.invokeAction("/url/test/abc" + extension2));
        assertEquals("/url_test1/abc", factory2.invokeAction("/url_test1/abc" + extension2));
        assertEquals("/url_test2/abc", factory2.invokeAction("/url_test2/abc" + extension2));
        assertEquals("/url_test3/abc", factory2.invokeAction("/url_test3/abc" + extension2));
        assertEquals("/url_test4/abc", factory2.invokeAction("/url_test4/abc" + extension2));
    }
}
