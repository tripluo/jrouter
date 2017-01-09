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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import jrouter.JRouterException;
import jrouter.impl.MultiParameterConverterFactory.MultiParameterConverter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试MultiParameterConverter转换参数。
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
        convert = new Object[]{obj};
        assertArrayEquals(null, testConvertMethod(c, "test", null, convert));  //invoke params
        assertArrayEquals(new Object[0], testConvertMethod(c, "test", new Object[0], convert));  //invoke params

        //test1(Object obj)
        assertArrayEquals(new Object[]{obj}, testConvertMethod(c, "test1", null, convert));  //match
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test1", new Object[]{str1}, convert));  //invoke params

        convert = new Object[]{str1};
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test1", null, convert));  //match
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test1", new Object[]{str1}, convert)); //invoke params
        assertArrayEquals(new Object[]{str2}, testConvertMethod(c, "test1", new Object[]{str2}, convert)); //invoke params

        //test2(String str)
        convert = new Object[]{obj, sb1};
        assertArrayEquals(new Object[]{null}, testConvertMethod(c, "test2", null, convert));  //no match fill null
        assertArrayEquals(new Object[]{obj}, testConvertMethod(c, "test2", new Object[]{obj}, convert));  //invoke params
        convert = new Object[]{str1};
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test2", null, convert));  //match
        assertArrayEquals(new Object[]{obj}, testConvertMethod(c, "test2", new Object[]{obj}, convert));  //invoke params

        //test3(Object obj, String str)
        convert = new Object[]{obj};
        assertArrayEquals(new Object[]{obj, null}, testConvertMethod(c, "test3", null, convert));  //match & fill null
        assertArrayEquals(new Object[]{str1, null}, testConvertMethod(c, "test3", new Object[]{str1}, convert));  //invoke params & no match
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test3", new Object[]{str1, str2}, convert));  //invoke params

        convert = new Object[]{obj, str2};
        assertArrayEquals(new Object[]{obj, str2}, testConvertMethod(c, "test3", null, convert));  //match
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test3", new Object[]{str1}, convert));  //invoke params & match
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test3", new Object[]{str1, str2}, convert));  //invoke params

        //test4(String str, Object obj)
        convert = new Object[]{obj};
        assertArrayEquals(new Object[]{null, obj}, testConvertMethod(c, "test4", null, convert));  //match & fill null
        assertArrayEquals(new Object[]{str1, obj}, testConvertMethod(c, "test4", new Object[]{str1}, convert));  //invoke params & match
        convert = new Object[]{str1};
        assertArrayEquals(new Object[]{str1, null}, testConvertMethod(c, "test4", null, convert));  //match
        convert = new Object[]{str1, str2};
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test4", null, convert));  //match
        assertArrayEquals(new Object[]{obj, sb2}, testConvertMethod(c, "test4", new Object[]{obj, sb2}, convert));  //invoke params
        assertArrayEquals(new Object[]{str2, sb2}, testConvertMethod(c, "test4", new Object[]{str2, sb2}, convert));  //invoke params

        //test5(String str, StringBuilder sb)
        convert = new Object[]{obj};
        assertArrayEquals(new Object[]{null, null}, testConvertMethod(c, "test5", null, convert));  //no match & fill null
        convert = new Object[]{sb1};
        assertArrayEquals(new Object[]{null, sb1}, testConvertMethod(c, "test5", null, convert));  //match & fill null
        assertArrayEquals(new Object[]{obj, sb1}, testConvertMethod(c, "test5", new Object[]{obj}, convert));  //invoke params & match

        //test6(CharSequence s1, CharSequence s2, String obj)
        convert = new Object[]{obj};
        assertArrayEquals(new Object[]{null, null, null}, testConvertMethod(c, "test6", null, convert));  //no match & fill null
        assertArrayEquals(new Object[]{obj, null, null}, testConvertMethod(c, "test6", new Object[]{obj}, convert));  //invoke params & no match & fill null
        convert = new Object[]{str1, str2};
        assertArrayEquals(new Object[]{str1, str2, null}, testConvertMethod(c, "test6", null, convert));  //match
        assertArrayEquals(new Object[]{obj, str1, str2}, testConvertMethod(c, "test6", new Object[]{obj}, convert));  //invoke params & match
        convert = new Object[]{sb1, sb2};
        assertArrayEquals(new Object[]{sb1, sb2, null}, testConvertMethod(c, "test6", null, convert));  //match & fill null
        assertArrayEquals(new Object[]{obj, sb1, null}, testConvertMethod(c, "test6", new Object[]{obj}, convert));  //invoke params & match & fill null
    }

    /**
     * @see MultiParameterConverter#convert(java.lang.reflect.Method, java.lang.Object, java.lang.Object[], java.lang.Object[])
     */
    private static Object[] testConvertMethod(MultiParameterConverter converter, String method,
            Object[] invokeParams, Object[] convertParams) throws JRouterException {
        return converter.convert(TestAction.TEST_METHODS.get(method), null, invokeParams, convertParams);
    }

    /**
     * TestAction.
     */
    private static class TestAction {

        //test methods
        static final Map<String, Method> TEST_METHODS = new HashMap<String, Method>(8);

        static {
            Method[] methods = TestAction.class.getDeclaredMethods();
            for (Method m : methods) {
                TEST_METHODS.put(m.getName(), m);
            }
            assertTrue(TEST_METHODS.size() == 7);
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

    }
}
