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
package net.jrouter.interceptor;

import net.jrouter.ActionInvocation;
import net.jrouter.annotation.Interceptor;
import net.jrouter.annotation.InterceptorStack;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * DemoInterceptor。
 */
public class DemoInterceptor {

    /** demo interceptor & interceptor stack */
    @InterceptorStack(interceptors = {"demo"})
    public static final String DEMO = "demo";

    /** spring inject interceptor */
    public static final String SPRING_DEMO = "springInject";

    //注入的属性
    private String value;

    //注入的属性
    private Integer number;

    @Autowired
    @Qualifier("springInject")
    private String inject;

    /**
     * 测试拦截器。
     */
    @Interceptor(name = DEMO)
    public Object test(ActionInvocation invocation) {
        Assert.assertEquals("demo interceptor", value);
        Assert.assertEquals((Integer) 10000, number);
        return invocation.invoke();
    }

    /**
     * 测试拦截器的springframework注入。
     */
    @Interceptor(name = SPRING_DEMO)
    public Object springTest(ActionInvocation invocation) {
        Assert.assertEquals("spring inject", inject);
        return invocation.invoke();
    }

    /**
     * 注入属性。
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 注入属性。
     */
    public void setNumber(Integer number) {
        this.number = number;
    }
}
