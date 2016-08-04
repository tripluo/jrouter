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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jrouter.ActionInvocation;
import jrouter.ConverterFactory;
import jrouter.JRouterException;
import jrouter.ParameterConverter;

/**
 * 创建多参数自动映射转换器的工厂类。
 */
public class MultiParameterConverterFactory implements ConverterFactory {

    /**
     * 不缓存转换参数位置的工厂类。提供一个便捷的无参数构造类。
     * MultiParameterConverterFactory.NoFixedOrder()即 MultiParameterConverterFactory(false)。
     */
    public static class NoFixedOrder extends MultiParameterConverterFactory {

        /**
         * 不缓存转换参数位置的工厂类。
         */
        public NoFixedOrder() {
            super(false);
        }

    }

    /** 缓存转换参数匹配的位置 */
    private Map<Method, int[]> methodParametersCache;

    /**
     * 转换参数类型是否固定顺序，默认固定参数。
     *
     * @see ActionInvocation#getConvertParameters()
     */
    private final boolean fixedOrder;

    /**
     * 多参数转换器，线程安全的单例对象。
     */
    private final ParameterConverter parameterConverter;

    /**
     * 创建固定参数自动映射转换器的工厂类。
     */
    public MultiParameterConverterFactory() {
        this(true);
    }

    /**
     * 创建多参数自动映射转换器的工厂类。
     *
     * @param fixedOrder 参数类型是否固定顺序。
     */
    public MultiParameterConverterFactory(boolean fixedOrder) {
        this.fixedOrder = fixedOrder;
        if (fixedOrder) {
            methodParametersCache = new ConcurrentHashMap<Method, int[]>();
        }
        parameterConverter = new MultiParameterConverter();
    }

    /**
     * 返回线程安全的多参数自动映射转换器。
     * 此参数转换器可能需要ActionFactory支持，在创建ActionInvocation时区分处理原始参数和转换参数。
     *
     */
    @Override
    public ParameterConverter getParameterConverter(ActionInvocation actionInvocation) {
        return parameterConverter;
    }

    /**
     * 提供多参数自动映射的转换器。不包含任何成员对象，线程安全。
     * 如果原方法的调用参数数目大于或等于方法本身所需的参数个数，则返回未处理的原调用参数（调用时可能会抛出方法调用异常）。
     * 仅在原方法的调用参数数目小于方法本身所需的参数个数时，注入并自动映射追加的转换参数（无转换参数类型匹配映射{@code null}）。
     */
    public class MultiParameterConverter implements ParameterConverter {

        @Override
        public Object[] convert(Method method, Object obj, Object[] originalParams,
                Object[] convertParams) throws JRouterException {
            if (convertParams == null || convertParams.length == 0)
                return originalParams;
            Class<?>[] parameterTypes = method.getParameterTypes();
            int originalSize = parameterTypes.length;
            //变长或原本无参数的方法
            if (method.isVarArgs() || originalSize == 0) {
                return originalParams;
            }
            int pLen = (originalParams == null ? 0 : originalParams.length);
            //保留原参数，追加支持的绑定参数
            if (originalSize > pLen) {
                Object[] newArgs = new Object[originalSize];
                if (pLen > 0)
                    System.arraycopy(originalParams, 0, newArgs, 0, pLen);
                int[] idx = match(method, pLen, parameterTypes, convertParams);
                for (int i = pLen; i < originalSize; i++) {
                    newArgs[i] = (idx[i] == -1 ? null : convertParams[idx[i]]);
                }
                return newArgs;
            }
            return originalParams;
        }

        /**
         * 匹配追加注入的参数相对于方法参数类型中的映射；
         * 匹配顺序不考虑父子优先级，追加的参数按顺序优先匹配；
         * 如果追加注入的参数类型固定，则会缓存记录。
         *
         * @param method 指定的方法。
         * @param parameterTypes 方法的参数类型。
         * @param convertParams 追加注入的参数。
         *
         * @return 追加注入的参数相对于方法参数类型中的映射。
         *
         * @see #methodParametersCache
         */
        private int[] match(Method method, int matchStart, Class[] parameterTypes,
                Object[] convertParams) {
            int[] idx = null;
            if (fixedOrder) {
                //get from cache
                idx = methodParametersCache.get(method);
                if (idx != null)
                    return idx;
            }
            idx = new int[parameterTypes.length];
            boolean[] convertMatched = null;
            if (convertParams != null) {
                convertMatched = new boolean[convertParams.length];
            }
            for (int i = matchStart; i < idx.length; i++) {
                //初始值-1, 无匹配
                idx[i] = -1;
                if (convertParams != null) {
                    Class parameterType = parameterTypes[i];
                    for (int j = 0; j < convertParams.length; j++) {
                        //不考虑父子优先级，参数按顺序优先匹配。
                        if (!convertMatched[j] && parameterType.isInstance(convertParams[j])) {
                            idx[i] = j;
                            convertMatched[j] = true;
                            break;
                        }
                    }
                }
            }
            if (fixedOrder) {
                //put in cache
                methodParametersCache.put(method, idx);
            }
            return idx;
        }
    }

    /**
     * 参数类型是否固定顺序。
     *
     * @return 参数类型是否固定顺序。
     */
    public boolean isFixedOrder() {
        return fixedOrder;
    }
}
