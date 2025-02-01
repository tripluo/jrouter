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

package net.jrouter.impl;

import net.jrouter.ActionFactory;
import net.jrouter.ActionInvocation;
import net.jrouter.ParameterConverter;
import net.jrouter.annotation.Action;
import net.jrouter.annotation.Dynamic;
import net.jrouter.annotation.Result;
import net.jrouter.util.CollectionUtil;
import net.jrouter.util.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * 基于{@code String}类型路径的{@link Action}运行时上下文代理类，
 * 记录了{@link Action}运行时的状态、调用参数、拦截器、结果对象、{@link ActionFactory}等信息。
 */
@Dynamic
public class PathActionInvocation implements ActionInvocation<String> {

    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(PathActionInvocation.class);

    /**
     * ActionFactory
     */
    @lombok.Getter
    private final ActionFactory<String> actionFactory;

    /**
     * PathActionProxy
     */
    @lombok.Getter
    private final PathActionProxy actionProxy;

    /**
     * interceptors reference
     */
    private final List<InterceptorProxy> interceptors;

    /**
     * Aciton调用的真实路径
     */
    @lombok.Getter
    private final String actionPath;

    /**
     * 方法原始调用的参数
     */
    private final Object[] originalParameters;

    /**
     * Action是否已调用
     */
    @lombok.Setter(lombok.AccessLevel.PACKAGE)
    @lombok.Getter
    private boolean executed = false;

    /**
     * recursion invoke index
     */
    private int interceptorIndex = 0;

    /**
     * 提供给转换器的参数
     */
    @lombok.Getter
    @lombok.Setter
    private Object[] convertParameters;

    /**
     * 方法调用后的结果
     */
    @lombok.Getter
    @lombok.Setter
    private Object invokeResult;

    /**
     * Action结果对象
     */
    @lombok.Getter
    @lombok.Setter
    private Result result;

    /**
     * Action路径的参数匹配映射
     */
    @lombok.Getter
    @lombok.Setter(lombok.AccessLevel.PACKAGE)
    private Map<String, String> pathParameters;

    /**
     * 方法参数转换器
     */
    @Dynamic
    @lombok.Getter
    @lombok.Setter
    private ParameterConverter parameterConverter;

    /**
     * 构造一个Action运行时上下文的代理类，包含指定的ActionFactory、ActionProxy、Action路径的参数匹配映射及调用参数。
     * @param realPath Actino不含绑定参数的真实路径。
     * @param actionFactory Action工厂对象。
     * @param actionProxy Action代理对象。
     * @param originalParams Action代理对象中方法调用的原始参数。
     */
    public PathActionInvocation(String realPath, ActionFactory<String> actionFactory, PathActionProxy actionProxy,
            Object... originalParams) {
        this.actionPath = realPath;
        this.actionFactory = actionFactory;
        this.actionProxy = actionProxy;
        this.originalParameters = originalParams;
        this.convertParameters = new Object[] { this };
        this.interceptors = actionProxy.getInterceptorProxies();
    }

    @Override
    public Object invokeActionOnly(Object... params) throws InvocationProxyException {
        Object[] originalParams = this.originalParameters;
        // additional parameters pass to Action
        if (CollectionUtil.isNotEmpty(params)) {
            originalParams = CollectionUtil.append(params, this); // TODO
        }
        setExecuted(true);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoke Action [{}]; Parameters {} at : {}", actionProxy.getPath(),
                    java.util.Arrays.toString(originalParams), actionProxy.getMethodInfo());
        }
        // set invokeResult
        invokeResult = MethodUtil.invoke(actionProxy, parameterConverter, originalParams, getConvertParameters());
        return invokeResult;
    }

    @Override
    public Object invoke(Object... params) throws InvocationProxyException {
        if (executed) {
            throw new IllegalStateException("Action [" + actionProxy.getPath() + "] has already been executed");
        }
        if (parameterConverter == null && actionFactory.getConverterFactory() != null) {
            parameterConverter = actionFactory.getConverterFactory().getParameterConverter();
        }
        // recursive invoke
        if (interceptors != null && interceptorIndex < interceptors.size()) {
            final InterceptorProxy interceptor = interceptors.get(interceptorIndex++);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Invoke Interceptor [{}] at : {}", interceptor.getName(), interceptor.getMethodInfo());
            }
            // pass ActionInvocation to Interceptor for recursive invoking by
            // parameterConverter
            MethodUtil.invokeConvertParameters(interceptor, this);
        }
        else // action invoke
        if (!executed) {
            invokeActionOnly(params);
        }
        return invokeResult;
    }

    @Override
    public Object[] getParameters() {
        return originalParameters;
    }

    /**
     * 结果对象的实现。
     */
    public static class ResultProxy implements Result {

        /**
         * 结果对象的名称
         */
        private final String name;

        /**
         * 结果对象的类型
         */
        private final String type;

        /**
         * 结果对象对应的资源路径
         */
        private final String location;

        /**
         * 构造一个结果对象。
         * @param name 结果对象的名称。
         * @param type 结果对象的类型。
         * @param location 结果对象对应的资源路径。
         */
        public ResultProxy(String name, String type, String location) {
            this.name = name;
            this.type = type;
            this.location = location;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public String location() {
            return location;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ResultProxy.class;
        }

    }

}
