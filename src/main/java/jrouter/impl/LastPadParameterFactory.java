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

import java.lang.reflect.Method;
import jrouter.ActionInvocation;
import jrouter.ConverterFactory;
import jrouter.JRouterException;
import jrouter.ParameterConverter;
import jrouter.annotation.Dynamic;

/**
 * 创建追加最后一个参数的转换对象的工厂类。
 */
public class LastPadParameterFactory implements ConverterFactory {

    /**
     * 默认追加ActionInvocation参数的转换类。如果原方法参数的个数或类型不符，则返回原参数。
     *
     * @param actionInvocation ActionInvocation对象。
     *
     * @return 追加ActionInvocation参数的转换类。
     */
    @Override
    public ParameterConverter getParameterConverter(ActionInvocation actionInvocation) {
        return new LastPadParameterConverter(actionInvocation);
    }

    /**
     * 提供追加最后一个参数的转换类。如果原方法参数的个数或类型不符，则返回未处理的参数。
     */
    @Dynamic
    public static class LastPadParameterConverter implements ParameterConverter {

        /** 追加的最后一个参数 */
        @Dynamic
        private Object lastParameter;

        /**
         * 构造一个空对象。
         */
        public LastPadParameterConverter() {
        }

        /**
         * 构造一个指定追加最后参数的转换对象。
         *
         * @param lastParameter 追加的最后一个参数。
         */
        public LastPadParameterConverter(Object lastParameter) {
            this.lastParameter = lastParameter;
        }

        @Override
        public Object[] convert(Method method, Object obj, Object[] params) throws JRouterException {
            int originalSize = method.getParameterTypes().length;
            if (method.isVarArgs() || originalSize == 0) {
                return params;
            }
            if (params == null || params.length == 0) {
                return originalSize == 1 ? new Object[]{lastParameter} : params;
            }
            //last parameter amount matched
            if (originalSize == params.length + 1) {
                //last parameter type matched
                if (method.getParameterTypes()[originalSize - 1].isInstance(lastParameter)) {
                    Object[] newArgs = new Object[originalSize];
                    System.arraycopy(params, 0, newArgs, 0, originalSize - 1);
                    //add the last parameter
                    newArgs[params.length] = lastParameter;
                    return newArgs;
                }
            }
            return params;
        }

        /**
         * 设置追加的最后一个参数。
         *
         * @param lastParameter 追加的最后一个参数。
         */
        public void setLastParameter(Object lastParameter) {
            this.lastParameter = lastParameter;
        }
    }
}
