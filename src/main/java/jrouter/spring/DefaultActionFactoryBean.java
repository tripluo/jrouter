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

import java.util.List;
import java.util.Map;
import java.util.Properties;
import jrouter.ActionFactory;
import jrouter.JRouterException;
import jrouter.ObjectFactory;
import jrouter.config.Configuration;
import jrouter.util.ClassUtil;
import jrouter.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * 提供与springframework集成的ActionFactory。Action指定path属性的注入由指定springframework的bean完成。
 */
public class DefaultActionFactoryBean implements FactoryBean<ActionFactory>, InitializingBean,
        DisposableBean, ApplicationContextAware {

    /** LOG */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultActionFactoryBean.class);

    /**
     * Location of a single JRouter XML config file. 不保证ActionFactory属性的重复加载。
     */
    private Resource configLocation;

    /* ActionFactory对象 */
    private ActionFactory actionFactory;

    /* ActionFactory的类型 */
    private Class<? extends ActionFactory> actionFactoryClass = null;

    /** Configuration对象 */
    private Configuration configuration;

    /** Configuration对象类型 */
    private Class<? extends Configuration> configurationClass = Configuration.class;

    /** @see Configuration#actionFactoryProperties */
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

    /** 扫描类工具的顺序配置 */
    private List<Properties> classScannerProperties;
////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 初始化ActionFactory。
     *
     * @see #buildActionFactory()
     * @see #afterActionFactoryCreation(jrouter.ActionFactory)
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        actionFactory = buildActionFactory();
        afterActionFactoryCreation(actionFactory);
    }

    protected ActionFactory buildActionFactory() throws Exception {
        LOG.info("Initiating JRouter ActionFactory at : " + new java.util.Date());
        configuration = configurationClass.newInstance();
        //不保证ActionFactory属性的重复加载
        if (configLocation != null) {
            LOG.debug("Load configuration : " + configLocation.getURL());
            configuration.load(configLocation.getURL());
        }

        if (actionFactoryClass != null)
            configuration.setActionFactoryClass(actionFactoryClass);

        configuration.addActionFactoryProperties((Map) actionFactoryProperties);

        //添加扫描工具属性
        if (classScannerProperties != null)
            configuration.addClassScannerProperties(classScannerProperties.toArray(new Map[classScannerProperties.size()]));

        //convert string to class
        convertList(interceptors, interceptorStacks, resultTypes, results, actions);

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

        return configuration.buildActionFactory();
    }

    /**
     * convert the
     * <code>String</code> element of the list into
     * <code>Class</code>.
     *
     * @param listArray the array of list.
     *
     * @throws ClassNotFoundException If the class was not found.
     */
    private static void convertList(List... listArray) throws ClassNotFoundException {
        for (List list : listArray) {
            if (CollectionUtil.isNotEmpty(list)) {
                for (int i = 0; i < list.size(); i++) {
                    Object obj = list.get(i);
                    if (obj instanceof String) {
                        list.set(i, ClassUtil.loadClass((String) obj));
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
    public ActionFactory getObject() {
        return this.actionFactory;
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
     * Hook that allows post-processing after the ActionFactory has been successfully created. The
     * ActionFactory is already available through
     * <code>getActionFactory()</code> at this point. <p>This implementation is empty.
     *
     * @param actionFactory ActionFactory。
     *
     * @see #buildActionFactory()
     */
    protected void afterActionFactoryCreation(ActionFactory actionFactory) {
    }

    /**
     * Hook that allows shutdown processing before the ActionFactory will be closed. The
     * ActionFactory is still available through
     * <code>getActionFactory()</code> at this point. <p>This implementation is empty.
     *
     * @see #destroy()
     */
    protected void beforeActionFactoryDestruction() {
    }
////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        setObjectFactory(new SpringObjectFactory(applicationContext));
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
     */
    public void setConfigurationClass(Class<? extends Configuration> configurationClass) {
        if (configurationClass == null || !Configuration.class.isAssignableFrom(configurationClass)) {
            throw new IllegalArgumentException(
                    "'configurationClass' must be assignable to [jrouter.config.Configuration]");
        }
        this.configurationClass = (Class<? extends Configuration>) configurationClass;
    }

    /**
     * 设置ActionFactory的类型。
     *
     * @param actionFactoryClass 指定的ActionFactory类型。
     *
     * @see Configuration#setActionFactoryClass(java.lang.Class)
     */
    public void setActionFactoryClass(Class<? extends ActionFactory> actionFactoryClass) {
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
        if (objectFactory != null) {
            actionFactoryProperties.put("objectFactory", objectFactory);
        }
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
     * 设置扫描类工具的顺序配置。
     *
     * @param classScannerProperties 扫描类工具的顺序配置集合。
     */
    public void setClassScannerProperties(List<Properties> classScannerProperties) {
        this.classScannerProperties = classScannerProperties;
    }
}
