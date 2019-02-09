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
package net.jrouter.result;

import net.jrouter.ActionInvocation;
import net.jrouter.annotation.Result;
import net.jrouter.annotation.ResultType;
import org.junit.Assert;

/**
 * DemoResult。
 */
public class DemoResult {

    /** demo */
    public static final String DEMO_RESULT_TYPE = "demo";

    /** result not found */
    public static final String DEMO_RESULT_NOT_FOUND = "demoResultNotFound";

    /** result exception */
    public static final String DEMO_RESULT_EXCEPTION = "demoResultException";

    //注入的属性
    private String value;

    /**
     * 返回结果对象的路径。
     *
     * @param invocation Action运行时上下文。
     */
    @ResultType(type = DEMO_RESULT_TYPE)
    public Object demo(ActionInvocation invocation) {
        Assert.assertEquals("demo result", value);
        return invocation.getResult().location();
    }

    /**
     * 返回结果未找到的字符串。
     *
     * @param invocation Action运行时上下文。
     */
    @Result(name = DEMO_RESULT_NOT_FOUND)
    public static Object resultNotFound(ActionInvocation invocation) {
        return DEMO_RESULT_NOT_FOUND + ":" + invocation.getActionPath();
    }

    /**
     * 抛出运行时异常。
     *
     * @param invocation Action运行时上下文。
     */
    @Result(name = DEMO_RESULT_EXCEPTION)
    public static Object resultException(ActionInvocation invocation) {
        throw new RuntimeException("Result excpetion : " + invocation.getActionPath());
    }

    /**
     * 注入属性。
     */
    public void setValue(String value) {
        this.value = value;
    }
}
