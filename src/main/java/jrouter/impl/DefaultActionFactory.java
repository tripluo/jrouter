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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import jrouter.*;
import jrouter.annotation.*;
import jrouter.bytecode.javassist.JavassistProxyFactory;
import jrouter.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认<code>ActionFactory</code>的实现类，以‘/’作为路径的分隔符。
 *
 * <p>
 * DefaultActionFactory中的加载和调用的拦截器、结果类型对象均为单例；
 * {@link Action}对象根据其{@link Action#scope()}属性判断是否单例。
 * </p>
 * <p>
 * DefaultActionFactory中的{@link Action}、{@link Interceptor}拦截器、{@link InterceptorStack}拦截栈、
 * {@link ResultType}结果类型、{@link Result}结果对象及的集合在初始化时加载完成，
 * 之后任何情况下DefaultActionFactory不再执行集合的修改和删除操作。对返回集合进行的修改和删除需自行保证其线程安全性。
 * </p>
 */
public class DefaultActionFactory implements ActionFactory {

    /** 日志 */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultActionFactory.class);

    /**
     * 路径分隔符
     */
    private char pathSeparator = PathTree.PATH_SEPARATOR;

    /**
     * 全匹配标识
     */
    private final String match = PathTree.SINGLE_MATCH;

    /**
     * 路径后缀，默认为'.'；非空时截断路径后缀
     */
    private String extension = ".";

    /**
     * Action运行时上下文的类型，用于传递上下文参数时的界限判断。未指定参数类型即null时表示允许ActionInvocation类型的任意子类。
     */
    private Class<? extends ActionInvocation> actionInvocationClass;

    /**
     * 默认拦截栈名称。作用于初始化Action时的配置。
     *
     * @see #createActionProxy(Method, Object)
     */
    private String defaultInterceptorStack = null;

    /**
     * 默认视图类型。作用于ResultType的调用。
     *
     * @see #invokeAction(String, Object[])
     */
    private String defaultResultType = null;

    /**
     * {@link #actionCache}最大缓存数目，默认最大缓存1w条记录；缓存数目小于0则无缓存。
     */
    private int actionCacheNumber = 10000;

    /**
     * 创建对象的工厂对象。
     */
    private ObjectFactory objectFactory;

    /**
     * 生成底层方法代理的工厂对象。
     */
    private ProxyFactory proxyFactory = null;
////////////////////////////////////////////////////////////////////////////////

    /**
     * 拦截器
     */
    private Map<String, InterceptorProxy> interceptors;

    /**
     * 拦截栈
     */
    private Map<String, InterceptorStackProxy> interceptorStacks;

    /**
     * 实际的Action树结构路径映射
     */
    private PathTreeMap<DefaultActionProxy> actions;

    /**
     * Action路径与代理对象的映射缓存.
     */
    private Map<String, ActionCacheEntry> actionCache;

    /**
     * 结果类型
     */
    private Map<String, ResultTypeProxy> resultTypes;

    /**
     * 默认的全局结果对象集合
     */
    private Map<String, ResultProxy> results;

    /**
     * 根据指定的键值映射构造初始化数据的DefaultActionFactory对象。
     *
     * @param properties 指定的初始化数据键值映射。
     */
    public DefaultActionFactory(Map<String, Object> properties) {
        //initiate properties
        setActionFactoryProperties(properties);

        interceptors = new HashMap<String, InterceptorProxy>();
        interceptorStacks = new HashMap<String, InterceptorStackProxy>();

        actions = new PathTreeMap<DefaultActionProxy>(pathSeparator);
        actionCache = Collections.synchronizedMap(new jrouter.util.LRUMap<String, ActionCacheEntry>(actionCacheNumber));

        resultTypes = new HashMap<String, ResultTypeProxy>();
        results = new HashMap<String, ResultProxy>();
    }

    /**
     * 构造DefaultActionFactory并初始化数据。
     */
    public DefaultActionFactory() {
        this(Collections.EMPTY_MAP);
    }

    /**
     * 设置ActionFactory初始化属性值。
     *
     * @param properties 属性值键值映射。
     */
    private void setActionFactoryProperties(Map<String, Object> properties) {
        boolean setBytecode = false;
        for (Map.Entry<String, Object> e : properties.entrySet()) {
            String name = e.getKey();
            Object value = e.getValue();
            if (value == null) {
                LOG.warn("Property \"{}\" can't be null.", name);
                continue;
            }
            //string value
            String strValue = value.toString().trim();
            if ("actionInvocationClass".equalsIgnoreCase(name)) {
                try {
                    //设置Action运行时上下文的类型
                    actionInvocationClass = value instanceof Class
                            ? (Class) value
                            : (Class<? extends ActionInvocation>) ClassUtil.loadClass(strValue);
                } catch (ClassNotFoundException ex) {
                    LOG.error("Can't set ActionInvocationClass of class : " + strValue);
                    throw new JRouterException(ex);
                }
            } else if ("defaultInterceptorStack".equalsIgnoreCase(name)) {
                //设置默认拦截栈名称
                this.defaultInterceptorStack = strValue;
                LOG.info("Set defaultInterceptorStack : " + defaultInterceptorStack);
            } else if ("defaultResultType".equalsIgnoreCase(name)) {
                //设置默认结果视图类型
                this.defaultResultType = strValue;
                LOG.info("Set defaultResultType : " + defaultResultType);
            } else if ("pathSeparator".equalsIgnoreCase(name)) {
                if (StringUtil.isNotBlank(strValue)) {
                    pathSeparator = strValue.charAt(0);
                    LOG.info("Set pathSeparator : " + this.pathSeparator);
                }
            } else if ("extension".equalsIgnoreCase(name)) {
                //设置路径后缀名称，不为null，可设置为空串
                this.extension = strValue;
                LOG.info("Set extension : " + this.extension);
            } else if ("actionCacheNumber".equalsIgnoreCase(name)) {
                actionCacheNumber = Integer.parseInt(strValue);
                LOG.info("Set actionCacheNumber : " + this.actionCacheNumber);
            } else if ("objectFactory".equalsIgnoreCase(name)) {
                //设置创建对象的工厂对象
                this.objectFactory = (ObjectFactory) value;
                LOG.info("Set objectFactory : " + this.objectFactory);
            } else if ("bytecode".equalsIgnoreCase(name)) {
                setBytecode = true;
                if (value instanceof String) {
                    //default to use java reflect directly
                    if ("default".equalsIgnoreCase(strValue)) {
                        proxyFactory = null;
                        LOG.info("Set proxyFactory : " + strValue);
                    } else if ("javassist".equalsIgnoreCase(strValue)) {
                        proxyFactory = new JavassistProxyFactory();
                        LOG.info("Set proxyFactory : " + this.proxyFactory);
                    } else {
                        setBytecode = false;
                        LOG.info("Unknown bytecode property : " + strValue);
                    }
                } else {
                    proxyFactory = (ProxyFactory) value;
                    LOG.info("Set proxyFactory : " + this.proxyFactory);
                }
            } else {
                LOG.warn("Ignore unknown property \"{}\" : {}", name, value);
            }
        }
        //default objectFactory
        setDefaultObjectFactory();
        //default proxyFactory
        if (!setBytecode)
            setDefaultProxyFactory();
    }

    /**
     * 当objectFactory对象为空时，设置其值。
     * 默认提供DefaultObjectFactory对象。
     */
    private void setDefaultObjectFactory() {
        if (objectFactory == null) {
            objectFactory = new DefaultObjectFactory();
            LOG.info("No objectFactory setting, use default : " + objectFactory);
        }
    }

    /**
     * 当proxyFactory对象为空时，设置其值。
     * 默认当引入了javassist时提供JavassistProxyFactory对象；若无javassist引用则默认采用java反射机制。
     */
    private void setDefaultProxyFactory() {
        if (proxyFactory == null) {
            try {
                //check javassist jar
                ClassUtil.loadClass("javassist.ClassPool");
                proxyFactory = new JavassistProxyFactory();
                LOG.info("No proxyFactory setting, use javassist as default : " + proxyFactory);
            } catch (ClassNotFoundException ex) {
                LOG.info("No proxyFactory setting and no javassist jar found, use java reflect as default");
            }
        }
    }

    /**
     * 通过路径调用相应的Action，可传递Action底层方法相应的参数。
     * Action调用是否为线程安全取决于路径所映射底层方法的线程安全性。
     *
     * @param path Action的映射路径。
     * @param params Action的调用参数。
     *
     * @return 调用后的结果；如果结果为字符串类型非空且存在结果对象，则查找相应的结果类型并返回调用后的结果；反之默认直接返回结果。
     *
     * @throws JRouterException 如果发生调用错误。
     *
     * @see #invokeUndefinedResult
     * @see #invokeObjectResult
     */
    @Override
    public Object invokeAction(String path, Object... params) throws JRouterException {
        LOG.debug("Start invoking Action [{}]; Parameters {} ", path, java.util.Arrays.toString(params));
        //remove the extension
        //当后缀为单个字符时，按路径最后出现分割符的位置截断路径后缀；当后缀为未空字符串时，如果路径已后缀结尾，截断后缀。
        if (StringUtil.isNotEmpty(extension)) {
            int len = extension.length();
            //extension为特定的标记字符，则截去标记字符后的部分
            if (len == 1 && !Character.isLetterOrDigit(extension.charAt(0))) {
                int index = path.lastIndexOf(extension.charAt(0));
                if (index != -1) {
                    path = path.substring(0, index);
                }
            } else {
                //extension为特定的后缀字符串
                if (path.endsWith(extension)) {
                    //如果extension前一位非字母或数字
                    if (!Character.isLetterOrDigit(path.charAt(path.length() - len - 1))) {
                        len++;
                    }
                    path = path.substring(0, path.length() - len);
                }
            }
        }

        //create ActionInvocation
        ActionInvocation<?> invocation = createActionInvocation(path, params);

        //invoke
        Object res = null;
        try {
            res = invocation.invoke(params);
            LOG.debug("Get invoked Action [{}] result : [{}]", path, res);
            //result is string
            if (res instanceof String) {
                String resInfo = res.toString();
                ActionProxy ap = invocation.getActionProxy();
                Result result = null;

                //如果action中存在相应的结果映射
                if ((result = ap.getResults().get(resInfo)) != null) {
                    //调用结果对象相应的结果类型
                    Object rr = invokeByResult(invocation, result);
                    if (rr != null)
                        res = rr;
                } //如果Action调用结果的路径信息中包含':'可省略Action中的@Result(name = "*"...)
                else if ((result = ap.getResults().get(match)) != null || resInfo.indexOf(':') != -1) {
                    //非完全匹配字符串路径的调用
                    Object rr = invokeByString(invocation, result, resInfo);
                    if (rr != null)
                        res = rr;
                } //如果全局结果对象集合中存在相应的结果映射
                else if (results.containsKey(resInfo)) {
                    ResultProxy rp = results.get(resInfo);
                    //ResultProxy直接调用
                    Object rr = rp.invoke(rp.isRequireAction() ? invocation : null);
                    if (rr != null)
                        res = rr;

                    result = rp.getResult();
                    //当Result的type值不为空时，执行相应的ResultType
                    if (StringUtil.isNotEmpty(result.type())) {
                        rr = invokeByResult(invocation, result);
                        if (rr != null)
                            res = rr;
                    }
                } else {
                    //若无匹配
                    Object rr = invokeUndefinedResult(invocation, resInfo);
                    if (rr != null)
                        res = rr;
                }
            } else {
                //非字符串结果的对象处理方式
                Object rr = invokeObjectResult(invocation, res);
                if (rr != null)
                    res = rr;
            }
        } catch (InvocationProxyException e) {
            //去除不必要的InvocationProxyException异常，封装异常的源并抛出。
            throw e.getSourceInvocationException();
        }
        LOG.debug("Finish invoking Action [{}]; Parameters {}; Final result : [{}]",
                path, java.util.Arrays.toString(params), String.valueOf(res));
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
    protected ActionInvocation<?> createActionInvocation(String path, Object... params) {
        //cache
        ActionCacheEntry ace = null;
        if (actionCacheNumber > 0)
            ace = actionCache.get(path);

        //Action代理对象
        DefaultActionProxy ap = null;
        //路径的参数匹配映射
        Map<String, String> matchParameters = null;

        //get from cache
        if (ace != null) {
            ap = ace.actionProxy;
            matchParameters = ace.matchParameters;
        } else {
            //initiate matchParameters
            matchParameters = new HashMap<String, String>(2);
            //get Action and fill matchParameters
            ap = actions.get(path, matchParameters);

            if (ap == null)
                throw new JRouterException("No such Action : " + path);

            //put in cache
            ace = new ActionCacheEntry(ap, Collections.unmodifiableMap(matchParameters));
            putActionCache(path, ace);
        }

        //scope action
        ap = ap.getInstance();
        //TODO actionInvocationClass ???
        //create DefaultActionInvocation
        DefaultActionInvocation ai = new DefaultActionInvocation(this, ap, params);
        //setActionPathParameters
        ai.setActionPathParameters(matchParameters);
        return ai;
    }

    /**
     * 调用Result相应的ResultType。
     *
     * @param invocation Action运行时上下文。
     * @param result 结果对象。
     *
     * @return 调用ResultType后的结果。
     */
    private Object invokeByResult(ActionInvocation<?> invocation, Result result) {
        String type = result.type();
        //default result type
        if (StringUtil.isEmpty(type))
            type = defaultResultType;

        ResultTypeProxy rtp = resultTypes.get(type);
        if (rtp == null)
            throw new JRouterException("No such ResultType [" + type + "] at : "
                    + invocation.getActionProxy().getMethod());

        if (rtp.isRequireAction()) {
            ((DefaultActionInvocation) invocation).setResult(result);
        }
        LOG.debug("Invoke ResultType [{}] at : {}", type, rtp.getMethod());
        //结果类型调用
        return rtp.invoke(rtp.isRequireAction() ? invocation : null);
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
     * @see #invokeByResult
     */
    private Object invokeByString(ActionInvocation<?> invocation, Result result, String pathinfo) {
        //default values
        String type = defaultResultType;
        String loc = null;
        if (result != null) {
            if (StringUtil.isNotEmpty(result.type()))
                type = result.type();
            loc = result.location();
        }
        //parse the string "type:location"
        String[] parseRes = parseMatch(pathinfo, type, loc);
        type = parseRes[0];
        loc = parseRes[1];
        //重新封装result参数
        Result res = new DefaultActionInvocation.ResultProxy(match, type, loc);
        return invokeByResult(invocation, res);
    }

    /**
     * "type:location"形式的字符串解析。
     *
     * @param pathinfo "type:location"形式的字符串。
     * @param def type和location的默认值。
     *
     * @return 解析后的{type,location}字符串数组。
     */
    private static String[] parseMatch(String pathinfo, String... def) {
        String type = def[0];
        String loc = def[1];
        //首個非空格非分隔符的位置
        int begin = -1;
        //分隔符前的最后一个非空格字符的位置
        int p1 = -1;
        //分隔符的位置
        int colon = -1;
        int len = pathinfo.length();
        //定位分隔符位置
        for (int i = 0; i < len; i++) {
            char c = pathinfo.charAt(i);
            //may extend
            if (c == ':') {
                colon = i;
                break;
            } else {
                if (c != ' ') {
                    p1 = i;
                    if (begin == -1)
                        begin = i;
                }
            }
        }
        //判断结果类型
        switch (colon) {
            case -1:
                begin = 0;
                p1 = len - 1;
                while (begin < p1 && pathinfo.charAt(begin) == ' ')
                    begin++;
                while (begin < p1 && pathinfo.charAt(p1) == ' ')
                    p1--;
                //非空
                if (begin < p1)
                    type = pathinfo.substring(begin, p1 + 1);
                break;
            case 0:
                break;
            default:
                //非空
                if (begin != -1)
                    type = pathinfo.substring(begin, p1 + 1);
        }
        //分隔符非末位，判断结果路径
        if (colon != -1 && colon != len - 1) {
            //分隔符后的第一个非空格字符的位置
            int p2 = colon + 1;
            //最后一个非空字符的位置
            int end = len - 1;
            while (p2 < end && pathinfo.charAt(p2) == ' ')
                p2++;
            while (p2 < end && pathinfo.charAt(end) == ' ')
                end--;

            if (p2 != end)
                loc = pathinfo.substring(p2, end + 1);
        }
        //返回0.type 1.location形式的数组
        return new String[]{type, loc};
    }

    /**
     * 用于子类继承，提供非字符串<code>string</code>对象的处理方式。
     * 默认直接返回非字符串对象，void方法返回 null 。
     *
     * @param invocation Action运行时上下文。
     * @param res Action调用完成后的结果。
     *
     * @return 非字符串对象。
     */
    protected Object invokeObjectResult(ActionInvocation<?> invocation, Object res) {
        LOG.warn("Invoking Object Result [{}] and return directly at : {}", res, invocation.getActionProxy().getMethod());
        return res;
    }

    /**
     * 用于子类继承，提供Action和全局结果对象集合均未匹配<code>string</code>结果字符串对象情况下的处理方式。
     * 默认直接返回结果字符串。
     *
     * @param invocation Action运行时上下文。
     * @param resInfo Action调用完成后的结果字符串。
     *
     * @return 结果字符串。
     */
    protected Object invokeUndefinedResult(ActionInvocation<?> invocation, String resInfo) {
        LOG.warn("Invoking undefined String Result [{}] at {}, return string directly", resInfo, invocation.getActionProxy().getMethod());
        //throw new JRouterException("No match Result [" + resInfo + "] at " + ap.getMethod(), ap);
        //不作处理直接跳过，直接返回调用结果字符串
        return resInfo;
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
            actionCache.put(path, ace);
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
        LOG.info("Clearing JRouter ActionFactory : " + this);
        actionCache.clear();
        actions.clear();
        interceptorStacks.clear();
        interceptors.clear();
        resultTypes.clear();
        results.clear();
        Injector.clear();
    }
////////////////////////////////////////////////////////////////////////////////

    /**
     * 添加拦截器。
     *
     * @param ip 拦截器代理对象。
     */
    public void addInterceptor(InterceptorProxy ip) {
        String name = ip.getName();

        if (StringUtil.isBlank(name))
            throw new IllegalArgumentException("Null name of Interceptor : " + ip.getMethod());

        if (interceptors.containsKey(name)) {
            throw new JRouterException("Duplicate Interceptor [" + name + "] : "
                    + ip.getMethod() + " override "
                    + interceptors.get(name).getMethod());
        } else {
            LOG.info("Add Interceptor [{}] at : {} ", name, ip.getMethod());
        }
        interceptors.put(name, ip);
    }

    /**
     * 添加拦截器。
     *
     * @param obj 包含{@link Interceptor}注解的类或实例对象。
     *
     * @see jrouter.annotation.Interceptor
     */
    public void addInterceptors(Object obj) {
        boolean isCls = obj instanceof Class;
        Class<?> cls = isCls ? (Class) obj : obj.getClass();
        Object invoker = isCls ? null : obj;
        Method[] ms = cls.getDeclaredMethods();
        for (Method m : ms) {
            int mod = m.getModifiers();
            //带@Interceptor的public/protected方法
            if ((Modifier.isPublic(mod) || Modifier.isProtected(mod))
                    && m.isAnnotationPresent(Interceptor.class)) {
                if (m.isAnnotationPresent(Ignore.class)) {
                    LOG.info("Ignore Interceptor : " + MethodUtil.getMethod(m));
                    continue;
                }
                m.setAccessible(true);
                //static method
                if (Modifier.isStatic(mod)) {
                    addInterceptor(createInterceptorProxy(m, null));
                } else {
                    //为类对象且调用者为 null
                    if (isCls && invoker == null) {
                        invoker = objectFactory.newInstance(cls);
                    }
                    //the same object
                    addInterceptor(createInterceptorProxy(m, invoker));
                }
            }
        }
    }

    /**
     * 添加拦截栈。
     *
     * @param isp 拦截栈代理对象。
     */
    public void addInterceptorStack(InterceptorStackProxy isp) {
        String name = isp.getName();
        if (StringUtil.isBlank(name))
            throw new IllegalArgumentException("Null name of InterceptorStack : " + isp.getFieldName());

        if (interceptorStacks.containsKey(name)) {
            throw new JRouterException("Duplicate InterceptorStack [" + name + "] : "
                    + isp.getFieldName() + " override "
                    + interceptorStacks.get(name).getFieldName());
        } else {
            LOG.info("Add InterceptorStack [{}] : {}", name, isp);
        }
        interceptorStacks.put(name, isp);
    }

    /**
     * 添加拦截栈。
     *
     * @param obj 包含{@link InterceptorStack}注解的类或实例对象。
     *
     * @see jrouter.annotation.InterceptorStack
     */
    public void addInterceptorStacks(Object obj) {
        boolean isCls = obj instanceof Class;
        Class<?> cls = isCls ? (Class) obj : obj.getClass();
        Object invoker = isCls ? null : obj;
        Field[] fs = cls.getDeclaredFields();
        //TODO 是否成员变量
        for (Field f : fs) {
            int mod = f.getModifiers();
            //带@InterceptorStack的public属性
            if (Modifier.isPublic(mod) && f.isAnnotationPresent(InterceptorStack.class)) {
                f.setAccessible(true);
                try {
                    //static field
                    if (Modifier.isStatic(mod)) {
                        addInterceptorStack(createInterceptorStackProxy(f, null));
                    } else {
                        //为类对象且调用者为 null
                        if (isCls && invoker == null) {
                            invoker = objectFactory.newInstance(cls);
                        }
                        //the same object
                        addInterceptorStack(createInterceptorStackProxy(f, invoker));
                    }
                } catch (IllegalAccessException e) {
                    throw new JRouterException(e);
                }
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////
    /**
     * 添加结果类型。
     *
     * @param rtp 结果类型的代理对象。
     */
    public void addResultType(ResultTypeProxy rtp) {
        String type = rtp.getType();
        if (StringUtil.isBlank(type))
            throw new IllegalArgumentException("Null type of ResultType : " + rtp.getMethod());

        if (resultTypes.containsKey(type)) {
            throw new JRouterException("Duplicate ResultType [" + type + "] : "
                    + rtp.getMethod() + " override "
                    + resultTypes.get(type).getMethod());
        } else {
            LOG.info("Add ResultType [{}] at : {}", type, rtp.getMethod());
        }
        resultTypes.put(type, rtp);
    }

    /**
     * 添加结果类型。
     *
     * @param obj 包含{@link ResultType}注解的类或实例对象。
     *
     * @see jrouter.annotation.ResultType
     */
    public void addResultTypes(Object obj) {
        boolean isCls = obj instanceof Class;
        Class<?> cls = isCls ? (Class) obj : obj.getClass();
        Object invoker = isCls ? null : obj;
        Method[] ms = cls.getDeclaredMethods();
        for (Method m : ms) {
            int mod = m.getModifiers();
            //带@ResultType的public/protected方法
            if ((Modifier.isPublic(mod) || Modifier.isProtected(mod))
                    && m.isAnnotationPresent(ResultType.class)) {
                if (m.isAnnotationPresent(Ignore.class)) {
                    LOG.info("Ignore ResultType : " + MethodUtil.getMethod(m));
                    continue;
                }
                m.setAccessible(true);
                //static method
                if (Modifier.isStatic(mod)) {
                    addResultType(createResultTypeProxy(m, null));
                } else {
                    //为类对象且调用者为 null
                    if (isCls && invoker == null) {
                        invoker = objectFactory.newInstance(cls);
                    }
                    //the same object
                    addResultType(createResultTypeProxy(m, invoker));
                }
            }
        }
    }

    /**
     * 添加结果对象。
     *
     * @param rp 结果对象的代理对象。
     */
    public void addResult(ResultProxy rp) {
        String name = rp.getResult().name();
        if (StringUtil.isBlank(name))
            throw new IllegalArgumentException("Null name of Result : " + rp.getMethod());

        if (results.containsKey(name)) {
            throw new JRouterException("Duplicate Result [" + name + "] : "
                    + rp.getMethod() + " override "
                    + results.get(name).getMethod());
        } else {
            LOG.info("Add Result [{}] : {}", name, rp.getMethod());
        }
        results.put(name, rp);
    }

    /**
     * 添加全局结果对象。
     *
     * @param obj 包含{@link Result}注解的类或实例对象。
     *
     * @see jrouter.annotation.Result
     */
    public void addResults(Object obj) {
        boolean isCls = obj instanceof Class;
        Class<?> cls = isCls ? (Class) obj : obj.getClass();
        Object invoker = isCls ? null : obj;
        Method[] ms = cls.getDeclaredMethods();
        for (Method m : ms) {
            int mod = m.getModifiers();
            //带@Result的public/protected方法
            if ((Modifier.isPublic(mod) || Modifier.isProtected(mod))
                    && m.isAnnotationPresent(Result.class)) {
                if (m.isAnnotationPresent(Ignore.class)) {
                    LOG.info("Ignore Result : " + MethodUtil.getMethod(m));
                    continue;
                }
                m.setAccessible(true);
                //static method
                if (Modifier.isStatic(mod)) {
                    addResult(createResultProxy(m, null));
                } else {
                    //为类对象且调用者为 null
                    if (isCls && invoker == null) {
                        invoker = objectFactory.newInstance(cls);
                    }
                    //the same object
                    addResult(createResultProxy(m, invoker));
                }
            }
        }
    }
/////////////////////////////////////////////////////////////////////////////////

    /**
     * 添加Action。
     *
     * @param ap Action代理对象。
     */
    public void addAction(DefaultActionProxy ap) {
        String aPath = ap.getPath();

        if (StringUtil.isBlank(aPath))
            throw new IllegalArgumentException("Null path of Action : " + ap.getMethod());

        //可能存在模糊匹配 或者 完全相等的路径
        DefaultActionProxy exist = actions.get(aPath);
        //模糊匹配添加新值，完全相等的路径则特换原路径的值
        actions.put(aPath, ap);
        //添加或替换后查询新路径的值
        DefaultActionProxy newAction = actions.get(aPath);

        if (exist != null) {
            //新增与原有完全相等的路径
            if (exist.getPath().equals(newAction.getPath())) {
                throw new JRouterException("Duplicate path Action [" + aPath + "] : "
                        + ap.getMethod() + " override "
                        + exist.getMethod());
            } //新增与原有相同的匹配路径，考虑匹配级别是否相同???
            //TODO
            //            else if (0 == PathTree.compareMathedPath(newAction.getPath(), exist.getPath())) {
            //                throw new JRouterException("Duplicate matched path Action [" + aPath + "] : "
            //                        + ap.getMethod() + " override "
            //                        + exist.getMethod());
            //            }
            //原有路径模糊匹配，继续添加新路径；或反之
            else {
                LOG.warn("Exist matched path [{}] : {}, add [{}] : {}",
                        exist.getPath(), exist.getMethod(), aPath, ap.getMethod());
            }
        } else {
            LOG.info("Add Action [{}] at : {}", aPath, ap.getMethod());
        }
    }

    /**
     * 添加Action。
     *
     * @param obj 包含{@link Action}注解的类或实例对象。
     *
     * @see jrouter.annotation.Action
     */
    public void addActions(Object obj) {
        //判断传入参数为类或实例对象
        boolean isCls = obj instanceof Class;
        Class<?> cls = isCls ? (Class) obj : obj.getClass();
        Object invoker = isCls ? null : obj;

        //declared methods
        Method[] ms = cls.getDeclaredMethods();
        List<Method> methods = new ArrayList<Method>(ms.length);
        Namespace ns = cls.getAnnotation(Namespace.class);
        if (ns != null && ns.autoIncluded()) {
            for (Method m : ms) {
                int mod = m.getModifiers();
                //全部public/protected方法
                if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) {
                    methods.add(m);
                }
            }
        } //no @Namespace
        else {
            for (Method m : ms) {
                //指定带@Action方法
                if (m.isAnnotationPresent(Action.class))
                    methods.add(m);
            }
        }

        for (Method m : methods) {
            int mod = m.getModifiers();
            //带@Action的public/protected方法
//            if ((Modifier.isPublic(mod) || Modifier.isProtected(mod))
//                    && m.isAnnotationPresent(Action.class)) {
            if (m.isAnnotationPresent(Ignore.class)) {
                LOG.info("Ignore Action : " + MethodUtil.getMethod(m));
                continue;
            }
            m.setAccessible(true);
            try {
                //static method
                if (Modifier.isStatic(mod)) {
                    addAction(createActionProxy(m, null));
                } else {
                    if (isCls && invoker == null) {
                        invoker = objectFactory.newInstance(cls);
                    }
                    //the same object
                    addAction(createActionProxy(m, invoker));
                }
            } catch (IllegalAccessException e) {
                throw new JRouterException(e);
            } catch (InvocationTargetException e) {
                throw new JRouterException(e);
            }
//            }
        }
    }

////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建Interceptor代理对象。
     *
     * @param method 指定的方法。
     * @param obj 方法所在的对象。
     *
     * @return Interceptor代理对象。
     */
    private InterceptorProxy createInterceptorProxy(Method method, Object obj) {
        Interceptor interceptor = method.getAnnotation(Interceptor.class);
        Class[] params = method.getParameterTypes();

        //check interceptor invoke args
        boolean requireAction = false;
        //interceptor代理的方法无参数或参数仅为ActionInvocation或其子类
        if (params.length == 0) {
            requireAction = false;
        } //未指定actionInvocationClass参数类型时允许ActionInvocation任意子类；否则为actionInvocationClass的父类
        else if (isProxyMethod(params)) {
            requireAction = true;
        } else {
            throw new IllegalArgumentException("Illegal arguments in Interceptor : " + method);
        }
        return new InterceptorProxy(this, interceptor, method, obj, requireAction);
    }

    /**
     * 创建InterceptorStack代理对象
     *
     * @param field 指定的字段。
     * @param obj 字段所在对象。
     *
     * @return InterceptorStack代理对象。
     *
     * @throws IllegalAccessException 如果调用的对象无法访问指定字段。
     */
    private InterceptorStackProxy createInterceptorStackProxy(Field field, Object obj) throws
            IllegalAccessException {
        InterceptorStack interceptorStack = field.getAnnotation(InterceptorStack.class);
        String name = interceptorStack.name().trim();

        //interceptorStack name
        //未指定拦截栈名称则取字符串的值为名称
        if (StringUtil.isEmpty(name)) {
            name = field.get(obj).toString();
            //空命名异常
            if (StringUtil.isEmpty(name))
                throw new IllegalArgumentException("Null name of InterceptorStack : "
                        + field.getName() + " at " + obj.getClass());
        }
        //interceptors name
        String[] names = interceptorStack.interceptors();

        List<InterceptorProxy> list = null;
        if (names != null) {
            list = new ArrayList<InterceptorProxy>(names.length);
            //add interceptorStack
            //for (int i = names.length - 1; i >= 0; i--) {
            for (int i = 0; i < names.length; i++) {
                InterceptorProxy ip = interceptors.get(names[i]);
                //if null
                if (ip == null) {
                    LOG.warn("No such Interceptor [{}] at : {}", names[i], field);
                } else {
                    list.add(ip);
                }
            }
        }
        return new InterceptorStackProxy(name, field, list);
    }

    /**
     * 创建ResultType代理对象。
     *
     * @param method 指定的方法。
     * @param obj 方法所在的对象。
     *
     * @return ResultType代理对象。
     */
    private ResultTypeProxy createResultTypeProxy(Method method, Object obj) {
        ResultType resultType = method.getAnnotation(ResultType.class);
        Class[] params = method.getParameterTypes();

        boolean requireAction = false;
        //ResultType代理的方法无参数或参数仅为ActionInvocation或其子类
        if (params.length == 0) {
            requireAction = false;
        } else if (isProxyMethod(params)) {
            requireAction = true;
        } else {
            throw new IllegalArgumentException("Illegal arguments in ResultType : " + method);
        }

        return new ResultTypeProxy(this, resultType, method, obj, requireAction);
    }

    /**
     * 创建Result代理对象。
     *
     * @param method 指定的方法。
     * @param obj 方法所在的对象。
     *
     * @return Result代理对象
     */
    private ResultProxy createResultProxy(Method method, Object obj) {
        Result res = method.getAnnotation(Result.class);
        Class[] params = method.getParameterTypes();

        boolean requireAction = false;
        //ResultType代理的方法无参数或参数仅为ActionInvocation或其子类
        if (params.length == 0) {
            requireAction = false;
        } else if (isProxyMethod(params)) {
            requireAction = true;
        } else {
            throw new IllegalArgumentException("Illegal arguments in Result : " + method);
        }
        return new ResultProxy(this, res, method, obj, requireAction);
    }

    /**
     * 判断代理方法的参数是否合法；仅允许无参数方法或仅ActionInvocation类型参数的方法。未指定参数类型时允许ActionInvocation类型的任意子类；否则为{@link #actionInvocationClass}的父类。
     *
     * @param params 代理方法的参数类型。
     *
     * @return 所代理的方法合法返回true，否则为false。
     */
    private boolean isProxyMethod(Class[] params) {
        if (params.length > 1)
            return false;
        return params.length == 0 ? true : actionInvocationClass == null
                ? ActionInvocation.class.isAssignableFrom(params[0])
                : params[0].isAssignableFrom(actionInvocationClass);
    }

    /**
     * 创建Action代理对象。
     *
     * @param method 指定的方法。
     * @param obj 方法所在的对象。
     *
     * @return Action代理对象。
     */
    private DefaultActionProxy createActionProxy(Method method, Object obj) throws
            IllegalAccessException, InvocationTargetException {
        Namespace ns = method.getDeclaringClass().getAnnotation(Namespace.class);
        //trim empay and '/'
        String namespace = ns == null ? pathSeparator + "" : pathSeparator + StringUtil.trim(ns.name(), pathSeparator);

        String path = null;
        //not nullable Action
        Action action = method.getAnnotation(Action.class);
        //如果Action为null
        if (action == null) {
            action = EMPTY_ACTION;
        }
        //Action名称为空字符串
        String aname = action.name().trim();
        if ("".equals(aname)) {
            //Action名称为空字符串时取其方法的名称（区分大小写）
            aname = method.getName();
            //if namespace is '/' or not
            path = namespace.length() == 1 ? pathSeparator + aname : namespace + pathSeparator + aname;
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
                path = namespace.length() == 1 ? pathSeparator + name : namespace + pathSeparator + name;
            }
        }

        //包含指定path的属性注入，其Action需重新生成对象
        if (obj != null && Injector.actionInjection.containsKey(path)) {
            obj = objectFactory.newInstance(obj.getClass());
            Injector.injectAction(path, obj);
        }

        //Action中不记录路径的后缀名称
        DefaultActionProxy ap = new DefaultActionProxy(this, namespace, path, action, method, obj);

        //void method
        if (void.class == method.getReturnType())
            LOG.warn("Mapping [{}] void method at : {}", ap.getPath(), ap.getMethod());

        //interceptorStack
        String stackName = action.interceptorStack().trim();
        //not not nullable action's interceptors
        String[] interceptorNames = action.interceptors();

        List<InterceptorProxy> inters = new ArrayList<InterceptorProxy>(5);
        //action interceptors
        if (interceptorNames.length != 0) {
            ////action interceptorStack
            if (StringUtil.isNotEmpty(stackName)) {
                addActionInterceptors(inters, stackName, ap);
            }
            //action中申明的interceptors
            for (String name : action.interceptors()) {
                InterceptorProxy ip = interceptors.get(name);
                if (ip == null) {
                    LOG.warn("No such Interceptor [{}] at : {}", name, ap.getMethod());
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
                        InterceptorProxy ip = interceptors.get(name);
                        if (ip == null) {
                            LOG.warn("No such Interceptor [{}] at : {}", name, ap.getMethod());
                        } else {
                            inters.add(ip);
                        }
                    }
                }
            }
            //defaultInterceptorStack
            if (!setInterceptors) {
                if (StringUtil.isNotEmpty(stackName = defaultInterceptorStack)) {
                    addActionInterceptors(inters, stackName, ap);
                }
            }
        }
        //trim
        ((ArrayList) inters).trimToSize();
        ap.setInterceptors(inters);

        //set action parameters
        Parameter[] ps = action.parameters();
        Map<String, String[]> params = new HashMap<String, String[]>(ps.length);
        for (Parameter p : ps) {
            params.put(p.name(), p.value());
        }
        ap.setActionParameters(Collections.unmodifiableMap(params));
        //set results
        Result[] rs = action.results();
        Map<String, Result> res = new HashMap<String, Result>(rs.length);
        for (Result r : rs) {
            res.put(r.name(), r);
        }
        ap.setResults(Collections.unmodifiableMap(res));

        return ap;
    }

    /**
     * 由指定拦截栈名称添加拦截器至Action的拦截器集合。
     *
     * @param interceptors Action的拦截器集合。
     * @param stackName 指定的拦截栈名称。
     * @param ap Action代理对象。
     */
    private void addActionInterceptors(List<InterceptorProxy> interceptors, String stackName,
            DefaultActionProxy ap) {
        InterceptorStackProxy isp = interceptorStacks.get(stackName);
        if (isp == null) {
            LOG.warn("No such InterceptorStack [{}] at : {}", stackName, ap.getMethod());
        } else {
            if (isp.getInterceptors() != null)
                interceptors.addAll(isp.getInterceptors());
        }
    }
////////////////////////////////////////////////////////////////////////////////

    @Override
    public Map<String, DefaultActionProxy> getActions() {
        return actions;
    }

    /**
     * 返回缓存的Action路径与其代理对象的映射。
     *
     * @return 缓存的Action路径与其代理对象的映射。
     */
    public Map<String, Object> getActionCache() {
        return (Map) actionCache;
    }

    @Override
    public Map<String, InterceptorProxy> getInterceptors() {
        return interceptors;
    }

    @Override
    public Map<String, InterceptorStackProxy> getInterceptorStacks() {
        return interceptorStacks;
    }

    @Override
    public Map<String, ResultTypeProxy> getResultTypes() {
        return resultTypes;
    }

    @Override
    public String getDefaultInterceptorStack() {
        return defaultInterceptorStack;
    }

    @Override
    public String getDefaultResultType() {
        return defaultResultType;
    }

    /**
     * 返回全局结果对象集合。
     *
     * @return 全局结果对象集合。
     */
    @Override
    public Map<String, ResultProxy> getResults() {
        return results;
    }

    /**
     * 返回路径后缀分隔符。
     *
     * @return 路径后缀分隔符名称。
     */
    public String getExtension() {
        return extension;
    }

    /**
     * 获取路径分隔符。
     *
     * @return 路径分隔符。
     */
    public char getPathSeparator() {
        return pathSeparator;
    }

    /**
     * Action运行时上下文的类型。
     *
     * @return Action运行时上下文的类型。
     */
    public Class<? extends ActionInvocation> getActionInvocationClass() {
        return actionInvocationClass;
    }

    /**
     * 返回{@link #getActionCache()}的最大缓存数目。
     *
     * @return {@link #getActionCache()}的最大缓存数目。
     */
    public int getActionCacheNumber() {
        return actionCacheNumber;
    }

    @Override
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    @Override
    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    /**
     * 默认创建对象的工厂类。
     */
    private static class DefaultObjectFactory implements ObjectFactory {

        @Override
        public <T> T newInstance(Class<T> clazz) {
            try {
                return clazz.newInstance();
            } catch (IllegalAccessException e) {
                throw new JRouterException(e);
            } catch (InstantiationException e) {
                throw new JRouterException(e);
            }
        }
    }

    /**
     * 缓存对象。
     */
    private static class ActionCacheEntry {

        /**
         * Action的代理对象
         */
        DefaultActionProxy actionProxy;

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
        ActionCacheEntry(DefaultActionProxy actionProxy, Map<String, String> matchParameters) {
            this.actionProxy = actionProxy;
            this.matchParameters = matchParameters;
        }
    }
    /**
     * 默认空Action的实现。
     */
    private static final Action EMPTY_ACTION = new Action() {
        @Override
        public String name() {
            return "";
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
