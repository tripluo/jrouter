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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import jrouter.ActionFactory;
import jrouter.ActionInvocation;
import jrouter.ActionProxy;
import jrouter.ParameterConverter;
import jrouter.annotation.Action;
import jrouter.annotation.Dynamic;
import jrouter.annotation.Result;
import jrouter.util.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于{@code String}类型路径的{@link Action}运行时上下文代理类，
 * 记录了{@link Action}运行时的状态、调用参数、拦截器、结果对象、{@link ActionFactory}等信息。
 */
@Dynamic
public class PathActionInvocation implements ActionInvocation<String> {

    /** 日志 */
    private static final Logger LOG = LoggerFactory.getLogger(PathActionInvocation.class);

    /** Action是否已调用 */
    private boolean executed = false;

    /** ActionFactory */
    private final ActionFactory actionFactory;

    /** PathActionProxy */
    private final PathActionProxy actionProxy;

    /** interceptors reference */
    private final List<InterceptorProxy> interceptors;

    /** recursion invoke index */
    private int _index = 0;

    /** Aciton调用的真实路径 */
    private final String actionPath;

    /** 方法调用的参数 */
    private final Object[] originalParams;

    /** 提供给转换器的参数 */
    private Object[] convertParameters;

    /** 方法调用后的结果 */
    private Object invokeResult;

    /** Action结果对象 */
    private Result result;

    /** Action路径的参数匹配映射 */
    private Map<String, String> actionPathParameters;

    /** 底层方法参数的转换器 */
    @Dynamic
    private ParameterConverter parameterConverter;

    /**
     * 构造一个Action运行时上下文的代理类，包含指定的ActionFactory、ActionProxy、Action路径的参数匹配映射及调用参数。
     *
     * @param realPath Actino不含绑定参数的真实路径。
     * @param actionFactory Action工厂对象。
     * @param actionProxy Action代理对象。
     * @param originalParams Action代理对象中方法调用的原始参数。
     */
    public PathActionInvocation(String realPath, ActionFactory actionFactory,
            PathActionProxy actionProxy, Object... originalParams) {
        this.actionPath = realPath;
        this.actionFactory = actionFactory;
        this.actionProxy = actionProxy;
        this.originalParams = originalParams;
        this.convertParameters = new Object[]{this};
        this.interceptors = actionProxy.getInterceptorProxies();
    }

    @Override
    public Object invokeActionOnly(Object... params) throws InvocationProxyException {
        //params is null only if explicitly set it null
        if (params == null || params.length == 0)
            params = this.originalParams; //TODO
        setExecuted(true);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoke Action [{}]; Parameters {} at : {}",
                    actionProxy.getPath(), java.util.Arrays.toString(params), actionProxy.getMethodInfo());
        }
        //set invokeResult
        invokeResult = MethodUtil.invoke(actionProxy, parameterConverter, params, getConvertParameters());
        return invokeResult;
    }

    @Override
    public Object invoke(Object... params) throws InvocationProxyException {
        if (executed) {
            throw new IllegalStateException("Action [" + actionProxy.getPath() + "] has already been executed");
        }
        if (parameterConverter == null && actionFactory.getConverterFactory() != null) {
            parameterConverter = actionFactory.getConverterFactory().getParameterConverter(this);
        }
        //recursive invoke
        if (interceptors != null && _index < interceptors.size()) {
            final InterceptorProxy interceptor = interceptors.get(_index++);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Invoke Interceptor [{}] at : {}", interceptor.getName(), interceptor.getMethodInfo());
            }
            //pass ActionInvocation to Interceptor for recursive invoking by parameterConverter
            invokeResult = MethodUtil.invoke(interceptor, parameterConverter, null, getConvertParameters());
        } else //action invoke
            if (!executed) {
                invokeActionOnly(params);
            }
        return invokeResult;
    }

    /**
     * 设置Action是否已调用。
     *
     * @param executed Action是否已调用。
     */
    void setExecuted(boolean executed) {
        this.executed = executed;
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public Object[] getParameters() {
        return originalParams;
    }

    @Override
    public Object getInvokeResult() {
        return invokeResult;
    }

    @Override
    public void setInvokeResult(Object invokeResult) {
        this.invokeResult = invokeResult;
    }

    @Override
    public ActionFactory getActionFactory() {
        return actionFactory;
    }

    @Override
    public ActionProxy getActionProxy() {
        return actionProxy;
    }

    @Override
    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public Result getResult() {
        return result;
    }

    /**
     * 设置Action路径匹配的键值映射。
     *
     * @return 设置Action路径匹配的键值映射。
     */
    void setActionPathParameters(Map<String, String> actionPathParameters) {
        this.actionPathParameters = actionPathParameters;
    }

    @Override
    public String getActionPath() {
        return actionPath;
    }

    /**
     * TODO
     *
     * 返回Action路径匹配的键值映射，不包含任何匹配的键值则返回空映射。
     *
     * @return Action路径匹配的键值映射。
     */
    public Map<String, String> getActionPathParameters() {
        return actionPathParameters;
    }

    @Override
    public void setParameterConverter(ParameterConverter parameterConverter) {
        this.parameterConverter = parameterConverter;
    }

    @Override
    public ParameterConverter getParameterConverter() {
        return this.parameterConverter;
    }

    @Override
    public Object[] getConvertParameters() {
        return this.convertParameters;
    }

    @Override
    public void setConvertParameters(Object... params) {
        this.convertParameters = params;
    }

    /**
     * 结果对象的实现。
     */
    public static class ResultProxy implements Result {

        /*
         * 结果对象的名称
         */
        private final String name;

        /*
         * 结果对象的类型
         */
        private final String type;

        /*
         * 结果对象对应的资源路径
         */
        private final String location;

        /**
         * 构造一个结果对象。
         *
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
