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
import jrouter.annotation.Action;
import jrouter.impl.ConverterParameterActionFactory.DemoActionInvocation;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试ActionFactory的ConverterFactory传递参数的扩展性。
 */
public class ConverterParameterActionFactoryTest {

    private ConverterParameterActionFactory factory;

    @Before
    public void init() {
        Map<String, Object> props = new HashMap<>();
        //manually set converterFactory
        props.put("converterFactory", jrouter.impl.MultiParameterConverterFactory.class);
//        props.put("bytecode", "default");
        factory = new ConverterParameterActionFactory(props);
        //add test Action
        factory.addActions(ConverterParameterAction.class);
    }

    /**
     * 测试ConverterParameterActionFactory。
     *
     * @see jrouter.impl.ConverterParameterActionFactory.DemoActionInvocation
     */
    @Test
    public void test_invoke() {
        assertEquals("demo", factory.invokeAction("/simple"));
    }

    /**
     * ConverterParameterAction.
     */
    public static class ConverterParameterAction {

        @Action
        public String simple(DemoActionInvocation invocation) {
            return invocation.getName();
        }
    }
}
