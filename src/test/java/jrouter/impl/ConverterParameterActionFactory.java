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

import java.util.Map;
import jrouter.ActionFactory;
import jrouter.ActionInvocation;
import jrouter.ActionProxy;
import jrouter.JRouterException;
import jrouter.ParameterConverter;
import jrouter.annotation.Result;

/**
 * 测试ActionFactory的ConverterFactory传递参数的扩展性。
 */
public class ConverterParameterActionFactory extends DefaultActionFactory {

    public ConverterParameterActionFactory(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    protected ActionInvocation createActionInvocation(String path, Object... params) {
        ActionInvocation ai = super.createActionInvocation(path, params);
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
    public static interface DemoActionInvocation extends ActionInvocation {

        String getName();
    }

    /**
     * 代理ActionInvocation，并添加自定义的属性和实现接口。
     */
    public static class DefaultDemoActionInvocation implements DemoActionInvocation {

        /* name */
        private final String name;

        /* 代理的ActionInvocation */
        private final ActionInvocation invocation;

        public DefaultDemoActionInvocation(String name, ActionInvocation invocation) {
            this.name = name;
            this.invocation = invocation;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ActionFactory getActionFactory() {
            return invocation.getActionFactory();
        }

        @Override
        public ActionProxy getActionProxy() {
            return invocation.getActionProxy();
        }

        @Override
        public boolean isExecuted() {
            return invocation.isExecuted();
        }

        @Override
        public Object[] getParameters() {
            return invocation.getParameters();
        }

        @Override
        public Object invoke(Object... params) throws JRouterException {
            return invocation.invoke(params);
        }

        @Override
        public Object invokeActionOnly(Object... params) throws JRouterException {
            return invocation.invokeActionOnly(params);
        }

        @Override
        public String getActionPath() {
            return invocation.getActionPath();
        }

        @Override
        public Object getInvokeResult() {
            return invocation.getInvokeResult();
        }

        @Override
        public void setInvokeResult(Object result) {
            invocation.setInvokeResult(result);
        }

        @Override
        public void setResult(Result result) {
            invocation.setResult(result);
        }

        @Override
        public Result getResult() {
            return invocation.getResult();
        }

        @Override
        public void setParameterConverter(ParameterConverter parameterConverter) {
            invocation.setParameterConverter(parameterConverter);
        }

        @Override
        public ParameterConverter getParameterConverter() {
            return invocation.getParameterConverter();
        }

        @Override
        public void setConvertParameters(Object... params) {
            invocation.setConvertParameters(params);
        }

        @Override
        public Object[] getConvertParameters() {
            return invocation.getConvertParameters();
        }

    }
}
