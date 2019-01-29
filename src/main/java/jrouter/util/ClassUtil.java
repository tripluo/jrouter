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
package jrouter.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class工具类。
 */
public class ClassUtil {

    /** log */
    private static final Logger LOG = LoggerFactory.getLogger(ClassUtil.class);

    /** 文件名称编码 */
    public static final String DECODING = "UTF-8";

    /** JAVA Class文件后缀 */
    private static final String JAVA_CLASS_SUFFIX = ".class";

    /** 检测是否引入javassist */
    private static final boolean JAVASSIST_SUPPORTED = loadClassQuietly("javassist.ClassPool") != null;

    /**
     * 从指定的包名中获取所有Class的名称集合，忽略类加载异常。
     *
     * @param packageNames 指定的包的名称。
     *
     * @return 指定的包名中所有Class的名称集合。
     */
    public static Set<Class<?>> getClasses(String... packageNames) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        loadClasses(classes, packageNames);
        return classes;
    }

    /**
     * 从指定的包名中获取所有Class的名称添加至指定的集合，忽略类加载异常。
     *
     * @param classes 指定的Class名称集合。
     * @param packageNames 指定的包的名称。
     */
    private static void loadClasses(Collection<Class<?>> classes, String... packageNames) {
        //recursive
        boolean recursive = true;
        for (String packageName : packageNames) {
            if (StringUtil.isEmpty(packageName)) {
                continue;
            }
            String packageDirName = packageName.replace('.', '/');
            Enumeration<URL> dirs = null;
            try {
                dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
                while (dirs.hasMoreElements()) {
                    URL url = dirs.nextElement();
                    //protocol
                    String protocol = url.getProtocol();
                    //if file
                    if ("file".equals(protocol)) {
                        String filePath = URLDecoder.decode(url.getFile(), DECODING);
                        //以文件的方式扫描整个包下的文件 并添加到集合中
                        loadClassesByPackageFile(packageName, filePath, recursive, classes);
                    } //if jar
                    else if ("jar".equals(protocol)) {
                        JarFile jar = null;
                        try {
                            jar = ((JarURLConnection) url.openConnection()).getJarFile();
                            Enumeration<JarEntry> entries = jar.entries();
                            while (entries.hasMoreElements()) {
                                //获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                                JarEntry entry = entries.nextElement();
                                String name = entry.getName();
                                char slash = '/';
                                //如果是以/开头的
                                if (name.charAt(0) == slash) {
                                    name = name.substring(1);
                                }
                                //如果前半部分和定义的包名相同
                                if (name.startsWith(packageDirName)) {
                                    int idx = name.lastIndexOf(slash);
                                    //如果以"/"结尾，是一个包
                                    if (idx != -1) {
                                        //获取包名，把"/"替换成"."
                                        packageName = name.substring(0, idx).replace(slash, '.');
                                    }
                                    //如果可以迭代下去 并且是一个包
                                    if ((idx != -1) || recursive) {
                                        //如果是一个.class文件，而且不是目录
                                        if (name.endsWith(JAVA_CLASS_SUFFIX) && !entry.isDirectory()) {
                                            //去掉后面的".class" 获取真正的类名
                                            String className = name.substring(packageName.length() + 1, name.length() - 6);
                                            Class<?> cls = loadClassQuietly(packageName + '.' + className);
                                            if (cls != null) { //NOPMD AvoidDeeplyNestedIfStmts
                                                classes.add(cls);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            LOG.error("IOException when loading files from : " + url, e);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("IOException when get classes from : " + packageName, e);
            }
        }
    }

    /**
     * 以文件的形式来获取指定包中所有Class的名称添加至指定的集合，忽略类加载异常。
     *
     * @param packageName 包名的目录形式。
     * @param packagePath 包所在的目录。
     * @param recursive 是否递归文件目录。
     * @param classes 指定的Class名称集合。
     */
    private static void loadClassesByPackageFile(String packageName, String packagePath, final boolean recursive,
            Collection<Class<?>> classes) {
        //package directory
        File dir = new File(packagePath);
        //not exists or not directory
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirfiles = dir.listFiles(new FileFilter() {

            @Override
            //file filter for .class files
            public boolean accept(File file) {
                return (recursive && file.isDirectory())
                        || (file.getName().endsWith(JAVA_CLASS_SUFFIX));
            }
        });
        for (File file : dirfiles) {
            //if directory
            if (file.isDirectory()) {
                loadClassesByPackageFile(packageName + '.' + file.getName(),
                        file.getAbsolutePath(), recursive, classes);
            } else {
                //remove .class suffix
                String className = file.getName().substring(0, file.getName().length() - 6);
                Class<?> cls = loadClassQuietly(packageName + '.' + className);
                if (cls != null) {
                    classes.add(cls);
                }
            }
        }
    }

    /**
     * 返回与带有给定字符串名的类或接口相关联的 Class 对象，如果 Class 对象不存在则返回{@code null}。
     *
     * @param className 所需类的完全限定名。
     *
     * @return 具有指定名的类的 Class 对象。
     */
    public static Class<?> loadClassQuietly(String className) {
        try {
            return loadClass(className);
        } catch (Throwable thr) { //NOPMD AvoidCatchingThrowable
            //ignore
        }
        return null;
    }

    /**
     * 返回与带有给定字符串名的类或接口相关联的 Class 对象。
     *
     * @param className 所需类的完全限定名。
     *
     * @return 具有指定名的类的 Class 对象。
     *
     * @throws ClassNotFoundException 如果无法定位该类。
     */
    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = contextCL == null ? ClassUtil.class.getClassLoader() : contextCL; //NOPMD UseProperClassLoader
        return loader.loadClass(className);
    }

    /**
     * 是否支持Javassist。
     *
     * @return 是否支持Javassist。
     */
    public static boolean isJavassistSupported() {
        return JAVASSIST_SUPPORTED;
    }
}
