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
import jrouter.JRouterException;
import static jrouter.impl.PathTreeTest.*;
import jrouter.interceptor.DemoThreadActionContextInterceptor;
import jrouter.result.DefaultResult;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试路径匹配的Action。
 */
public class PathActionFactoryTest {

    private PathActionFactory factory;

    @Before
    public void init() {
        PathActionFactory.Properties prop = new PathActionFactory.Properties();
        prop.setExtension("");
        prop.setDefaultInterceptorStack(DemoThreadActionContextInterceptor.DEMO_THREAD);
        prop.setDefaultResultType(DefaultResult.EMPTY);
        factory = new PathActionFactory(prop);

        //interceptor
        factory.addInterceptors(new DemoThreadActionContextInterceptor(false));

        //interceptor stack
        factory.addInterceptorStacks(DemoThreadActionContextInterceptor.class);

        //result
        factory.addResultTypes(DefaultResult.class);

        //path aciotn
        factory.addActions(jrouter.PathTestAction.class);

    }

    @After
    public void tearDown() {
        factory.clear();
    }

    /**
     * 测试Action调用。
     *
     * @see PathTreeTest#testGet
     */
    @Test
    public void test_invoke() {
        assertNotNull(factory);

        for (String p : PATHS) {
            assertEquals(p, factory.invokeAction(p));
        }

        assertEquals("/{k1}", factory.invokeAction("/zzz"));
        assertEquals("/{k1}", factory.invokeAction("/aa"));

        assertEquals("/aa/b3/*/d1", factory.invokeAction("/aa/b3/c1/d1"));
        assertEquals("/aa/b3/c1/d1/*", factory.invokeAction("/aa/b3/c1/d1/e1"));
        assertEquals("/aa/b3/c1/d1/*", factory.invokeAction("/aa/b3/c1/d1/null"));
        assertEquals("/aa/b3/*/d1/{k2}", factory.invokeAction("/aa/b3/null/d1/null"));

        assertEquals("/aa/*/c1/d1", factory.invokeAction("/aa/b2/c1/d1"));
        assertEquals("/aa/*/c1/d1/e1", factory.invokeAction("/aa/b1/c1/d1/e1"));
        assertEquals("/aa/*/c1/d1/e1", factory.invokeAction("/aa/b2/c1/d1/e1"));

        assertEquals("/aa/b4/{k1}/d1", factory.invokeAction("/aa/b4/c1/d1"));
        assertEquals("/aa/b4/{k1}/d2", factory.invokeAction("/aa/b4/c1/d2"));

        assertEquals("/aa/*/c1/d1/e1", factory.invokeAction("/aa/b4/c1/d1/e1"));
        assertEquals("/aa/b4/{k1}/d2/e1", factory.invokeAction("/aa/b4/c1/d2/e1"));
        assertEquals("/aa/b4/{k1}/d2/{k2}", factory.invokeAction("/aa/b4/c1/d2/null"));
        assertEquals("/aa/b4/{k1}/d2/{k2}", factory.invokeAction("/aa/b4/null/d2/null"));

        assertEquals("/aa/*/c1/d1/e1", factory.invokeAction("/aa/null/c1/d1/e1"));
    }

    /**
     * 测试Action调用，返回匹配参数。
     *
     * @see PathTreeTest#testTreePathParameters()
     */
    @Test
    public void test_getActionPathParameters() {
        Map<String, String> excepted = new HashMap<>();

        factory.invokeAction("/xx/yy/zz");
        assertNotNull(getActionPathParameters());
        factory.invokeAction("/aa/b1/c1/d0");
        assertNotNull(getActionPathParameters());
        factory.invokeAction("/aa/b1/c1/d1");
        assertNotNull(getActionPathParameters());

        try {
            factory.invokeAction("/aa/zzz/c1/d1/e1/null/null/null");
            fail("no exception");
        } catch (JRouterException e) {
            //no such Action
            assertNotNull(e);
        }

        excepted.put("k1", "zzz");
        //"/*"
        assertPathParameters(excepted, "/zzz");

        excepted.put("k1", "zzzzz");
        assertPathParameters(excepted, "/zzzzz");

        //"/aa/b3/*/d1"
        excepted.put("*", "c1");
        assertPathParameters(excepted, "/aa/b3/c1/d1");
        excepted.put("*", "null");
        assertPathParameters(excepted, "/aa/b3/null/d1");

        //"/aa/b3/*/d1/{k2}"
        excepted.put("*", "null1");
        excepted.put("k2", "null2");
        assertPathParameters(excepted, "/aa/b3/null1/d1/null2");

        //"/aa/b4/c1/d2/null"
        excepted.put("k1", "c1");
        excepted.put("k2", "null");
        assertPathParameters(excepted, "/aa/b4/c1/d2/null");

        //"/aa/b5/*/*/*/*"
        excepted.put("*", "null1");
        excepted.put("*2", "null2");
        excepted.put("*3", "null3");
        excepted.put("*4", "null4");
        assertPathParameters(excepted, "/aa/b5/null1/null2/null3/null4");

    }

    /**
     * 从线程变量中返回Action路径匹配的键值映射。
     *
     * @return 返回Action路径匹配的键值映射。
     */
    private Map<String, String> getActionPathParameters() {
        return ((PathActionInvocation) DemoThreadActionContextInterceptor.get()).getActionPathParameters();
    }

    /**
     * 测试期望的键值映射是否与调用Action路径后的路径匹配的键值映射一致。
     *
     * @param excepted 期望的键值映射。
     * @param path Action路径。
     */
    private void assertPathParameters(Map<String, String> excepted, String path) {
        factory.invokeAction(path);
        Map<String, String> actual = getActionPathParameters();
        assertEquals(excepted, actual);
        //clear the excepted map at last
        excepted.clear();
    }
}
