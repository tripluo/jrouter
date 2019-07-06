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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.jrouter.JRouterException;
import net.jrouter.impl.MultiParameterConverterFactory.MultiParameterConverter;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * 测试{@link MultiParameterConverter}转换参数。
 */
public class MultiParameterConverterTest {

    @Test
    public void testConvert() throws Exception {

        Object obj = new Object();
        CharSequence str1 = "String_1";
        CharSequence str2 = "String_2";
        CharSequence sb1 = new StringBuilder("StringBuilder_1");
        CharSequence sb2 = new StringBuilder("StringBuilder_2");

        //根据动态参数构建转换器，测试不缓存方法参数匹配的位置。
        MultiParameterConverter c = new MultiParameterConverterFactory(false).new MultiParameterConverter();
        Object[] convert = null;
        //test()
        assertArrayEquals(new Object[0], testConvertMethod(c, "test", null, null));
        assertArrayEquals(new Object[0], testConvertMethod(c, "test", null, new Object[0]));

        //test1(Object obj)
        assertArrayEquals(new Object[1], testConvertMethod(c, "test1", null, null));
        assertArrayEquals(new Object[]{obj}, testConvertMethod(c, "test1", new Object[]{obj}, null));
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test1", new Object[]{null, str1}, null));
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test1", new Object[]{str1}, null));
        assertArrayEquals(new Object[]{str2}, testConvertMethod(c, "test1", new Object[]{str2, str1}, null));

        //test2(String str)
        assertArrayEquals(new Object[1], testConvertMethod(c, "test2", null, null));
        assertArrayEquals(new Object[1], testConvertMethod(c, "test2", new Object[]{obj}, null));
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test2", new Object[]{str1}, null));
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test2", new Object[]{obj, str1}, null));

        //test3(Object obj, String str)
        assertArrayEquals(new Object[]{obj, null}, testConvertMethod(c, "test3", new Object[]{obj}, null));
        assertArrayEquals(new Object[]{str1, null}, testConvertMethod(c, "test3", new Object[]{str1}, null));
        assertArrayEquals(new Object[]{str1, null}, testConvertMethod(c, "test3", new Object[]{str1, obj}, null));
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test3", new Object[]{str1, str2, obj}, null));

        assertArrayEquals(new Object[]{obj, str2}, testConvertMethod(c, "test3", new Object[]{obj, str2}, null));
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test3", new Object[]{str1, obj, str2}, null));
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test3", new Object[]{str1, str2, obj, str2}, null));

        //test4(String str, Object obj)
        assertArrayEquals(new Object[]{null, obj}, testConvertMethod(c, "test4", new Object[]{obj}, null));
        assertArrayEquals(new Object[]{str1, obj}, testConvertMethod(c, "test4", new Object[]{str1, obj}, null));
        assertArrayEquals(new Object[]{str1, null}, testConvertMethod(c, "test4", new Object[]{str1}, null));
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test4", new Object[]{str1, str2}, null));
        assertArrayEquals(new Object[]{str1, obj}, testConvertMethod(c, "test4", new Object[]{obj, sb2, str1, str2}, null));
        assertArrayEquals(new Object[]{str2, sb2}, testConvertMethod(c, "test4", new Object[]{str2, sb2, str1, str2}, null));

        //test5(String str, StringBuilder sb)
        assertArrayEquals(new Object[2], testConvertMethod(c, "test5", new Object[]{obj}, null));
        convert = new Object[]{sb1};
        assertArrayEquals(new Object[]{null, sb1}, testConvertMethod(c, "test5", new Object[]{sb1}, null));
        assertArrayEquals(new Object[]{null, sb1}, testConvertMethod(c, "test5", new Object[]{obj, sb1}, null));

        //test6(CharSequence s1, CharSequence s2, String obj)
        assertArrayEquals(new Object[3], testConvertMethod(c, "test6", new Object[]{obj}, null));
        assertArrayEquals(new Object[3], testConvertMethod(c, "test6", new Object[]{obj, obj, obj}, null));
        assertArrayEquals(new Object[]{str1, str2, null}, testConvertMethod(c, "test6", new Object[]{str1, str2}, null));
        assertArrayEquals(new Object[]{str1, str2, null}, testConvertMethod(c, "test6", new Object[]{obj, str1, str2}, null));
        assertArrayEquals(new Object[]{str1, str2, null}, testConvertMethod(c, "test6", new Object[]{null, obj, str1, str2}, null));
        assertArrayEquals(new Object[]{str1, str2, null}, testConvertMethod(c, "test6", new Object[]{obj, obj, obj, str1, str2}, null));
        assertArrayEquals(new Object[]{sb1, sb2, null}, testConvertMethod(c, "test6", new Object[]{sb1, sb2}, null));
        assertArrayEquals(new Object[]{sb1, sb2, null}, testConvertMethod(c, "test6", new Object[]{obj, sb1, sb2}, null));
        assertArrayEquals(new Object[]{str1, sb2, str2}, testConvertMethod(c, "test6", new Object[]{obj, str1, sb2, str2}, null));

        //test7(int n, byte[] bytes, Number[] numbers, Boolean boo, CharSequence... args)
        assertArrayEquals(new Object[5], testConvertMethod(c, "test7", new Object[]{obj}, null));
        assertArrayEquals(new Object[]{100, new byte[1], new Integer[2], true, null}, testConvertMethod(c, "test7", new Object[]{100, new byte[1], new Integer[2], true}, null));
        assertArrayEquals(new Object[]{100, new byte[1], new Long[2], true, null}, testConvertMethod(c, "test7", new Object[]{100, new byte[1], new Long[2], true, new Object[10]}, null));
        assertArrayEquals(new Object[]{100, new byte[1], new Number[2], true, new String[10]}, testConvertMethod(c, "test7", new Object[]{100, new byte[1], new Integer[2], true, new String[10]}, null));
        assertArrayEquals(new Object[]{100, new byte[1], new Integer[2], true, new StringBuilder[10]}, testConvertMethod(c, "test7", new Object[]{100, new byte[1], new Integer[2], true, new StringBuilder[10]}, null));
        //error for array match
        //assertArrayEquals(new Object[]{1, new byte[1], new Long[2], true}, testConvertMethod(c, "test7", new Object[]{Integer.decode("1"), new Byte[1], new long[2], true}, null));
        assertArrayEquals(new Object[]{1, new byte[1], new Long[2], true, null}, testConvertMethod(c, "test7", new Object[]{Integer.decode("1"), new byte[1], new Long[2], true}, null));

    }

    /**
     * @see MultiParameterConverter#convert
     */
    private static Object[] testConvertMethod(MultiParameterConverter converter, String method,
                                              Object[] originalParams, Object[] convertParams) throws JRouterException {
        return converter.convert(TestAction.TEST_METHODS.get(method), null, originalParams, convertParams);
    }

    /**
     * TestAction.
     */
    private static class TestAction {

        //test methods
        static final Map<String, Method> TEST_METHODS = new HashMap<>(8);

        static {
            Method[] methods = TestAction.class.getDeclaredMethods();
            for (Method m : methods) {
                TEST_METHODS.put(m.getName(), m);
            }
            assertTrue(TEST_METHODS.size() >= 7);
        }

        public void test() {
        }

        public void test1(Object obj) {
        }

        public void test2(String str) {
        }

        public void test3(Object obj, String str) {
        }

        public void test4(String str, Object obj) {
        }

        public void test5(String str, StringBuilder sb) {
        }

        public void test6(CharSequence s1, CharSequence s2, String obj) {
        }

        public void test7(int n, byte[] bytes, Number[] numbers, Boolean boo, CharSequence... args) {
        }
    }
}
