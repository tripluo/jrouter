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

import jrouter.ObjectFactory;
import jrouter.SimpleAction;
import jrouter.URLTestAction2;
import jrouter.impl.PathActionFactory;
import jrouter.interceptor.DemoInterceptor;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * SpringObjectFactoryTest。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:jrouter-spring_test.xml"})
public class SpringObjectFactoryTest extends AbstractJUnit4SpringContextTests {

    //PathActionFactory
    private PathActionFactory factory;

    //ObjectFactory
    private ObjectFactory objectFactory;

    @Before
    public void setUp() {
        //SpringObjectFactory
        objectFactory = new SpringObjectFactory(applicationContext);

        PathActionFactory.Properties prop = new PathActionFactory.Properties();
        prop.setObjectFactory(objectFactory);
        //create ActionFactory not by springframework
        factory = new PathActionFactory(prop);

        factory.addInterceptors(DemoInterceptor.class);
        factory.addInterceptorStacks(DemoInterceptor.class);

        factory.addActions(SimpleAction.class);

    }

    @After
    public void tearDown() {
        factory.clear();
    }

    /**
     * Test of newInstance method, of class SpringObjectFactory.
     */
    @Test
    public void testNewInstance() {
        SimpleAction sa1 = objectFactory.newInstance(SimpleAction.class);
        sa1.springInject();
        SimpleAction sa2 = objectFactory.newInstance(SimpleAction.class);
        sa2.springInject();
        assertNotSame(sa1, sa2);
        //注入的为单例bean对象
        assertSame(sa1.springInject(), sa2.springInject());
    }

    /**
     * 测试 springframework 注入。
     *
     * @see SimpleAction#springInject()
     */
    @Test
    public void testSpringInject() {
        String url = "/test/springInject";

        //check interceptors
        assertEquals(1, factory.getActions().get(url).getInterceptorProxies().size());
        assertEquals(1, factory.getActions().get(url).getInterceptors().size());
        assertEquals(DemoInterceptor.SPRING_DEMO, factory.getActions().get(url).getInterceptorProxies().get(0).getName());
        assertEquals(DemoInterceptor.SPRING_DEMO, factory.getActions().get(url).getInterceptors().get(0).name());

        assertTrue(factory.invokeAction(url) instanceof URLTestAction2);
        assertSame(factory.invokeAction(url), factory.invokeAction(url));
    }
}
