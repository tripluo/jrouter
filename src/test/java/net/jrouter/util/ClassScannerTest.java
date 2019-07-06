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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.jrouter.interceptor.*;
import net.jrouter.result.DefaultResult;
import net.jrouter.result.DemoResult;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * 基于类扫描工具的测试。
 */
public class ClassScannerTest {

    //类扫描工具
    private ClassScanner classScanner;

    @Before
    public void setUp() {
        classScanner = new ClassScanner();
    }

    /**
     * 清除类扫描工具ClassScanner的设置。
     */
    private void clear() {
        if (classScanner != null) {
            classScanner.setIncludePackages(Collections.EMPTY_SET);
            classScanner.setIncludeExpressions(Collections.EMPTY_SET);
            classScanner.setExcludeExpressions(Collections.EMPTY_SET);
        }
    }

    /**
     * Test of getClasses method, of class ClassScanner.
     */
    @Test
    public void testCalculateScanComponents() throws Exception {
        assertTrue(classScanner.getClasses().isEmpty());

        //include packages
        classScanner.setIncludePackages(stringToSet("net.jrouter.result, net.jrouter.interceptor"));
        assertCollectionEqualContains(new Class<?>[]{
                        DefaultInterceptorStack.class,
                        DemoInterceptorStack.class,
                        SampleInterceptor.class,
                        DefaultResult.class,
                        DemoInterceptor.class,
                        DemoThreadActionContextInterceptor.class,
                        DemoResult.class
                },
                classScanner.getClasses());
        clear();

        //include expressions
        classScanner.setIncludePackages(stringToSet("net.jrouter.result, net.jrouter.interceptor"));
        classScanner.setIncludeExpressions(stringToSet("**.Demo*, **.Default*"));
        assertCollectionEqualContains(new Class<?>[]{
                        DefaultInterceptorStack.class,
                        DemoInterceptorStack.class,
                        DemoThreadActionContextInterceptor.class,
                        DefaultResult.class,
                        DemoInterceptor.class,
                        DemoResult.class
                },
                classScanner.getClasses());

        clear();

        //include expressions
        classScanner.setIncludePackages(stringToSet("net.jrouter"));
        classScanner.setIncludeExpressions(stringToSet("net.jrouter.interceptor.DefaultInterceptorStack"));
        assertCollectionEqualContains(new Class<?>[]{
                        DefaultInterceptorStack.class,},
                classScanner.getClasses());
        clear();

        //include expressions
        classScanner.setIncludePackages(stringToSet("net.jrouter.result, net.jrouter.interceptor"));
        classScanner.setIncludeExpressions(stringToSet("**.Demo*, **.Default*"));
        assertCollectionEqualContains(new Class<?>[]{
                        DefaultInterceptorStack.class,
                        DemoInterceptorStack.class,
                        DemoThreadActionContextInterceptor.class,
                        DefaultResult.class,
                        DemoInterceptor.class,
                        DemoResult.class
                },
                classScanner.getClasses());

        clear();

        //exclude expressions
        classScanner.setIncludePackages(stringToSet("net.jrouter"));
        classScanner.setExcludeExpressions(stringToSet("net.jrouter.**"));
        assertTrue(classScanner.getClasses().isEmpty());

        //exclude expressions
        classScanner.setIncludePackages(stringToSet("net.jrouter.result, net.jrouter.interceptor"));
        classScanner.setExcludeExpressions(stringToSet("net.jrouter.result.**, net.jrouter.interceptor.**"));
        assertTrue(classScanner.getClasses().isEmpty());
        clear();

        //exclude expressions
        classScanner.setIncludePackages(stringToSet("net.jrouter.result, net.jrouter.interceptor"));
        classScanner.setExcludeExpressions(stringToSet("net.jrouter.interceptor.*"));
        assertCollectionEqualContains(new Class<?>[]{
                        DefaultResult.class,
                        DemoResult.class
                },
                classScanner.getClasses());
        clear();

        //exclude expressions
        classScanner.setIncludePackages(stringToSet("net.jrouter.result, net.jrouter.interceptor"));
        classScanner.setExcludeExpressions(stringToSet("net.jrouter.interceptor.DefaultInterceptorStack"));
        assertCollectionEqualContains(new Class<?>[]{
                        DemoInterceptorStack.class,
                        SampleInterceptor.class,
                        DefaultResult.class,
                        DemoInterceptor.class,
                        DemoThreadActionContextInterceptor.class,
                        DemoResult.class
                },
                classScanner.getClasses());
        clear();

        //exclude expressions
        classScanner.setIncludePackages(stringToSet("net.jrouter.result, net.jrouter.interceptor"));
        classScanner.setExcludeExpressions(stringToSet("**.Demo*"));
        assertCollectionEqualContains(new Class<?>[]{
                        DefaultInterceptorStack.class,
                        SampleInterceptor.class,
                        DefaultResult.class,},
                classScanner.getClasses());
        clear();

        //include and exclude expressions
        classScanner.setIncludePackages(stringToSet("net.jrouter.result, net.jrouter.interceptor"));
        classScanner.setIncludeExpressions(stringToSet("**.Demo*, **.Default*"));
        classScanner.setExcludeExpressions(stringToSet("**.Demo*, net.jrouter.interceptor.DefaultInterceptorStack"));
        assertCollectionEqualContains(new Class<?>[]{
                        DefaultResult.class
                },
                classScanner.getClasses());
        clear();
    }

    /**
     * 匹配集合元素完全相等，无视顺序。
     */
    private static <T> void assertCollectionEqualContains(T[] expected,
                                                          Collection<? extends T> actual) {
        if (expected.length != actual.size())
            fail("expected Collection size:<[" + expected.length + "]> but was:<[" + actual.size() + "]>");

        for (T t : expected) {
            if (!actual.contains(t)) {
                fail("expected Collection element:<[" + t + "]>");
            }
        }
    }

    /**
     * 匹配集合元素完全相等，无视顺序。
     */
    private static <T> void assertCollectionEqualContains(Collection<? extends T> expected,
                                                          Collection<? extends T> actual) {
        if (expected.size() != actual.size())
            fail("expected Collection size:<[" + expected.size() + "]> but was:<[" + actual.size() + "]>");

        for (T t : expected) {
            if (!actual.contains(t)) {
                fail("expected Collection element:<[" + t + "]>");
            }
        }
    }

    //convert String to LinkedHashSet
    private static Set<String> stringToSet(String strings) {
        return CollectionUtil.stringToCollection(strings, new LinkedHashSet<String>(), ',');
    }
}
