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
package net.jrouter;

import java.util.Arrays;
import net.jrouter.annotation.*;
import net.jrouter.impl.PathActionInvocation;
import net.jrouter.interceptor.DefaultInterceptorStack;
import net.jrouter.interceptor.DemoInterceptor;
import net.jrouter.interceptor.SampleInterceptor;
import net.jrouter.result.DefaultResult;
import net.jrouter.result.DemoResult;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SimpleAction。
 *
 * @see net.jrouter.impl.ActionFactoryTest
 * @see net.jrouter.spring.SpringObjectFactoryTest
 */
@Component
@Namespace(name = "/test", autoIncluded = true)
public class SimpleAction {

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(SimpleAction.class);

    /**
     * 表示Action执行成功
     */
    public static final String SUCCESS = "success";

    /**
     * 测试与 springframework 的集成注入。
     */
    @Autowired
    private URLTestAction2 URLTestAction2;

    /**
     * 测试与 springframework 的集成注入。
     *
     * @return 由springframework注入的bean对象。
     */
    @Action(interceptors = {DemoInterceptor.SPRING_DEMO})
    public URLTestAction2 springInject() {
        Assert.assertNotNull(URLTestAction2);
        return URLTestAction2;
    }

    /**
     * 测试简单调用。
     *
     * @return Action执行成功。
     */
    @Action(name = "simple", interceptorStack = DefaultInterceptorStack.EMPTY_INTERCEPTOR_STACK)
    protected String simple() {
        return SUCCESS;
    }

    /**
     * 测试简单调用。
     *
     * @return Action执行成功。
     */
    @Action(name = "simple2", interceptorStack = DefaultInterceptorStack.EMPTY_INTERCEPTOR_STACK)
    protected String simple2(String str) {
        return SUCCESS;
    }

    /**
     * 测试Action的初始属性。
     */
    @Action(parameters = {
            @Parameter(name = "test1", value = "value1"),
            @Parameter(name = "test2", value = {"value2"}),
            @Parameter(name = "test3", value = {"value3", "value33"})
    })
    public String actionParameters() {
        return null;
    }

    /**
     * 带参数调用。
     */
    @Action(name = "param", interceptors = {SampleInterceptor.LOGGING}, scope = Scope.PROTOTYPE)
    public String param(String str, int n) {
        return str + n;
    }

    /**
     * 带参数调用。
     */
    @Action
    public String singleVarArgsArray(String... params) {
        return Arrays.toString(params);
    }

    /**
     * 带参数调用。
     */
    @Action
    public String varArgsArray(Integer number, String... params) {
        return number + Arrays.toString(params);
    }

    /**
     * 带参数调用。
     */
    @Action
    public String varArgsArray2(Integer number, Integer number2, String... params) {
        return "" + number + number2 + Arrays.toString(params);
    }

    /**
     * 测试调用时抛出异常。
     */
    @Action(name = "exception", interceptorStack = DefaultInterceptorStack.SAMPLE_INTERCEPTOR_STACK)
    public void exception() {

        //throw exception
        System.out.println(1 / 0);
    }

    /**
     * 测试forward结果类型。
     *
     * @return 跳转至/test/simple的结果。
     */
    @Action(results = {
            @Result(name = "forward", type = DefaultResult.FORWARD, location = "/test/simple")
    })
    public String forward() {
        return "forward";
    }

    /**
     * 二次forward调用。
     *
     * @return 跳转至/test/forward -> /test/simple的结果。
     */
    @Action(results = {
            @Result(name = "forward2", type = DefaultResult.FORWARD, location = "/test/forward")
    })
    public String forward2() {
        return "forward2";
    }

    /**
     * 提供基于返回单表达式字符串的结果视图。
     * <p>
     * 例如： forward : /test/forward redirect: /test jsp: /index.jsp ......
     *
     * @param type 测试输入的返回类型值。
     *
     * @return 结果视图的表达式字符串。
     */
    @Action(results = {
            @Result(name = "test", type = DefaultResult.FORWARD, location = "/test/simple2"),
            @Result(name = "*")
    })
    public String autoRender(String type) {
        return type;
    }

    //测试注入的字符串
    private String string;

    //测试注入的数字
    private int number;

    /**
     * 测试Action的注入属性。
     */
    @Action(interceptorStack = DemoInterceptor.DEMO,
            results = {
                    @Result(name = "*", type = DemoResult.DEMO_RESULT_TYPE)
            })
    public String inject() {
        Assert.assertEquals("admin", string);
        Assert.assertEquals(100, number);
        return ":" + string + number;
    }

    /**
     * 测试Action注入的不同属性。
     */
    @Action(scope = Scope.PROTOTYPE)
    public String inject2() {
        Assert.assertEquals("admin", string);
        Assert.assertEquals(200, number);
        return string + number;
    }

    /**
     * 测试全局结果对象。
     */
    @Action
    public String resultNotFound(String res) {
        return res;
    }

    /**
     * 测试autoIncluded。
     *
     * @see Namespace#autoIncluded()
     */
    public long autoIncluded() {
        return 1;
    }

    /**
     * 测试LastPadParameter。
     *
     * @see net.jrouter.impl.MultiParameterConverterFactory
     */
    @Action
    public String lastPadParameter(ActionInvocation<String> invocation) {
        Assert.assertNotNull(invocation);
        return invocation.getActionPath();
    }

    /**
     * 测试LastPadParameter。
     *
     * @see net.jrouter.impl.MultiParameterConverterFactory
     */
    @Action
    public String lastPadParameter2(String test, PathActionInvocation invocation) {
        Assert.assertNotNull(invocation);
        return test + invocation.getActionPath();
    }

    /**
     * 测试@Ignore。
     */
    @Action
    @Ignore
    public String ignore() {
        return "ignore";
    }

    /**
     * 注入属性。
     */
    @Ignore
    public void setNumber(int number) {
        LOG.info("Set number \"{}\" in {}", number, this);
        this.number = number;
    }

    /**
     * 注入属性。
     */
    @Ignore
    public void setString(String string) {
        LOG.info("Set string [{}] in {}", string, this);
        this.string = string;
    }

    /**
     * 测试ActionFactory的actionFilter属性。
     */
    @RequestMapping
    public String actionFilter(String string) {
        LOG.info("In actionFilter [{}] in {}", string, this);
        return string;
    }

    /**
     * 测试ActionFactory的actionFilter属性。
     */
    @RequestMapping(path = {"/actionFilter2"})
    public String actionFilter2(ActionInvocation<String> invocation) {
        LOG.info("In actionFilter [{}] in {}", string, this);
        return invocation.getActionPath();
    }
}
