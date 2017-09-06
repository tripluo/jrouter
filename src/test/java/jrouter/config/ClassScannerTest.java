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
package jrouter.config;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import jrouter.interceptor.DefaultInterceptorStack;
import jrouter.interceptor.DemoInterceptor;
import jrouter.interceptor.DemoThreadActionContextInterceptor;
import jrouter.interceptor.SampleInterceptor;
import jrouter.result.DefaultResult;
import jrouter.result.DemoResult;
import jrouter.util.CollectionUtil;
import static org.junit.Assert.*;
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
     * Test of calculateScanComponents method, of class ClassScanner.
     */
    @Test
    public void testCalculateScanComponents() throws Exception {
        assertTrue(classScanner.calculateScanComponents().isEmpty());

        //include packages
        classScanner.setIncludePackages(stringToSet("jrouter.result, jrouter.interceptor"));
        assertCollectionEqualContains(
                new Class<?>[]{
                    DefaultInterceptorStack.class,
                    SampleInterceptor.class,
                    DefaultResult.class,
                    DemoInterceptor.class,
                    DemoThreadActionContextInterceptor.class,
                    DemoResult.class
                },
                classScanner.calculateScanComponents());
        clear();

        //include expressions
        classScanner.setIncludePackages(stringToSet("jrouter.result, jrouter.interceptor"));
        classScanner.setIncludeExpressions(stringToSet("**.Demo*, **.Default*"));
        assertCollectionEqualContains(
                new Class<?>[]{
                    DefaultInterceptorStack.class,
                    DemoThreadActionContextInterceptor.class,
                    DefaultResult.class,
                    DemoInterceptor.class,
                    DemoResult.class
                },
                classScanner.calculateScanComponents());

        clear();

        //include expressions
        classScanner.setIncludePackages(stringToSet("jrouter"));
        classScanner.setIncludeExpressions(stringToSet("jrouter.interceptor.DefaultInterceptorStack"));
        assertCollectionEqualContains(
                new Class<?>[]{
                    DefaultInterceptorStack.class,},
                classScanner.calculateScanComponents());
        clear();

        //include expressions
        classScanner.setIncludePackages(stringToSet("jrouter.result, jrouter.interceptor"));
        classScanner.setIncludeExpressions(stringToSet("**.Demo*, **.Default*"));
        assertCollectionEqualContains(
                new Class<?>[]{
                    DefaultInterceptorStack.class,
                    DemoThreadActionContextInterceptor.class,
                    DefaultResult.class,
                    DemoInterceptor.class,
                    DemoResult.class
                },
                classScanner.calculateScanComponents());

        clear();

        //exclude expressions
        classScanner.setIncludePackages(stringToSet("jrouter"));
        classScanner.setExcludeExpressions(stringToSet("jrouter.**"));
        assertTrue(classScanner.calculateScanComponents().isEmpty());

        //exclude expressions
        classScanner.setIncludePackages(stringToSet("jrouter.result, jrouter.interceptor"));
        classScanner.setExcludeExpressions(stringToSet("jrouter.result.**, jrouter.interceptor.**"));
        assertTrue(classScanner.calculateScanComponents().isEmpty());
        clear();

        //exclude expressions
        classScanner.setIncludePackages(stringToSet("jrouter.result, jrouter.interceptor"));
        classScanner.setExcludeExpressions(stringToSet("jrouter.interceptor.*"));
        assertCollectionEqualContains(
                new Class<?>[]{
                    DefaultResult.class,
                    DemoResult.class
                },
                classScanner.calculateScanComponents());
        clear();

        //exclude expressions
        classScanner.setIncludePackages(stringToSet("jrouter.result, jrouter.interceptor"));
        classScanner.setExcludeExpressions(stringToSet("jrouter.interceptor.DefaultInterceptorStack"));
        assertCollectionEqualContains(
                new Class<?>[]{
                    SampleInterceptor.class,
                    DefaultResult.class,
                    DemoInterceptor.class,
                    DemoThreadActionContextInterceptor.class,
                    DemoResult.class
                },
                classScanner.calculateScanComponents());
        clear();

        //exclude expressions
        classScanner.setIncludePackages(stringToSet("jrouter.result, jrouter.interceptor"));
        classScanner.setExcludeExpressions(stringToSet("**.Demo*"));
        assertCollectionEqualContains(
                new Class<?>[]{
                    DefaultInterceptorStack.class,
                    SampleInterceptor.class,
                    DefaultResult.class,},
                classScanner.calculateScanComponents());
        clear();

        //include and exclude expressions
        classScanner.setIncludePackages(stringToSet("jrouter.result, jrouter.interceptor"));
        classScanner.setIncludeExpressions(stringToSet("**.Demo*, **.Default*"));
        classScanner.setExcludeExpressions(stringToSet("**.Demo*, jrouter.interceptor.DefaultInterceptorStack"));
        assertCollectionEqualContains(
                new Class<?>[]{
                    DefaultResult.class
                },
                classScanner.calculateScanComponents());
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
