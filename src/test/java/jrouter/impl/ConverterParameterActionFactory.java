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

import jrouter.ActionInvocation;
import jrouter.support.ActionInvocationDelegate;

/**
 * 测试ActionFactory的ConverterFactory传递参数的扩展性。
 */
public class ConverterParameterActionFactory extends PathActionFactory {

    public ConverterParameterActionFactory(Properties properties) {
        super(properties);
    }

    @Override
    protected ActionInvocation createActionInvocation(String path, Object... params) {
        ActionInvocation<String> ai = super.createActionInvocation(path, params);
        DemoActionInvocation invocation = new DefaultDemoActionInvocation("demo", ai);
        //重设调用ActionInvocation的参数转换器
        ai.setParameterConverter(invocation.getActionFactory().getConverterFactory().getParameterConverter(invocation));
        //重设调用ActionInvocation的转换参数
        invocation.setConvertParameters(new Object[]{invocation});
        return invocation;
    }

    /**
     * 自定义扩展ActionInvocation接口。
     */
    public static interface DemoActionInvocation extends ActionInvocation<String> {

        String getName();
    }

    /**
     * 代理ActionInvocation，并添加自定义的属性和实现接口。
     */
    public static class DefaultDemoActionInvocation extends ActionInvocationDelegate<String> implements
            DemoActionInvocation {

        /* name */
        private final String name;

        public DefaultDemoActionInvocation(String name, ActionInvocation<String> invocation) {
            super();
            this.name = name;
            this.delegate = invocation;
        }

        @Override
        public String getName() {
            return name;
        }

    }
}
