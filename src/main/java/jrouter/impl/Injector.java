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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jrouter.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean注入工具类。
 */
public class Injector {

    /* 日志记录 */
    private static final Logger LOG = LoggerFactory.getLogger(Injector.class);

    /** 对象类型与其注入属性的映射 */
    static final Map<Class, Injection[]> classInjection = new HashMap<Class, Injection[]>();

    /** action对象与其注入属性的映射 */
    static final Map<String, Injection[]> actionInjection = new HashMap<String, Injection[]>();

    /**
     * private constructor
     */
    private Injector() {
    }

    /**
     * 添加指定对象类型与注入属性的映射。
     *
     * @param cls 指定的对象类型。
     * @param properties 对象类型的属性映射集合。
     *
     * @return 如果已存在对象类型的属性则返回原有的属性集合，没有则返回 null。
     *
     * @throws IntrospectionException 如果在内省期间发生异常。
     */
    public static Injection[] putClassProperties(Class<?> cls, Map<String, Object> properties)
            throws IntrospectionException {
        return classInjection.put(cls, convertToInjections(cls, properties));
    }

    /**
     * 添加指定path的Action与注入属性的映射。
     *
     * @param cls 指定Action的类型。
     * @param actionPath 指定Action的全路径。
     * @param properties 对象类型的属性映射集合。
     *
     * @return 如果已存在Action的属性则返回原有的属性集合，没有则返回 null。
     *
     * @throws IntrospectionException 如果在内省期间发生异常。
     */
    public static Injection[] putActionProperties(Class<?> cls, String actionPath,
            Map<String, Object> properties) throws IntrospectionException {
        return actionInjection.put(actionPath, convertToInjections(cls, properties));
    }

    /**
     * 转换指定对象类型的属性映射至注入对象集合。
     *
     * @param cls 指定的对象类型。
     * @param properties 对象类型的属性映射集合。
     *
     * @return 注入对象集合。
     *
     * @throws IntrospectionException 如果在内省期间发生异常。
     *
     * @see Injection
     */
    private static Injection[] convertToInjections(Class<?> cls, Map<String, Object> properties)
            throws IntrospectionException {
        //common class properties
        Map<String, PropertyDescriptor> supports = Injector.getSupportedProperties(cls);
        List<Injection> injections = new ArrayList<Injection>(properties.size());
        for (Map.Entry<String, Object> prop : properties.entrySet()) {
            String pName = prop.getKey();
            PropertyDescriptor pd = supports.get(pName);
            if (pd == null) {
                LOG.warn("Not supported property [{}] in [{}]", pName, cls);
            } else {
                Object value = prop.getValue();
                if (value instanceof String) {
                    Object convertedValue = Injector.stringToObject((String) value, pd.getPropertyType());
                    if (convertedValue == null) {
                        LOG.warn("Not supported property [{}] for type [{}] in " + cls, pName, pd.getPropertyType());
                    } else {
                        injections.add(new Injection(pd.getWriteMethod(), convertedValue));
                    }
                } else {
                    injections.add(new Injection(pd.getWriteMethod(), value));
                }
            }
        }
        return injections.toArray(new Injection[injections.size()]);
    }

    /*
     * 注入Action所在路径（path）的属性；路径（path）属性已包含对象类型的属性。
     *
     * @param actionPath 指定Action的全路径。
     * @param invoker Action所在的对象。
     *
     * @throws IllegalAccessException 如果属性注入的方法不可访问。
     * @throws InvocationTargetException 如果属性注入的方法发生异常。
     */
    static void injectAction(String actionPath, Object invoker) throws IllegalAccessException,
            InvocationTargetException {
        Injection[] injects = actionInjection.get(actionPath);
        //如果指定的Action中无注入属性，则查找其对象类型的注入属性
//        if (injects == null) {
//            injects = classInjection.get(invoker.getClass());
//        }
        if (injects != null && injects.length > 0) {
            for (Injection ij : injects) {
                ij.setter.invoke(invoker, ij.value);
            }
        }
    }

    /**
     * 注入指定对象的属性。
     *
     * @param obj 指定的对象。
     *
     * @throws IllegalAccessException 如果属性注入的方法不可访问。
     * @throws InvocationTargetException 如果属性注入的方法发生异常。
     */
    public static void injectObject(Object obj) throws IllegalAccessException,
            InvocationTargetException {
        Injection[] injects = classInjection.get(obj.getClass());
        if (injects != null && injects.length > 0) {
            for (Injection ij : injects) {
                ij.setter.invoke(obj, ij.value);
            }
        }
    }

    /**
     * 清除类型与注入属性的映射。
     */
    static void clear() {
        classInjection.clear();
        actionInjection.clear();
    }

    /**
     * 返回指定对象类型支持的属性描述映射。
     *
     * @param cls 指定的对象类型。
     *
     * @return 支持的属性描述映射。
     *
     * @throws IntrospectionException 如果在内省期间发生异常。
     */
    public static Map<String, PropertyDescriptor> getSupportedProperties(Class<?> cls) throws
            IntrospectionException {
        PropertyDescriptor[] propds = Introspector.getBeanInfo(cls).getPropertyDescriptors();
        Map<String, PropertyDescriptor> support = new HashMap<String, PropertyDescriptor>(propds.length);
        //getPropertyDescriptors返回的PropertyDescriptor[]已经过滤了重复的属性名。
        for (PropertyDescriptor p : propds) {
            if (supportTypes.contains(p.getPropertyType()))
                support.put(p.getName(), p);
        }
        return support;
    }
    /**
     * 注入指定对象的属性。
     *
     * @param obj 指定的对象。
     * @param props 属性名称和字符串值的映射。
     *
     * @throws IntrospectionException 如果在内省期间发生异常。
     * @throws IllegalAccessException 如果底层方法不可访问。
     * @throws InvocationTargetException 如果底层方法抛出异常。
     * @throws ClassNotFoundException 如果没有找到具有指定名称的类。
     */
//        public static void setProperties(Object obj, Map<String, String> props) throws
//                IntrospectionException, IllegalAccessException, InvocationTargetException,
//                ClassNotFoundException {
//            PropertyDescriptor[] propds = Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors();
//            for (PropertyDescriptor p : propds) {
//                String value = props.get(p.getName());
//                if (value != null) {
//                    Class type = p.getPropertyType();
//                    if (supportTypes.contains(type)) {
//                        p.getWriteMethod().invoke(obj, stringToObject(value, type));
//                    } else {
//                        //not supported setter method if been invoked
//                        LOG.error("Not supported set [{}] as [{}]", p.getName(), p.getPropertyType());
//                    }
//                }
//            }
//        }
    /**
     * 所支持的属性转换对象类型
     */
    private static final Set<Class<?>> supportTypes = new HashSet<Class<?>>(18);

    //初始化所支持的对象类型。
    static {
        //String to Class
        supportTypes.add(Class.class);
        supportTypes.add(String.class);
        supportTypes.add(boolean.class);
        supportTypes.add(Boolean.class);
        supportTypes.add(byte.class);
        supportTypes.add(Byte.class);
        supportTypes.add(char.class);
        supportTypes.add(Character.class);
        supportTypes.add(double.class);
        supportTypes.add(Double.class);
        supportTypes.add(float.class);
        supportTypes.add(Float.class);
        supportTypes.add(int.class);
        supportTypes.add(Integer.class);
        supportTypes.add(long.class);
        supportTypes.add(Long.class);
        supportTypes.add(short.class);
        supportTypes.add(Short.class);
    }

    /**
     * 转换字符串至指定类型的对象。
     *
     * @param str 指定的字符串。
     * @param type 指定的类型。
     *
     * @return 转换后的对象。
     */
    public static Object stringToObject(String str, Class<?> type) {
        if (type == String.class)
            return str;
        else if (type == Class.class)
            try {
                return ClassUtil.loadClass(str);
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            }
        else if (type == int.class || type == Integer.class)
            return Integer.parseInt(str);
        else if (type == boolean.class || type == Boolean.class)
            return Boolean.parseBoolean(str);
        else if (type == double.class || type == Double.class)
            return Double.parseDouble(str);
        else if (type == long.class || type == Long.class)
            return Long.parseLong(str);
        else if (type == float.class || type == Float.class)
            return Float.parseFloat(str);
        else if (type == byte.class || type == Byte.class)
            return Byte.parseByte(str);
        else if (type == char.class || type == Character.class)
            return str.charAt(0);
        else if (type == short.class || type == Short.class)
            return Short.parseShort(str);
        return null;
    }

    /**
     * 封装了Bean的setter方法和其值。
     */
    public static class Injection {

        /**
         * 属性的setter方法
         */
        private Method setter;

        /**
         * 注入属性的值
         */
        private Object value;

        /**
         * 构造一个指定setter方法和属性值的对象。
         *
         * @param setter 属性的setter方法。
         * @param value 属性的值。
         */
        public Injection(Method setter, Object value) {
            this.setter = setter;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Injection{" + "setter=" + setter.getName() + ", value=" + value + '}';
        }
    }
}
