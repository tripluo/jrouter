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
import net.jrouter.ActionProxy;
import net.jrouter.annotation.Action;
import net.jrouter.annotation.Interceptor;
import net.jrouter.annotation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Action代理类，包含了Action的命名空间、全路径、所对应的拦截器集合、结果对象集合等信息。
 */
public final class PathActionProxy extends DefaultProxy implements ActionProxy<String>, Cloneable {

    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(PathActionProxy.class);

    /**
     * ActionFactory
     */
    private final ActionFactory<String> actionFactory;

    /**
     * 命名空间
     */
    @lombok.Getter
    private final String namespace;

    /**
     * 全路径
     */
    @lombok.Getter
    private final String path;

    /**
     * Action
     */
    @lombok.Getter
    private final Action action;

    /**
     * Action初始化参数的键/值映射。
     */
    @lombok.Setter(lombok.AccessLevel.PACKAGE)
    @lombok.Getter
    private Map<String, String[]> actionParameters;

    /**
     * Action所配置的拦截器集合
     */
    @lombok.Setter(lombok.AccessLevel.PACKAGE)
    private List<InterceptorProxy> interceptors;

    /**
     * 结果对象的映射集合
     */
    @lombok.Setter(lombok.AccessLevel.PACKAGE)
    @lombok.Getter
    private Map<String, Result> results;

    /**
     * 构造一个Action的代理类，包含指定的ActionFactory、命名空间、全路径、所代理的Action、代理的方法及方法的对象。
     * @param actionFactory 指定的ActionFactory。
     * @param namespace 命名空间。
     * @param path 全路径。
     * @param action 代理的Action。
     * @param method 代理的方法。
     * @param object 代理的方法的对象。
     */
    public PathActionProxy(ActionFactory<String> actionFactory, String namespace, String path, Action action,
            Method method, Object object) {
        super(method, object, actionFactory);
        this.actionFactory = actionFactory;
        this.namespace = namespace;
        this.path = path;
        this.action = action;
    }

    /**
     * 根据Action状态（是否单例）返回代理对象。
     * @return 如果Action为单例则返回其代理对象，否则创建并返回新的代理对象。
     */
    public PathActionProxy getInstance() {
        if (action != null) {
            switch (action.scope()) {
                case SINGLETON:
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Get singleton ActionProxy [{}] at : {}", this, getMethodInfo());
                    }
                    return this;
                case PROTOTYPE: {
                    if (object != null) {
                        try {
                            Object invoker = actionFactory.getObjectFactory().newInstance(object.getClass());
                            // inject properties
                            Injector.injectAction(path, invoker);
                            PathActionProxy ap = this.clone();
                            ap.object = invoker;
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Get prototype ActionProxy [{}] at : {}", ap, getMethodInfo());
                            }
                            return ap;
                        }
                        catch (IllegalAccessException | CloneNotSupportedException ex) {
                            throw new InvocationProxyException(ex, this);
                        }
                        catch (InvocationTargetException ex) {
                            throw new InvocationProxyException(ex.getTargetException(), this);// NOPMD
                            // PreserveStackTrace
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
        return this;
    }

    @Override
    public PathActionProxy clone() throws CloneNotSupportedException {
        return (PathActionProxy) super.clone();
    }

    @Override
    public String getActionParameter(String name) {
        String[] params = getActionParameterValues(name);
        if (params == null || params.length == 0) {
            return null;
        }
        if (params.length > 1) {
            LOG.warn(
                    "ActionParameter '{}' is [Ljava.lang.String[{}]; return the first value; use method \"getActionParameterValues\" instead",
                    name, params.length);
            // throw new ClassCastException("ActionParameter '" + name + "' is
            // [Ljava.lang.String[" + params.length + "]; cannot be cast to
            // java.lang.String");
        }
        return params[0];
    }

    @Override
    public String[] getActionParameterValues(String name) {
        return actionParameters.get(name);
    }

    @Override
    public List<Interceptor> getInterceptors() {
        List<Interceptor> is = new java.util.ArrayList<>(interceptors.size());
        for (InterceptorProxy ip : interceptors) {
            is.add(ip.getInterceptor());
        }
        return is;
    }

    /**
     * 返回Action所配置的拦截器集合，不包含任何拦截器则返回长度为 0 的集合。
     * @return Action所配置的拦截器集合。
     */
    public List<InterceptorProxy> getInterceptorProxies() {
        return interceptors;
    }

}
