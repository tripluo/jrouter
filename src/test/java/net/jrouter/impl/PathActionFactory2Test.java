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

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import static net.jrouter.impl.PathTreeTest.PATHS;
import net.jrouter.result.DefaultResult;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试路径匹配的Action。
 */
@Slf4j
public class PathActionFactory2Test {

    private PathActionFactory factory;

    @Before
    public void init() {
        PathActionFactory.Properties prop = new PathActionFactory.Properties();
        prop.setExtension("");
        prop.setDefaultResultType(DefaultResult.EMPTY);
        prop.setPathGenerator(new PathActionFactory.StringPathGenerator() {

            @Override
            protected String buildActionPath(String namespace, String aname, Method method) {
                try {
                    //提供定制化
                    namespace = String.format(namespace, "test");
                } catch (Exception e) {
                    log.error("Exception occurs when building action's path.", e);
                }
                return super.buildActionPath(namespace, aname, method);
            }
        });
        factory = new PathActionFactory(prop);
        assertEquals(factory, prop.getActionFactory());

        //result
        factory.addResultTypes(DefaultResult.class);

        //path action
        factory.addActions(net.jrouter.PathTestAction2.class);

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
        String prefix = "test";

        for (String p : PATHS) {
            assertEquals(p, factory.invokeAction(String.format("%s/%s", prefix, p)));
        }

        assertEquals("/{k1}", factory.invokeAction(String.format("%s/%s", prefix, "/zzz")));
        assertEquals("/{k1}", factory.invokeAction(String.format("%s/%s", prefix, "/aa")));

        assertEquals("/aa/b3/*/d1", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b3/c1/d1")));
        assertEquals("/aa/b3/c1/d1/*", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b3/c1/d1/e1")));
        assertEquals("/aa/b3/c1/d1/*", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b3/c1/d1/null")));
        assertEquals("/aa/b3/*/d1/{k2}", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b3/null/d1/null")));

        assertEquals("/aa/*/c1/d1", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b2/c1/d1")));
        assertEquals("/aa/*/c1/d1/e1", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b1/c1/d1/e1")));
        assertEquals("/aa/*/c1/d1/e1", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b2/c1/d1/e1")));

        assertEquals("/aa/b4/{k1}/d1", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b4/c1/d1")));
        assertEquals("/aa/b4/{k1}/d2", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b4/c1/d2")));

        assertEquals("/aa/*/c1/d1/e1", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b4/c1/d1/e1")));
        assertEquals("/aa/b4/{k1}/d2/e1", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b4/c1/d2/e1")));
        assertEquals("/aa/b4/{k1}/d2/{k2}", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b4/c1/d2/null")));
        assertEquals("/aa/b4/{k1}/d2/{k2}", factory.invokeAction(String.format("%s/%s", prefix, "/aa/b4/null/d2/null")));

        assertEquals("/aa/*/c1/d1/e1", factory.invokeAction(String.format("%s/%s", prefix, "/aa/null/c1/d1/e1")));
    }
}
