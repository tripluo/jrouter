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
package jrouter.spring;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import jrouter.ActionFactory;
import jrouter.JRouterException;
import jrouter.ObjectFactory;
import jrouter.config.AopAction;
import jrouter.config.Configuration;
import jrouter.impl.DefaultActionFactory;
import jrouter.util.AntPathMatcher;
import jrouter.util.ClassUtil;
import jrouter.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * 提供与springframework集成的ActionFactory。Action指定path属性的注入由指定springframework的bean完成。
 *
 * @param <T> ActionFactory特定类型。
 */
public class DefaultActionFactoryBean<T extends ActionFactory> implements FactoryBean<T>,
        InitializingBean, DisposableBean, ApplicationContextAware {

    /** LOG */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultActionFactoryBean.class);

    /**
     * Location of a single JRouter XML config file. 不保证ActionFactory属性的重复加载。
     */
    private Resource configLocation;

    /* ActionFactory对象 */
    private T actionFactory;

    /* ActionFactory的类型 */
    private Class<T> actionFactoryClass = null;

    /** Configuration对象 */
    private Configuration configuration;

    /**
     * Configuration对象类型
     *
     * @deprecated since 1.6.4
     */
    @Deprecated
    private Class<? extends Configuration> configurationClass = Configuration.class;

    /** @see ActionFactory#getObjectFactory() */
    private ObjectFactory objectFactory;

    /**
     * @see Configuration#actionFactoryProperties
     * @see jrouter.impl.DefaultActionFactory#setActionFactoryProperties(java.util.Map)
     */
    private Properties actionFactoryProperties = new Properties();

////////////////////////////////////////////////////////////////////////////////////////////////////
    /* 拦截器的bean名称和类名称的集合 */
    private List<Object> interceptors = null;

    /* 拦截栈的bean名称和类名称的集合 */
    private List<Object> interceptorStacks = null;

    /* 结果类型的bean名称和类名称的集合 */
    private List<Object> resultTypes = null;

    /* 结果对象的bean名称和类名称的集合 */
    private List<Object> results = null;

    /* Action的bean名称和类名称的集合 */
    private List<Object> actions = null;

    /**
     * 扫描类工具的顺序配置，依次配置(包名/匹配包含/匹配排除)属性。
     *
     * @see Configuration#parsecComponentClassScanner(java.util.Map)
     */
    private List<Properties> componentClassScanProperties;

    /** actions' aop */
    private List<? extends AopAction> aopActions;
////////////////////////////////////////////////////////////////////////////////////////////////////

    /** 根据匹配的name自动加载Bean，依次配置匹配(包含/排除)的beans' name属性。 */
    private Properties componentBeanScanProperties;

    /** ApplicationContext */
    private ApplicationContext applicationContext;

    /**
     * 初始化ActionFactory。
     *
     * @see #buildActionFactory()
     * @see #afterActionFactoryCreation(jrouter.ActionFactory)
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        actionFactory = (T) buildActionFactory();
        afterActionFactoryCreation(actionFactory);
    }

    /**
     * 由注入的属性创建ActionFactory。
     *
     * @return ActionFactory
     *
     * @throws Exception 如果发生异常。
     */
    protected ActionFactory buildActionFactory() throws Exception {
        LOG.info("Initiating JRouter ActionFactory at : " + new java.util.Date());
        if (configuration == null) {
            configuration = createDefaultConfiguration();
        }
        //不保证ActionFactory属性的重复加载
        if (configLocation != null) {
            LOG.debug("Load configuration : " + configLocation.getURL());
            configuration.load(configLocation.getURL());
        }

        if (actionFactoryClass != null) {
            LOG.debug("Set actionFactoryClass : " + actionFactoryClass);
            configuration.setActionFactoryClass(actionFactoryClass);
        } else {
            setDefaultActionFactoryClass(configuration);
        }
        if (objectFactory == null) {
            objectFactory = createDefaultObjectFactory(configuration);
        }
        actionFactoryProperties.put("objectFactory", objectFactory);
        configuration.addActionFactoryProperties((Map) actionFactoryProperties);

        //添加扫描工具属性
        if (componentClassScanProperties != null)
            configuration.addComponentClassScanProperties(componentClassScanProperties.toArray(new Map[componentClassScanProperties.size()]));

        //convert string to class
        convertList(interceptors, interceptorStacks, resultTypes, results, actions);

        if (CollectionUtil.isNotEmpty(componentBeanScanProperties)) {
            char[] sep = {',', ';'};
            //匹配包含bean名称的表达式
            List<String> includeComponentBeanExpressions = new ArrayList<String>(2);
            CollectionUtil.stringToCollection(componentBeanScanProperties.getProperty("includeComponentBeanExpression"), includeComponentBeanExpressions, sep);
            //匹配排除bean名称的表达式
            List<String> excludeComponentBeanExpressions = new ArrayList<String>(2);
            CollectionUtil.stringToCollection(componentBeanScanProperties.getProperty("excludeComponentBeanExpression"), excludeComponentBeanExpressions, sep);

            //匹配包含class的表达式
            List<String> includeComponentClassExpressions = new ArrayList<String>(2);
            CollectionUtil.stringToCollection(componentBeanScanProperties.getProperty("includeComponentClassExpression"), includeComponentClassExpressions, sep);

            //匹配排除class名称的表达式
            List<String> excludeComponentClassExpressions = new ArrayList<String>(2);
            CollectionUtil.stringToCollection(componentBeanScanProperties.getProperty("excludeComponentClassExpression"), excludeComponentClassExpressions, sep);

            AntPathMatcher matcher = new AntPathMatcher(".");
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            Set<String> includes = new LinkedHashSet<String>();
            out:
            for (String name : beanNames) {
                for (String excludeComponentBeanExpression : excludeComponentBeanExpressions) {
                    if (matcher.match(excludeComponentBeanExpression, name)) {
                        continue out;
                    }
                }
                for (String includeComponentBeanExpression : includeComponentBeanExpressions) {
                    if (matcher.match(includeComponentBeanExpression, name)) {
                        includes.add(name);
                    }
                }
            }
            //add include beans
            out:
            for (String includeName : includes) {
                try {
                    Object bean = applicationContext.getBean(includeName);
                    String clsName = bean.getClass().getName();
                    for (String excludeComponentClassExpression : excludeComponentClassExpressions) {
                        //exclude class
                        if (matcher.match(excludeComponentClassExpression, clsName)) {
                            continue out;
                        }
                    }
                    if (CollectionUtil.isNotEmpty(includeComponentClassExpressions)) {
                        for (String includeComponentClassExpression : includeComponentClassExpressions) {
                            if (matcher.match(includeComponentClassExpression, clsName)) {
                                //only add matched-class bean
                                addComponentToList(bean, interceptors, interceptorStacks, resultTypes, results, actions);
                                continue out;
                            }
                        }
                    } else {
                        //if no includeComponentClassExpression, means allow all
                        addComponentToList(bean, interceptors, interceptorStacks, resultTypes, results, actions);
                    }
                } catch (BeansException e) {
                    //ignore
                    LOG.warn("Can't get bean : " + includeName);
                }
            }
        }

        //set configuration
        if (CollectionUtil.isNotEmpty(interceptors))
            configuration.addInterceptors(interceptors);
        if (CollectionUtil.isNotEmpty(interceptorStacks))
            configuration.addInterceptorStacks(interceptorStacks);
        if (CollectionUtil.isNotEmpty(resultTypes))
            configuration.addResultTypes(resultTypes);
        if (CollectionUtil.isNotEmpty(results))
            configuration.addResults(results);
        if (CollectionUtil.isNotEmpty(actions))
            configuration.addActions(actions);

        //TODO
        //configuration.setPathProperties(null);
        //actions' aop
        if (CollectionUtil.isNotEmpty(aopActions)) {
            configuration.addAopActions(aopActions);
        }
        return configuration.buildActionFactory();
    }

    /**
     * 未直接设置Configuration对象时，提供默认的{@code SpringConfiguration}对象实现。
     *
     * @return {@code SpringConfiguration}对象。
     *
     * @see #setConfiguration(jrouter.config.Configuration)
     */
    protected Configuration createDefaultConfiguration() {
        //create SpringObjectFactory
        return new SpringConfiguration(applicationContext);
    }

    /**
     * 未设置actionFactoryClass属性时，提供默认的{@code DefaultActionFactory.class}属性。
     *
     * @param config Configuration对象。
     *
     * @see #setActionFactoryClass(java.lang.Class)
     */
    protected void setDefaultActionFactoryClass(Configuration config) {
        config.setActionFactoryClass(DefaultActionFactory.class);
    }

    /**
     * 未设置objectFactory属性时，提供默认的{@code SpringObjectFactory}对象实现。
     *
     * @param config Configuration对象。
     *
     * @return {@code SpringObjectFactory}对象。
     *
     * @see #setObjectFactory(jrouter.ObjectFactory)
     */
    protected ObjectFactory createDefaultObjectFactory(Configuration config) {
        //create SpringObjectFactory
        return new SpringObjectFactory(applicationContext);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Add matched bean.
     *
     * @param bean matched bean.
     * @param listArray the array of list.
     */
    private void addComponentToList(Object bean, List... listArray) {
        out:
        for (List<Object> componentList : listArray) {
            if (CollectionUtil.isNotEmpty(componentList)) {
                for (Object exist : componentList) {
                    if (bean.getClass() == (exist instanceof Class ? exist : exist.getClass()))
                        continue out;
                }
                componentList.add(bean);
            }
        }
    }

    /**
     * Convert the {@code String} element of the list into {@code Class}.
     *
     * @param listArray the array of list.
     *
     * @throws ClassNotFoundException If the class was not found.
     */
    private static void convertList(List... listArray) throws ClassNotFoundException {
        for (List componentList : listArray) {
            if (CollectionUtil.isNotEmpty(componentList)) {
                for (int i = 0; i < componentList.size(); i++) {
                    Object obj = componentList.get(i);
                    if (obj instanceof String) {
                        componentList.set(i, ClassUtil.loadClass((String) obj));
                    }
                }
            }
        }
    }

    /**
     * 在bean工厂关闭时移除ActionFactory中所有关联关系。
     *
     * @throws JRouterException 如果发生错误。
     */
    @Override
    public void destroy() throws JRouterException {
        LOG.info("Closing JRouter ActionFactory : " + actionFactory);
        try {
            beforeActionFactoryDestruction();
        } finally {
            if (actionFactory != null)
                this.actionFactory.clear();
        }
    }

    /**
     * 返回ActionFactory对象，默认为单例状态。
     */
    @Override
    public T getObject() {
        return (T) this.actionFactory;
    }

    @Override
    public Class<? extends ActionFactory> getObjectType() {
        return actionFactory == null ? null : actionFactory.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Hook that allows post-processing after the ActionFactory has been successfully created.
     * The ActionFactory is already available through {@code getActionFactory()} at this point.
     * <p>
     * This implementation is empty.
     *
     * @param actionFactory ActionFactory。
     *
     * @see #buildActionFactory()
     */
    protected void afterActionFactoryCreation(T actionFactory) {
    }

    /**
     * Hook that allows shutdown processing before the ActionFactory will be closed.
     * The ActionFactory is still available through {@code getActionFactory()} at this point.
     * <p>
     * This implementation is empty.
     *
     * @see #destroy()
     */
    protected void beforeActionFactoryDestruction() {
    }
////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Set the location of a single JRouter XML config file.
     *
     * @param configLocation Location of a single JRouter XML config file.
     *
     * @see jrouter.config.Configuration#load(java.net.URL)
     */
    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    /**
     * 指定的JRouter Configuration类型。
     *
     * @param configurationClass 指定的Configuration类型。
     *
     * @see jrouter.config.Configuration
     * @see #setConfiguration(jrouter.config.Configuration)
     *
     * @deprecated since 1.6.4
     */
    @Deprecated
    public void setConfigurationClass(Class<? extends Configuration> configurationClass) {
        if (configurationClass == null || !Configuration.class.isAssignableFrom(configurationClass)) {
            throw new IllegalArgumentException(
                    "'configurationClass' must be assignable to [jrouter.config.Configuration]");
        }
        this.configurationClass = (Class<? extends Configuration>) configurationClass;
    }

    /**
     * 设置指定的Configuration。
     *
     * @param configuration 指定的Configuration。
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Get the JRouter Configuration object used to build the ActionFactory.
     *
     * @return the JRouter Configuration object used to build the ActionFactory.
     */
    public final Configuration getConfiguration() {
        if (this.configuration == null) {
            throw new IllegalStateException("Configuration not initialized yet");
        }
        return this.configuration;
    }

    /**
     * 设置ActionFactory的类型。
     *
     * @param actionFactoryClass 指定的ActionFactory类型。
     *
     * @see Configuration#setActionFactoryClass(java.lang.Class)
     */
    public void setActionFactoryClass(Class<T> actionFactoryClass) {
        if (actionFactoryClass == null || !ActionFactory.class.isAssignableFrom(actionFactoryClass)) {
            throw new IllegalArgumentException(
                    "'actionFactoryClass' must be assignable to [jrouter.ActionFactory]");
        }
        this.actionFactoryClass = actionFactoryClass;
    }

    /**
     * 添加ActionFactory的属性映射集合。
     *
     * @param actionFactoryProperties ActionFactory的属性映射集合。
     *
     * @see Configuration#addActionFactoryProperties(java.util.Map)
     * @see jrouter.impl.DefaultActionFactory#setActionFactoryProperties(java.util.Map)
     */
    public void setActionFactoryProperties(Properties actionFactoryProperties) {
        this.actionFactoryProperties = actionFactoryProperties;
    }

    /**
     * 设置ActionFactory中创建对象的工厂对象。
     *
     * @param objectFactory ActionFactory中创建对象的工厂对象。
     *
     * @see Configuration#addActionFactoryProperties(java.util.Map)
     * @see ActionFactory#getObjectFactory()
     */
    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 设置Action的bean名称和类名称的集合。
     *
     * @param actions Action的bean名称和类名称的集合。
     */
    public void setActions(List<Object> actions) {
        this.actions = actions;
    }

    /**
     * 设置拦截栈的bean名称和类名称的集合。
     *
     * @param interceptorStacks 拦截栈的bean名称和类名称的集合。
     */
    public void setInterceptorStacks(List<Object> interceptorStacks) {
        this.interceptorStacks = interceptorStacks;
    }

    /**
     * 设置拦截器的bean名称和类名称的集合。
     *
     * @param interceptors 拦截器的bean名称和类名称的集合。
     */
    public void setInterceptors(List<Object> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * 设置结果类型的bean名称和类名称的集合。
     *
     * @param resultTypes 结果类型的bean名称和类名称的集合。
     */
    public void setResultTypes(List<Object> resultTypes) {
        this.resultTypes = resultTypes;
    }

    /**
     * 设置结果对象的bean名称和类名称的集合。
     *
     * @param results 结果对象的bean名称和类名称的集合。
     */
    public void setResults(List<Object> results) {
        this.results = results;
    }

    /**
     * @see #setComponentClassScanProperties(java.util.List)
     *
     * @deprecated
     */
    @Deprecated
    public void setClassScannerProperties(List<Properties> classScannerProperties) {
        this.setComponentClassScanProperties(classScannerProperties);
    }

    /**
     * 设置扫描类工具的顺序配置。
     *
     * @param componentClassScanProperties 扫描类工具的顺序配置集合。
     *
     * @see Configuration#parsecComponentClassScanner(java.util.Map)
     */
    public void setComponentClassScanProperties(List<Properties> componentClassScanProperties) {
        this.componentClassScanProperties = componentClassScanProperties;
    }

    /**
     * 设置Action的aop。
     *
     * @param aopActions Actions' Aop。
     */
    public void setAopActions(List<? extends AopAction> aopActions) {
        this.aopActions = aopActions;
    }

    /**
     * 设置匹配包含/排除的beans' name属性。
     *
     * @param componentBeanScanProperties 匹配包含/排除的beans' name属性。
     *
     */
    public void setComponentBeanScanProperties(Properties componentBeanScanProperties) {
        this.componentBeanScanProperties = componentBeanScanProperties;
    }

    /**
     * Configuration for springframework's DI.
     */
    private static class SpringConfiguration extends Configuration {

        /** ApplicationContext */
        private final ApplicationContext applicationContext;

        /**
         * 构造一个指定ApplicationContext的对象。
         *
         * @param applicationContext ApplicationContext对象。
         */
        public SpringConfiguration(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public void afterActionFactoryCreation(ActionFactory actionFactory) {
            applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(actionFactory, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
        }

        @Override
        protected void afterActionFactoryBuild(ActionFactory actionFactory) {
            applicationContext.getAutowireCapableBeanFactory().initializeBean(actionFactory, null);
        }
    }
}
