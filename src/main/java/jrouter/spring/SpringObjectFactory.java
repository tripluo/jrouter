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

import jrouter.ObjectFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 借由 springframework 的工厂对象创建新的对象实例。
 */
public class SpringObjectFactory implements ObjectFactory, ApplicationContextAware {

    /**
     * springframework 的AutowireCapableBeanFactory对象。
     */
    protected AutowireCapableBeanFactory autowireCapableBeanFactory;

    /**
     * springframework 属性注入的策略；默认{@code byName}
     */
    private int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    /**
     * 构造一个空对象。
     */
    public SpringObjectFactory() {
    }

    /**
     * 构造一个指定ApplicationContext的对象。
     *
     * @param applicationContext ApplicationContext对象。
     */
    public SpringObjectFactory(ApplicationContext applicationContext) {
        setApplicationContext(applicationContext);
    }

    @Override
    public <T> T newInstance(Class<T> clazz) {
        if (AutowireCapableBeanFactory.AUTOWIRE_NO == autowireMode)
            return autowireCapableBeanFactory.createBean(clazz);
        return (T) autowireCapableBeanFactory.createBean(clazz, autowireMode, false);
    }

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) {
        autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    /**
     * 返回注入策略。
     *
     * @return 注入策略。
     */
    public int getAutowireMode() {
        return autowireMode;
    }

    /**
     * 设置注入策略。
     *
     * @param autowireMode 注入策略。
     *
     * @see AutowireCapableBeanFactory#AUTOWIRE_BY_NAME
     * @see AutowireCapableBeanFactory#AUTOWIRE_BY_TYPE
     * @see AutowireCapableBeanFactory#AUTOWIRE_CONSTRUCTOR
     * @see AutowireCapableBeanFactory#AUTOWIRE_NO
     */
    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }
}
