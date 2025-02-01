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

import net.jrouter.ActionFactory;
import net.jrouter.impl.PathActionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * ConfigurationTest。
 */
public class ConfigurationTest extends Assert {

    private Configuration config = null;

    private ActionFactory factory;

    @After
    public void tearDown() {
        factory.clear();
    }

    /**
     * 测试加载配置文件。
     */
    @Test
    public void test_load() {

        config = new Configuration();
        config.load();

        factory = config.buildActionFactory();

        assertNotNull(factory);
        assertSame(PathActionFactory.class, factory.getClass());
        assertNotNull(factory.getActions());
        assertNotNull(factory.getDefaultInterceptorStack());
        assertNotNull(factory.getDefaultResultType());
        assertNotNull(factory.getInterceptorStacks());
        assertNotNull(factory.getInterceptors());
        assertNotNull(factory.getResultTypes());
        assertNotNull(factory.getResults());
    }

}
