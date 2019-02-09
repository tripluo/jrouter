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

import java.util.List;
import net.jrouter.impl.InterceptorProxy;
import net.jrouter.impl.PathActionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * ConfigurationAopTest。
 */
public class ConfigurationAopTest extends Assert {

    private final Configuration config = new Configuration().load("/jrouter_aop.xml");

    private final PathActionFactory factory = config.buildActionFactory();

    @After
    public void tearDown() {
        factory.clear();
    }

    /**
     * 测试action aop。
     */
    @Test
    public void test_aopAction() {
        assertNotNull(factory);

        assertInterceptorProxies("/test/param", "[timer, timer, springInject, logging]");

        assertInterceptorProxies("/test/simple", "[demo, springInject]");
        assertInterceptorProxies("/test/exception", "[logging, timer, demo, springInject]");
        assertInterceptorProxies("/test/springInject", "[springInject, demo, springInject]");
        assertInterceptorProxies("/test/forward", "[demo, springInject]");
        assertInterceptorProxies("/test/forward2", "[demo, springInject]");

        assertInterceptorProxies("/test1", "[]");
        assertInterceptorProxies("/xx/yy/zz", "[]");
        assertInterceptorProxies("/{k1}", "[]");
        assertInterceptorProxies("/aa/b5/*/*/*/*", "[]");
    }

    /**
     * 测试指定path的action的拦截器集合。
     */
    private void assertInterceptorProxies(String actionPath, String interceptorProxies) {
        assertEquals(interceptorsToString(factory.getActions().get(actionPath).getInterceptorProxies()), interceptorProxies);
    }

    /**
     * 拦截器集合字符串显示名称。
     */
    private String interceptorsToString(List<InterceptorProxy> interceptors) {
        if (interceptors == null)
            return "null";
        int iMax = interceptors.size() - 1;
        if (iMax == -1)
            return "[]";
        StringBuilder msg = new StringBuilder();
        msg.append('[');
        for (int i = 0; ; i++) {
            msg.append(interceptors.get(i).getName());
            if (i == iMax)
                return msg.append(']').toString();
            msg.append(", ");
        }
    }
}
