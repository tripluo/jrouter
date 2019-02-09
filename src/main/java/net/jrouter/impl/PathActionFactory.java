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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import net.jrouter.*;
import net.jrouter.annotation.*;
import net.jrouter.util.CollectionUtil;
import net.jrouter.util.MethodUtil;
import net.jrouter.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于调用{@code String}类型路径{@link Action}的{@link ActionFactory}实现类，以'/'作为路径的分隔符。
 *
 * <p>
 * PathActionFactory中的加载和调用的拦截器、结果类型对象均为单例；
 * {@link Action}对象根据其{@link Action#scope()}属性判断是否单例。
 * </p>
 * <p>
 * PathActionFactory中的{@link Action}、{@link Interceptor}拦截器、{@link InterceptorStack}拦截栈、
 * {@link ResultType}结果类型、{@link Result}结果对象及的集合在初始化时加载完成，
 * 之后任何情况下PathActionFactory不再执行集合的修改和删除操作。对返回集合进行的修改和删除需自行保证其线程安全性。
 * </p>
 */
public class PathActionFactory extends AbstractActionFactory<String> {

    /** 日志 */
    private static final Logger LOG = LoggerFactory.getLogger(PathActionFactory.class);

    /**
     * 路径分隔符。
     */
    @lombok.Getter
    private final char pathSeparator;

    /**
     * 全匹配标识。
     *
     * @deprecated
     */
    private final static String MATCH = PathTree.SINGLE_MATCH;

    /**
     * 路径后缀，默认为null；非空时截断路径后缀。
     */
    @lombok.Getter
    private final String extension;

    /**
     * {@link #actionCache}最大缓存数目，默认最大缓存1w条记录；缓存数目小于0则无缓存。
     */
    @lombok.Getter
    private final int actionCacheNumber;

    /**
     * 默认拦截栈名称。作用于初始化Action时的配置。
     *
     * @see #createActionProxy(Method, Object)
     */
    @lombok.Getter
    private final String defaultInterceptorStack;

    /**
     * 默认的结果类型。
     *
     * @see #invokeResult
     */
    @lombok.Getter
    private final String defaultResultType;

    /* default object handler */
    private ResultTypeProxy cacheDefaultResultType = null;
////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 实际的Action树结构路径映射。
     */
    private final PathTreeMap<PathActionProxy> pathActions;

    /**
     * 路径生成器。
     */
    @lombok.Getter
    private final PathGenerator<String> pathGenerator;

    /**
     * Action路径与代理对象的映射缓存。
     */
    private final ActionCache actionCache;

////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 根据指定的{@code Properties}初始化{@code PathActionFactory}对象。
     *
     * @param properties 指定的{@code Properties}。
     */
    public PathActionFactory(Properties properties) {
        super(properties);
        //pass properties
        this.pathGenerator = properties.pathGenerator;
        this.pathSeparator = properties.pathSeparator;
        this.extension = properties.extension;
        this.actionCacheNumber = properties.actionCacheNumber;
        this.defaultInterceptorStack = properties.defaultInterceptorStack;
        this.defaultResultType = properties.defaultResultType;
        //initiate
        pathActions = new PathTreeMap<>(pathSeparator);
        actionCache = new ActionCache(new java.util.concurrent.ConcurrentHashMap<String, ActionCacheEntry>(),
                Collections.synchronizedMap(new net.jrouter.util.LRUMap<String, ActionCacheEntry>(actionCacheNumber)));
    }

    /**
     * 根据指定的键值映射构造初始化数据的PathActionFactory对象。
     *
     * @param properties 指定的初始化数据键值映射。
     *
     * @deprecated Use {@link #PathActionFactory(Properties)}.
     */
    public PathActionFactory(Map<String, Object> properties) {
        this(new Properties().properties(properties));
    }

    /**
     * 通过路径调用相应的Action，可传递Action方法相应的参数。
     * Action调用是否为线程安全取决于路径所映射方法的线程安全性。
     *
     * @param path Action的映射路径。
     * @param params Action的调用参数。
     *
     * @return 调用后的结果；如果结果为字符串类型非空且存在结果对象，则查找相应的结果类型并返回调用后的结果；反之默认直接返回结果。
     *
     * @throws JRouterException 如果发生调用错误。
     * @see #invokeResult
     */
    @Override
    public Object invokeAction(String path, Object... params) throws JRouterException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start invoking Action [{}]; Parameters {} ", path, java.util.Arrays.toString(params));
        }
        //remove the extension
        //当后缀为单个字符时，按路径最后出现分割符的位置截断路径后缀；当后缀为非空字符串时，如果路径以后缀结尾，截断后缀。
        if (StringUtil.isNotEmpty(extension)) {
            int len = extension.length();
            //extension为特定的标记字符，则截去标记字符后的部分
            if (len == 1 && !Character.isLetterOrDigit(extension.charAt(0))) {
                int index = path.lastIndexOf(extension.charAt(0));
                if (index != -1) {
                    path = path.substring(0, index);
                }
            } else //extension为特定的后缀字符串
                if (path.endsWith(extension)) {
                    //如果extension前一位非字母或数字
                    if (!Character.isLetterOrDigit(path.charAt(path.length() - len - 1))) {
                        len++;
                    }
                    path = path.substring(0, path.length() - len);
                }
        }

        //create ActionInvocation
        ActionInvocation<String> invocation = createActionInvocation(path, params);
        //invoke
        Object res = null;
        try {
            res = invocation.invoke();
            LOG.debug("Get invoked Action [{}] result : [{}]", path, res);
            Object rr = invokeResult(invocation, res);
            if (rr != null) {
                res = rr;
            }
        } catch (InvocationProxyException e) {
            //去除不必要的InvocationProxyException异常，封装异常的源并抛出。
            throw e.getSourceInvocationException();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finish invoking Action [{}]; Parameters {}; Final result : [{}]",
                    path, java.util.Arrays.toString(params), String.valueOf(res));
        }
        return res;
    }

    /**
     * 创建Action调用时的上下文对象。
     *
     * @param path Action的映射路径。
     * @param params Action的调用参数。
     *
     * @return Action调用时的上下文对象。
     */
    protected ActionInvocation<String> createActionInvocation(String path, Object... params) {
        //cache
        ActionCacheEntry ace = null;
        if (actionCacheNumber > 0) {
            ace = actionCache.get(path);
        }
        //Action代理对象
        PathActionProxy ap = null;
        //路径的参数匹配映射
        Map<String, String> matchParameters = null;

        //get from cache
        if (ace != null) {
            ap = ace.actionProxy;
            matchParameters = ace.matchParameters;
        } else {
            //initiate matchParameters
            matchParameters = new HashMap<>(2);
            //get Action and fill matchParameters
            ap = pathActions.get(path, matchParameters);

            if (ap == null) {
                throw new NotFoundException("No such Action : " + path);
            }

            ace = new ActionCacheEntry(ap, matchParameters.isEmpty()
                    ? Collections.EMPTY_MAP : Collections.unmodifiableMap(matchParameters));
            //put in cache, ignore multi-thread issue here
            putActionCache(path, ace);
        }

        //scope action
        ap = ap.getInstance();
        //create PathActionInvocation
        PathActionInvocation ai = new PathActionInvocation(path, this, ap, params);
        //setActionPathParameters
        ai.setActionPathParameters(matchParameters);
        return ai;
    }

    @Override
    public void addResultType(ResultTypeProxy rtp) {
        super.addResultType(rtp);
        if (rtp.getType().equals(defaultResultType)) {
            LOG.info("Loading default ResultType [{}] : {}", defaultResultType, rtp);
            cacheDefaultResultType = rtp;
        }
    }

    /**
     * 用于子类继承，提供非字符串{@code string}对象的处理方式。
     * 默认直接返回非字符串对象，void方法返回 null 。
     *
     * @param invocation Action运行时上下文。
     * @param res Action调用完成后的结果。
     *
     * @return 非字符串对象。
     */
    protected Object invokeResult(ActionInvocation invocation, Object res) {
        if (cacheDefaultResultType != null) {
            return MethodUtil.invokeConvertParameters(cacheDefaultResultType, invocation);
        }
        if (LOG.isWarnEnabled()) {
            LOG.warn("Invoking Object Result [{}] and return directly at : {}", res, MethodUtil.getMethod(invocation.getActionProxy().getMethod()));
        }
        return res;
    }

    /**
     * 提供特定字符串结果的处理；默认解析冒号':'结果。
     * 默认提供"type:location"形式的字符串解析；以第一个':'划分。
     *
     * @see #invokeColonString
     * @see #parseMatch
     */
    public static class ColonString extends PathActionFactory {

        /**
         * 默认{@code String}结果的处理类型。
         */
        @lombok.Getter
        private final String defaultStringResultType;

        /**
         * Constructor.
         *
         * @param properties Properties
         */
        public ColonString(Properties properties) {
            super(properties);
            this.defaultStringResultType = properties.defaultStringResultType;
        }

        /**
         * Constructor.
         *
         * @param properties Properties
         *
         * @deprecated Use {@link #ColonString(Properties)}.
         */
        public ColonString(Map<String, Object> properties) {
            this(new Properties().properties(properties));
        }

        /**
         * ColonString 属性。
         */
        @lombok.Getter
        @lombok.Setter
        public static class Properties extends PathActionFactory.Properties {

            /**
             * @see ColonString#defaultStringResultType
             */
            private String defaultStringResultType = null;

            @Override
            protected Properties properties(Map<String, Object> properties) {
                super.properties(properties);
                Object value = properties.get("defaultStringResultType");
                if (value != null) {
                    defaultStringResultType = value.toString();
                }
                return this;
            }

        }

        @Override
        protected Object invokeResult(ActionInvocation invocation, Object res) {
            //string result
            if (res instanceof String) {
                Object rr = invokeStringResult(invocation, res.toString());
                if (rr != null) {
                    res = rr;
                }
            } else {
                //非字符串结果的对象处理方式
                Object rr = super.invokeResult(invocation, res);
                if (rr != null) {
                    res = rr;
                }
            }
            return res;
        }

        /**
         * 用于子类继承，提供字符串{@code string}对象的处理方式。
         * 默认直接返回非字符串对象，void方法返回 null 。
         *
         * @param invocation Action运行时上下文。
         * @param stringRes Action调用后的字符串型结果。
         *
         * @return 调用的结果。
         */
        protected Object invokeStringResult(ActionInvocation invocation, String stringRes) {
            Object res = null;
            ActionProxy<String> ap = invocation.getActionProxy();
            Result result = null;

            //如果action中存在相应的结果映射
            if ((result = ap.getResults().get(stringRes)) != null) {
                //调用结果对象相应的结果类型
                Object rr = invokeStringResult(invocation, result);
                if (rr != null) {
                    res = rr;
                }
            } //如果Action调用结果的路径信息中包含':'可省略Action中的@Result(name = "*"...)
            else if ((result = ap.getResults().get(MATCH)) != null || stringRes.indexOf(':') != -1) {
                //非完全匹配字符串路径的调用
                Object rr = invokeColonString(invocation, result, stringRes);
                if (rr != null) {
                    res = rr;
                }
            } //如果全局结果对象集合中存在相应的结果映射
            else if (getResults().containsKey(stringRes)) {
                ResultProxy rp = getResults().get(stringRes);
                //ResultProxy直接调用
                Object rr = MethodUtil.invokeConvertParameters(rp, invocation);
                if (rr != null) {
                    res = rr;
                }
                result = rp.getResult();
                //当Result的type值不为空时，执行相应的ResultType
                if (StringUtil.isNotEmpty(result.type())) {
                    rr = invokeStringResult(invocation, result);
                    if (rr != null) {
                        res = rr;
                    }
                }
            } else {
                //若无匹配
                Object rr = super.invokeResult(invocation, stringRes);
                if (rr != null) {
                    res = rr;
                }
            }
            return res;
        }

        /**
         * 调用Result相应的ResultType。
         *
         * @param invocation Action运行时上下文。
         * @param result 结果对象。
         *
         * @return 调用ResultType后的结果。
         */
        private Object invokeStringResult(ActionInvocation invocation, Result result) {
            String type = result.type();
            //default result type
            if (StringUtil.isEmpty(type)) {
                type = defaultStringResultType;
            }
            ResultTypeProxy rtp = getResultTypes().get(type);
            if (rtp == null) {
                throw new NotFoundException("No such ResultType [" + type + "] at : "
                        + MethodUtil.getMethod(invocation.getActionProxy().getMethod()));
            }
            invocation.setResult(result);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Invoke ResultType [{}] at : {}", type, rtp.getMethodInfo());
            }
            //结果类型调用
            return MethodUtil.invokeConvertParameters(rtp, invocation);
        }

        /**
         * 提供非完全匹配路径的结果对象的调用方式。
         * 默认提供全字符串结果的匹配处理。
         *
         * @param invocation Action运行时上下文。
         * @param result Action调用完成后的结果对象。
         * @param pathinfo Action调用的结果对象的路径信息。
         *
         * @return 调用相应结果类型后的值，无匹配则返回 null。
         *
         * @see #invokeStringResult
         */
        private Object invokeColonString(ActionInvocation invocation, Result result, String pathinfo) {
            //default values
            String type = defaultStringResultType;
            String loc = null;
            if (result != null) {
                if (StringUtil.isNotEmpty(result.type())) {
                    type = result.type();
                }
                loc = result.location();
            }
            //parse the string "type:location"
            String[] parseRes = parseMatch(pathinfo, type, loc);
            type = parseRes[0];
            loc = parseRes[1];
            //重新封装result参数
            Result res = new PathActionInvocation.ResultProxy(MATCH, type, loc);
            return invokeStringResult(invocation, res);
        }

        /**
         * "type:location"形式的字符串解析；以第一个':'划分。
         *
         * @param pathinfo "type:location"形式的字符串。
         * @param def type和location的默认值。
         *
         * @return 解析后的{type,location}字符串数组。
         */
        private static String[] parseMatch(String pathinfo, String... def) {
            String type = def[0];
            String loc = def[1];
            int idx = pathinfo.indexOf(':');
            String temp = null;
            switch (idx) {
                case -1:
                    if (!(temp = StringUtil.trim(pathinfo)).isEmpty()) {
                        type = temp;
                    }
                    break;
                case 0:
                    if (!(temp = StringUtil.trim(pathinfo.substring(1))).isEmpty()) {
                        loc = temp;
                    }
                    break;
                default:
                    if (!(temp = StringUtil.trim(pathinfo.substring(0, idx))).isEmpty()) {
                        type = temp;
                    }
                    if (!(temp = StringUtil.trim(pathinfo.substring(idx + 1))).isEmpty()) {
                        loc = temp;
                    }
                    break;
            }
            //返回0.type 1.location形式的数组, 值为null则返回""
            return new String[]{type == null ? "" : type, loc == null ? "" : loc};
        }
    }

    /**
     * 添加Action的路径及代理对象至缓存。
     *
     * @param path Action的路径。
     * @param ace Action的缓存对象。
     */
    private void putActionCache(String path, ActionCacheEntry ace) {
        //如果缓存设置数大于0
        if (actionCacheNumber > 0) {
            //full match
            if (ace.matchParameters.isEmpty()) {
                actionCache.putFullPathAction(path, ace);
            } else {
                actionCache.putMatchedPathAction(path, ace);
            }
        }
    }

    /**
     * 清除Action的路径映射缓存。
     */
    public void clearActionCache() {
        actionCache.clear();
    }

    @Override
    public void clear() {
        LOG.info("Clearing JRouter ActionFactory : {}", this);
        actionCache.clear();
        pathActions.clear();
        super.clear();
        Injector.clear();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 添加Action。
     *
     * @param aps Action代理对象集合。
     */
    public void addAction(PathActionProxy... aps) {
        for (PathActionProxy ap : aps) {
            String aPath = ap.getPath();

            if (StringUtil.isBlank(aPath)) {
                throw new IllegalArgumentException("Null path of Action : " + ap.getMethodInfo());
            }
            //可能存在模糊匹配 或者 完全相等的路径
            PathActionProxy exist = pathActions.get(aPath);
            //模糊匹配添加新值，完全相等的路径则特换原路径的值
            pathActions.put(aPath, ap);
            //添加或替换后查询新路径的值
            PathActionProxy newAction = pathActions.get(aPath);

            if (exist != null) {
                //新增与原有完全相等的路径
                if (exist.getPath().equals(newAction.getPath())) {
                    throw new JRouterException("Duplicate path Action [" + aPath + "] : "
                            + ap.getMethodInfo() + " override "
                            + exist.getMethodInfo());
                } //新增与原有相同的匹配路径，考虑匹配级别是否相同???
                //TODO
                //            else if (0 == PathTree.compareMathedPath(newAction.getPath(), exist.getPath())) {
                //                throw new JRouterException("Duplicate matched path Action [" + aPath + "] : "
                //                        + ap.getMethodInfo() + " override "
                //                        + exist.getMethodInfo());
                //            }
                //原有路径模糊匹配，继续添加新路径；或反之
                else if (LOG.isWarnEnabled()) {
                    LOG.warn("Exist matched path [{}] : {}, add [{}] : {}",
                            exist.getPath(), exist.getMethodInfo(), aPath, ap.getMethodInfo());
                }
            } else if (LOG.isInfoEnabled()) {
                LOG.info("Add Action [{}] at : {}", aPath, ap.getMethodInfo());
            }
        }
    }

    /**
     * 添加Action。
     *
     * @param obj 包含{@link Action}注解的类或实例对象。
     *
     * @see net.jrouter.annotation.Action
     */
    public void addActions(Object obj) {
        //判断传入参数为类或实例对象
        boolean isCls = obj instanceof Class;
        Class<?> cls = isCls ? (Class) obj : getObjectFactory().getClass(obj);
        Object invoker = isCls ? null : obj;
        //declared methods
        Method[] ms = cls.getDeclaredMethods();
        for (Method m : ms) {
            if (m.isAnnotationPresent(Ignore.class)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Ignore Action : {}", MethodUtil.getMethod(m));
                }
                continue;
            }
            Namespace ns = getActionFilter().getNamespace(obj, m);
            boolean autoIncluded = ns != null && ns.autoIncluded();
            int mod = m.getModifiers();
            //include all public/protected methods
            if ((autoIncluded && (Modifier.isPublic(mod) || Modifier.isProtected(mod)))
                    //ActionFilter accept
                    || getActionFilter().accept(obj, m)) {
                m.setAccessible(true);
                try {
                    //static method
                    if (Modifier.isStatic(mod)) {
                        addAction(createActionProxy(m, null));
                    } else {
                        if (isCls && invoker == null) {
                            invoker = getObjectFactory().newInstance(cls);
                        }
                        //the same object
                        addAction(createActionProxy(m, invoker));
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new JRouterException(e);
                }
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 创建Action代理对象。
     *
     * @param method 指定的方法。
     * @param obj 方法所在的对象。
     *
     * @return Action代理对象。
     */
    private PathActionProxy[] createActionProxy(final Method method, final Object obj) throws IllegalAccessException,
            InvocationTargetException {
        Namespace ns = getActionFilter().getNamespace(obj, method);
        //trim empty and '/'
        String namespace = ns == null ? Character.toString(pathSeparator) : pathSeparator + StringUtil.trim(ns.name(), pathSeparator);
        //use ActionFilter first
        Action action = getActionFilter().getAction(obj, method);
        //如果Action为null
        if (action == null) {
            if (ns != null && ns.autoIncluded()) {
                action = EMPTY_ACTION;
            }
            if (action == null) {
                throw new NotFoundException("@Action or specific ActionFilter is required : " + MethodUtil.getMethod(method));
            }
        }
        Class<?> objCls = (obj == null ? null : getObjectFactory().getClass(obj));
        String[] paths = pathGenerator.generatePath(objCls, method);
        PathActionProxy[] aps = new PathActionProxy[paths.length];
        int idx = 0;
        for (String path : paths) {
            //包含指定path的属性注入，其Action需重新生成对象
            Object objVar = obj;
            if (objVar != null && Injector.ACTION_INJECTION.containsKey(path)) {
                objVar = getObjectFactory().newInstance(objCls);
                Injector.injectAction(path, objVar);
            }

            //Action中不记录路径的后缀名称
            PathActionProxy ap = new PathActionProxy(this, namespace, path, action, method, objVar);
            aps[idx++] = ap;
            //void method
            if (void.class == method.getReturnType()) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Mapping [{}] void method at : {}", ap.getPath(), ap.getMethodInfo());
                }
            }
            //interceptorStack
            String stackName = action.interceptorStack().trim();
            //not not nullable action's interceptors
            String[] interceptorNames = action.interceptors();

            List<InterceptorProxy> inters = new ArrayList<>(5);
            //action interceptors
            if (interceptorNames.length != 0) {
                //action interceptorStack
                if (StringUtil.isNotEmpty(stackName)) {
                    addActionInterceptors(inters, stackName, ap);
                }
                //action中申明的interceptors
                for (String name : action.interceptors()) {
                    InterceptorProxy ip = getInterceptors().get(name);
                    if (ip == null) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("No such Interceptor [{}] at : {}", name, ap.getMethodInfo());
                        }
                    } else {
                        inters.add(ip);
                    }
                }
            } //action interceptorStack
            else if (StringUtil.isNotEmpty(stackName)) {
                addActionInterceptors(inters, stackName, ap);
            } else {
                //是否已设置action的拦截器集合
                boolean setInterceptors = false;
                //namespace interceptorStack & interceptors
                if (ns != null) {
                    //namespace interceptorStack
                    if (StringUtil.isNotEmpty(stackName = ns.interceptorStack().trim())) {
                        setInterceptors = true;
                        addActionInterceptors(inters, stackName, ap);
                    }
                    //namespace interceptors
                    if (ns.interceptors().length != 0) {
                        setInterceptors = true;
                        for (String name : ns.interceptors()) {
                            InterceptorProxy ip = getInterceptors().get(name);
                            if (ip == null) {
                                if (LOG.isWarnEnabled()) {
                                    LOG.warn("No such Interceptor [{}] at : {}", name, ap.getMethodInfo());
                                }
                            } else {
                                inters.add(ip);
                            }
                        }
                    }
                }
                //defaultInterceptorStack
                if (!setInterceptors) {
                    if (StringUtil.isNotEmpty(stackName = getDefaultInterceptorStack())) {
                        addActionInterceptors(inters, stackName, ap);
                    }
                }
            }
            //trim
            ((ArrayList) inters).trimToSize();
            ap.setInterceptors(inters);

            //set action parameters
            Parameter[] ps = action.parameters();
            Map<String, String[]> params = new HashMap<>(ps.length);
            for (Parameter p : ps) {
                params.put(p.name(), p.value());
            }
            ap.setActionParameters(Collections.unmodifiableMap(params));
            //set results
            Result[] rs = action.results();
            Map<String, Result> res = new HashMap<>(rs.length);
            for (Result r : rs) {
                res.put(r.name(), r);
            }
            ap.setResults(Collections.unmodifiableMap(res));
        }
        return aps;
    }

    /**
     * 由指定拦截栈名称添加拦截器至Action的拦截器集合。
     *
     * @param interceptors Action的拦截器集合。
     * @param stackName 指定的拦截栈名称。
     * @param ap Action代理对象。
     */
    private void addActionInterceptors(List<InterceptorProxy> interceptors, String stackName, PathActionProxy ap) {
        InterceptorStackProxy isp = getInterceptorStacks().get(stackName);
        if (isp == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("No such InterceptorStack [{}] at : {}", stackName, ap.getMethodInfo());
            }
        } else if (isp.getInterceptors() != null) {
            interceptors.addAll(isp.getInterceptors());
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * PathActionFactory 属性。
     */
    @lombok.Getter
    @lombok.Setter
    public static class Properties extends AbstractActionFactory.Properties {

        /**
         * @see PathActionFactory#pathSeparator
         */
        private char pathSeparator = PathTree.PATH_SEPARATOR;

        /**
         * @see PathActionFactory#extension
         */
        private String extension = null;

        /**
         * @see PathActionFactory#actionCacheNumber
         */
        private int actionCacheNumber = 10000;

        /**
         * @see PathActionFactory#defaultInterceptorStack
         */
        private String defaultInterceptorStack = null;

        /**
         * @see PathActionFactory#defaultResultType
         */
        private String defaultResultType = null;

        /**
         * @see PathActionFactory#pathGenerator
         */
        private PathGenerator<String> pathGenerator = new PathGenerator<String>() {

            @Override
            public String[] generatePath(Class<?> targetClass, Method method) {
                Namespace ns = getActionFilter().getNamespace(targetClass, method);
                //trim empty and '/'
                String namespace = ns == null ? Character.toString(pathSeparator) : pathSeparator + StringUtil.trim(ns.name(), pathSeparator);
                //use ActionFilter first
                Action action = getActionFilter().getAction(targetClass, method);
                //如果Action为null
                if (action == null) {
                    if (ns != null && ns.autoIncluded()) {
                        action = EMPTY_ACTION;
                    }
                    if (action == null) {
                        throw new NotFoundException("@Action or specific ActionFilter is required : " + MethodUtil.getMethod(method));
                    }
                }

                String[] names = action.name();
                //name优先value
                if (names.length == 0) {
                    names = action.value();
                }
                //name/value都未赋值或Action为null的情况
                if (names.length == 0) {
                    names = new String[]{""};
                }
                //去重复的path
                Collection<String> paths = new LinkedHashSet<>(1);
                for (String name : names) {
                    if (name != null) {
                        //Action名称可为空字符串
                        paths.add(buildActionPath(namespace, name.trim(), method));
                    }
                }
                return paths.toArray(new String[paths.size()]);
            }
        };

        /**
         * Default Constructor.
         */
        public Properties() {
            super();
        }

        @Deprecated
        @Override
        protected Properties properties(Map<String, Object> properties) {
            super.properties(properties);
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                String name = e.getKey();
                Object value = e.getValue();
                if (value == null) {
                    LOG.warn("Ingore null value Property [{}].", name);
                    continue;
                }
                //string value
                String strValue = value.toString().trim();
                if ("defaultInterceptorStack".equalsIgnoreCase(name)) {
                    //设置默认拦截栈名称
                    this.defaultInterceptorStack = strValue;
                    LOG.info("Set defaultInterceptorStack : {}", defaultInterceptorStack);
                } else if ("defaultResultType".equalsIgnoreCase(name)) {
                    //设置默认结果视图类型
                    this.defaultResultType = strValue;
                    LOG.info("Set defaultResultType : {}", defaultResultType);
                }
                if ("pathSeparator".equalsIgnoreCase(name)) {
                    if (StringUtil.isNotBlank(strValue)) {
                        this.pathSeparator = strValue.charAt(0);
                        LOG.info("Set pathSeparator : {}", this.pathSeparator);
                    }
                } else if ("pathGenerator".equalsIgnoreCase(name)) {
                    this.pathGenerator = loadComponent(PathGenerator.class, value);
                    LOG.info("Set pathGenerator : {}", this.pathGenerator);
                } else if ("extension".equalsIgnoreCase(name)) {
                    //设置路径后缀名称，不为null，可设置为空串
                    this.extension = strValue;
                    LOG.info("Set extension : {}", this.extension);
                } else if ("actionCacheNumber".equalsIgnoreCase(name)) {
                    this.actionCacheNumber = Integer.parseInt(strValue);
                    LOG.info("Set actionCacheNumber : {}", this.actionCacheNumber);
                }
            }
            return this;
        }

        /**
         * 提供继承修改构建Action路径。
         * 最终构建的路径已删除前导空白和尾部空白、以{@linkplain #getPathSeparator() pathSeparator}起始、
         * 并截去尾部{@linkplain #getPathSeparator() pathSeparator}（如果包含）。
         *
         * @param namespace Namespace名称。
         * @param aname Action的原路径。
         * @param method 指定的方法。
         *
         * @return 构建完成的Action路径。
         *
         * @see #getPathSeparator()
         */
        protected String buildActionPath(String namespace, String aname, Method method) {
            String path = null;
            if ("".equals(aname)) {
                //Action名称为空字符串时取其方法的名称（区分大小写）
                aname = method.getName();
                //if namespace is '/' or not
                path = (namespace.length() == 1 ? pathSeparator + aname : namespace + pathSeparator + aname);
            } else {
                //action's name can't be null by annotation
                String name = StringUtil.trim(aname, pathSeparator);
                //if action's name is trim as empty
                if (name.isEmpty()) {
                    path = namespace;
                } else if (pathSeparator == aname.charAt(0)) {
                    path = pathSeparator + name;
                } else {
                    //if namespace is '/' or not
                    path = (namespace.length() == 1 ? pathSeparator + name : namespace + pathSeparator + name);
                }
            }
            return path;
        }

    }

    @Override
    public Map<String, PathActionProxy> getActions() {
        return pathActions;
    }

    /**
     * 返回缓存的Action路径与其代理对象的映射。
     *
     * @return 缓存的Action路径与其代理对象的映射。
     */
    public Map<String, Object> getActionCache() {
        return (Map) actionCache.toMap();
    }

    /**
     * Action路径与缓存对象的映射，线程安全。
     */
    private static class ActionCache {

        /**
         * 固定路径Action路径与缓存对象的映射。
         */
        private final Map<String, ActionCacheEntry> fullPathActionCache;

        /**
         * 参数匹配Action路径与缓存对象的映射。
         */
        private final Map<String, ActionCacheEntry> matchedPathActionCache;

        /**
         * 构造缓存。
         *
         * @param fullPathActionCache 固定路径Action路径与缓存对象的映射。
         * @param matchedPathActionCache 参数匹配Action路径与缓存对象的映射。
         */
        ActionCache(Map<String, ActionCacheEntry> fullPathActionCache,
                    Map<String, ActionCacheEntry> matchedPathActionCache) {
            this.fullPathActionCache = fullPathActionCache;
            this.matchedPathActionCache = matchedPathActionCache;
        }

        /**
         * 获取Action代理对象。
         *
         * @param path Action路径。
         *
         * @return Action缓存对象。
         */
        ActionCacheEntry get(String path) {
            ActionCacheEntry cache = fullPathActionCache.get(path);
            if (cache != null) {
                return cache;
            }
            return matchedPathActionCache.get(path);
        }

        /**
         * 添加固定路径Action缓存。
         *
         * @param path Action路径。
         * @param cache Action缓存对象。
         *
         * @return 以前与Action路径关联的缓存对象，如果没有Action路径的映射关系，则返回 null。
         */
        ActionCacheEntry putFullPathAction(String path, ActionCacheEntry cache) {
            return fullPathActionCache.put(path, cache);
        }

        /**
         * 添加参数匹配Action缓存。
         *
         * @param path Action路径。
         * @param cache Action缓存对象。
         *
         * @return 以前与Action路径关联的缓存对象，如果没有Action路径的映射关系，则返回 null。
         */
        ActionCacheEntry putMatchedPathAction(String path, ActionCacheEntry cache) {
            return matchedPathActionCache.put(path, cache);
        }

        /**
         * 清空缓存。
         */
        void clear() {
            fullPathActionCache.clear();
            matchedPathActionCache.clear();
        }

        /**
         * 返回缓存的Map视图。
         *
         * @return 缓存的Map视图。
         */
        Map<String, ActionCacheEntry> toMap() {
            Map<String, ActionCacheEntry> cache = new LinkedHashMap<>(matchedPathActionCache);
            cache.putAll(fullPathActionCache);
            return cache;
        }
    }

    /**
     * 缓存对象。
     */
    private static class ActionCacheEntry {

        /**
         * Action的代理对象。
         */
        PathActionProxy actionProxy;

        /**
         * Action路径的参数匹配映射，如果没有则为 null
         */
        Map<String, String> matchParameters;

        /**
         * 构造一个无属性的缓存对象。
         */
        ActionCacheEntry() {
        }

        /**
         * 构造一个指定Action的代理对象和Action路径的参数匹配映射的缓存对象。
         *
         * @param actionProxy Action的代理对象。
         * @param matchParameters Action路径的参数匹配映射。
         */
        ActionCacheEntry(PathActionProxy actionProxy, Map<String, String> matchParameters) {
            this.actionProxy = actionProxy;
            this.matchParameters = matchParameters;
        }
    }

    /**
     * 默认空Action的实现。
     */
    private static final Action EMPTY_ACTION = new Action() {

        @Override
        public String[] value() {
            return CollectionUtil.EMPTY_STRING_ARRAY;
        }

        @Override
        public String[] name() {
            return CollectionUtil.EMPTY_STRING_ARRAY;
        }

        @Override
        public String interceptorStack() {
            return "";
        }

        @Override
        public String[] interceptors() {
            return CollectionUtil.EMPTY_STRING_ARRAY;
        }

        @Override
        public Result[] results() {
            return new Result[0];
        }

        @Override
        public Scope scope() {
            return Scope.SINGLETON;
        }

        @Override
        public Parameter[] parameters() {
            return new Parameter[0];
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Action.class;
        }

    };

}
