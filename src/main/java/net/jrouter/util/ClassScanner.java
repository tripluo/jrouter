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

package net.jrouter.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * 类扫描的工具。通过设置扫描的包名及包含的表达式、排除类的表达式（排除优先于包含），计算并返回扫描结果的类的集合。
 * 默认排除接口、抽象类、非公共类及无公共无参数构造函数的类。
 *
 * @see #getClasses()
 */
public class ClassScanner {

    /**
     * Class路径匹配器。
     */
    private final AntPathMatcher classMatcher = new AntPathMatcher(".");

    /**
     * 包含的包或类。
     */
    @lombok.Setter
    private Set<String> includePackages = Collections.emptySet();

    /**
     * 包含匹配类的表达式。
     */
    @lombok.Setter
    private Set<String> includeExpressions;

    /**
     * 排除匹配类的表达式。
     */
    @lombok.Setter
    private Set<String> excludeExpressions;

    /**
     * 计算最终扫描结果的类集合，排除接口、抽象类、非公共类及无公共无参数构造函数的类。
     *
     * @return 扫描结果的类集合。
     */
    public Set<Class<?>> getClasses() {
        Set<Class<?>> includes = ClassUtil.getClasses(includePackages.toArray(new String[0]));
        // filter the scan classes
        Iterator<Class<?>> it = includes.iterator();
        out:
        while (it.hasNext()) {
            Class<?> cls = it.next();
            // exclude interface, no public class and no default constructors
            if (cls.isInterface()
                    || Modifier.isAbstract(cls.getModifiers())
                    || !Modifier.isPublic(cls.getModifiers())) {
                it.remove();
                continue out;
            } //去除无默认public空构造方法的类
            else {
                // 获取对象的public构造方法
                Constructor<?>[] cs = cls.getConstructors();
                if (cs.length == 0) {
                    it.remove();
                    continue out;
                } else {
                    boolean hasEmptyConstructor = false;
                    for (Constructor<?> c : cs) {
                        // 空构造方法
                        if (c.getParameterTypes().length == 0) {
                            hasEmptyConstructor = true;
                            break;
                        }
                    }
                    if (!hasEmptyConstructor) {
                        it.remove();
                        continue out;
                    }
                }
            }

            // class name
            String clsName = cls.getName();

            // only include matched expression, it means to exclude the classes not match the expression
            if (CollectionUtil.isNotEmpty(includeExpressions)) {
                boolean isInclude = false;
                for (String includeExpression : includeExpressions) {
                    // the include expression must a pattern
                    if (classMatcher.match(includeExpression, clsName)) {
                        isInclude = true;
                        break;
                    }
                }
                // 不包含与include expressions中
                if (!isInclude) {
                    it.remove();
                    continue out;
                }
            }

            // exclude matched expressions
            if (CollectionUtil.isNotEmpty(excludeExpressions)) {
                for (String excludeExpression : excludeExpressions) {
                    if (classMatcher.match(excludeExpression, clsName)) {
                        it.remove();
                        continue out;
                    }
                }
            }
        }
        return includes;
    }

    @Override
    public String toString() {
        return "ClassScanner{" + "includePackages=" + includePackages + ", includeExpressions=" + includeExpressions + ", excludeExpressions=" + excludeExpressions + '}';
    }
}
