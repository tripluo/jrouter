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

import net.jrouter.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Bean注入工具类。
 */
public final class Injector {

    /**
     * 对象类型与其注入属性的映射
     */
    static final Map<Class<?>, Injection[]> CLASS_INJECTION = new HashMap<>();

    /**
     * action对象与其注入属性的映射
     */
    static final Map<String, Injection[]> ACTION_INJECTION = new HashMap<>();

    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(Injector.class);

    /**
     * 所支持的属性转换对象类型
     */
    private static final Set<Class<?>> SUPPORT_TYPES = new HashSet<>(18);

    // 初始化所支持的对象类型。
    static {
        // String to Class
        SUPPORT_TYPES.add(Class.class);
        SUPPORT_TYPES.add(String.class);
        SUPPORT_TYPES.add(boolean.class);
        SUPPORT_TYPES.add(Boolean.class);
        SUPPORT_TYPES.add(byte.class);
        SUPPORT_TYPES.add(Byte.class);
        SUPPORT_TYPES.add(char.class);
        SUPPORT_TYPES.add(Character.class);
        SUPPORT_TYPES.add(double.class);
        SUPPORT_TYPES.add(Double.class);
        SUPPORT_TYPES.add(float.class);
        SUPPORT_TYPES.add(Float.class);
        SUPPORT_TYPES.add(int.class);
        SUPPORT_TYPES.add(Integer.class);
        SUPPORT_TYPES.add(long.class);
        SUPPORT_TYPES.add(Long.class);
        SUPPORT_TYPES.add(short.class);
        SUPPORT_TYPES.add(Short.class);
    }

    /**
     * private constructor
     */
    private Injector() {
    }

    /**
     * 添加指定对象类型与注入属性的映射。
     * @param cls 指定的对象类型。
     * @param properties 对象类型的属性映射集合。
     * @return 如果已存在对象类型的属性则返回原有的属性集合，没有则返回 null。
     * @throws IntrospectionException 如果在内省期间发生异常。
     */
    public static Injection[] putClassProperties(Class<?> cls, Map<String, Object> properties)
            throws IntrospectionException {
        return CLASS_INJECTION.put(cls, convertToInjections(cls, properties));
    }

    /**
     * 添加指定path的Action与注入属性的映射。
     * @param cls 指定Action的类型。
     * @param actionPath 指定Action的全路径。
     * @param properties 对象类型的属性映射集合。
     * @return 如果已存在Action的属性则返回原有的属性集合，没有则返回 null。
     * @throws IntrospectionException 如果在内省期间发生异常。
     */
    public static Injection[] putActionProperties(Class<?> cls, String actionPath, Map<String, Object> properties)
            throws IntrospectionException {
        return ACTION_INJECTION.put(actionPath, convertToInjections(cls, properties));
    }

    /**
     * 转换指定对象类型的属性映射至注入对象集合。
     * @param cls 指定的对象类型。
     * @param properties 对象类型的属性映射集合。
     * @return 注入对象集合。
     * @throws IntrospectionException 如果在内省期间发生异常。
     * @see Injection
     */
    private static Injection[] convertToInjections(Class<?> cls, Map<String, Object> properties)
            throws IntrospectionException {
        // common class properties
        Map<String, PropertyDescriptor> supports = Injector.getSupportedProperties(cls);
        List<Injection> injections = new ArrayList<>(properties.size());
        for (Map.Entry<String, Object> prop : properties.entrySet()) {
            String pName = prop.getKey();
            PropertyDescriptor pd = supports.get(pName);
            if (pd == null) {
                LOG.warn("Not supported property [{}] in [{}]", pName, cls);
            }
            else {
                Object value = prop.getValue();
                if (value instanceof String) {
                    Object convertedValue = Injector.stringToObject((String) value, pd.getPropertyType());
                    if (convertedValue == null) {
                        LOG.warn("Not supported property [{}] for type [{}] in {}.", pName, pd.getPropertyType(), cls);
                    }
                    else {
                        injections.add(new Injection(pd.getWriteMethod(), convertedValue));
                    }
                }
                else {
                    injections.add(new Injection(pd.getWriteMethod(), value));
                }
            }
        }
        return injections.toArray(new Injection[0]);
    }

    /*
     * 注入Action所在路径（path）的属性；路径（path）属性已包含对象类型的属性。
     *
     * @param actionPath 指定Action的全路径。
     *
     * @param invoker Action所在的对象。
     *
     * @throws IllegalAccessException 如果属性注入的方法不可访问。
     *
     * @throws InvocationTargetException 如果属性注入的方法发生异常。
     */
    static void injectAction(String actionPath, Object invoker)
            throws IllegalAccessException, InvocationTargetException {
        Injection[] injects = ACTION_INJECTION.get(actionPath);
        // 如果指定的Action中无注入属性，则查找其对象类型的注入属性
        // if (injects == null) {
        // injects = classInjection.get(invoker.getClass());
        // }
        if (injects != null) {
            for (Injection ij : injects) {
                ij.setter.invoke(invoker, ij.value);
            }
        }
    }

    /**
     * 注入指定对象的属性。
     * @param obj 指定的对象。
     * @throws IllegalAccessException 如果属性注入的方法不可访问。
     * @throws InvocationTargetException 如果属性注入的方法发生异常。
     */
    public static void injectObject(Object obj) throws IllegalAccessException, InvocationTargetException {
        Injection[] injects = CLASS_INJECTION.get(obj.getClass());
        if (injects != null) {
            for (Injection ij : injects) {
                ij.setter.invoke(obj, ij.value);
            }
        }
    }

    /**
     * 清除类型与注入属性的映射。
     */
    static void clear() {
        CLASS_INJECTION.clear();
        ACTION_INJECTION.clear();
    }

    /**
     * 返回指定对象类型支持的属性描述映射。
     * @param cls 指定的对象类型。
     * @return 支持的属性描述映射。
     * @throws IntrospectionException 如果在内省期间发生异常。
     */
    public static Map<String, PropertyDescriptor> getSupportedProperties(Class<?> cls) throws IntrospectionException {
        PropertyDescriptor[] descriptors = Introspector.getBeanInfo(cls).getPropertyDescriptors();
        Map<String, PropertyDescriptor> support = new HashMap<>(descriptors.length);
        // getPropertyDescriptors返回的PropertyDescriptor[]已经过滤了重复的属性名。
        for (PropertyDescriptor p : descriptors) {
            if (SUPPORT_TYPES.contains(p.getPropertyType())) {
                support.put(p.getName(), p);
            }
        }
        return support;
    }

    /**
     * 转换字符串至指定类型的对象。
     * @param str 指定的字符串。
     * @param type 指定的类型。
     * @return 转换后的对象。
     */
    public static Object stringToObject(String str, Class<?> type) {
        if (type == String.class) {
            return str;
        }
        else if (type == Class.class) {
            try {
                return ClassUtil.loadClass(str);
            }
            catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(str);
        }
        else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(str);
        }
        else if (type == double.class || type == Double.class) {
            return Double.parseDouble(str);
        }
        else if (type == long.class || type == Long.class) {
            return Long.parseLong(str);
        }
        else if (type == float.class || type == Float.class) {
            return Float.parseFloat(str);
        }
        else if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(str);
        }
        else if (type == char.class || type == Character.class) {
            return str.charAt(0);
        }
        else if (type == short.class || type == Short.class) {
            return Short.parseShort(str);
        }
        return null;
    }

    /**
     * 封装了Bean的setter方法和其值。
     */
    public static class Injection {

        /**
         * 属性的setter方法
         */
        private final Method setter;

        /**
         * 注入属性的值
         */
        private final Object value;

        /**
         * 构造一个指定setter方法和属性值的对象。
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
