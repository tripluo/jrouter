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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * StringUtilTest。
 */
public class StringUtilTest {

    /** 路径分隔符 */
    public static final char PATH = '/';

    /**
     * 测试判断空值。
     */
    @Test
    public void testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty(" "));
        assertFalse(StringUtil.isEmpty("test"));

        assertFalse(StringUtil.isNotEmpty(null));
        assertFalse(StringUtil.isNotEmpty(""));
        assertTrue(StringUtil.isNotEmpty(" "));
        assertTrue(StringUtil.isNotEmpty("test"));
    }

    /**
     * 测试判断空或空白。
     */
    @Test
    public void testIsBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank(" "));
        assertFalse(StringUtil.isBlank("test"));

        assertFalse(StringUtil.isNotBlank(null));
        assertFalse(StringUtil.isNotBlank(""));
        assertFalse(StringUtil.isNotBlank(" "));
        assertTrue(StringUtil.isNotBlank("test"));
    }

    /**
     * 测试去除字符串首尾的空格和特定字符。
     */
    @Test
    public void testTrim() {
        assertEquals("", StringUtil.trim("", PATH));
        assertEquals("", StringUtil.trim("/", PATH));
        assertEquals("", StringUtil.trim("/  ", PATH));
        assertEquals("", StringUtil.trim("  /", PATH));
        assertEquals("", StringUtil.trim("  /   ", PATH));
        assertEquals("", StringUtil.trim("///   ", PATH));
        assertEquals("x", StringUtil.trim("x", PATH));
        assertEquals("x", StringUtil.trim("x  ", PATH));
        assertEquals("x", StringUtil.trim("/x", PATH));
        assertEquals("x", StringUtil.trim("x/", PATH));
        assertEquals("x", StringUtil.trim("//x", PATH));
        assertEquals("x", StringUtil.trim("x///", PATH));
        assertEquals("x", StringUtil.trim("/x/", PATH));
        assertEquals("x", StringUtil.trim("//x///", PATH));
        assertEquals("x/y", StringUtil.trim("/x/y/", PATH));
        assertEquals("test", StringUtil.trim("/test/", PATH));
        assertEquals("test", StringUtil.trim("/test/   ", PATH));
        assertEquals("test", StringUtil.trim(" /test   ", PATH));
        assertEquals("test", StringUtil.trim(" test/   ", PATH));
        assertEquals("test/123", StringUtil.trim(" /test/123////   ", PATH));
    }
}
