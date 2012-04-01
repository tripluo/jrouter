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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * AntPathMatcherTest。
 */
public class AntPathMatcherTest {

    @Test
    public void tesMatch() {
        AntPathMatcher matcher = new AntPathMatcher();

        assertTrue(matcher.match("a", "a"));
        assertTrue(matcher.match("abb", "abb"));

        assertFalse(matcher.match("?", ""));
        assertFalse(matcher.match("?", " "));
        assertTrue(matcher.match("?", "a"));
        assertFalse(matcher.match("?", "aa"));

        assertTrue(matcher.match("???", "aaa"));
        assertFalse(matcher.match("a?", "a"));
        assertFalse(matcher.match("?b", "b"));

        assertTrue(matcher.match("?b", "ab"));
        assertTrue(matcher.match("?b", "bb"));
        assertFalse(matcher.match("?b", "ba"));
        assertTrue(matcher.match("a?c", "abc"));
        assertTrue(matcher.match("a?cd?", "abcde"));


        assertFalse(matcher.match("*", ""));
        assertFalse(matcher.match("*", "   "));
        assertTrue(matcher.match("*", "a"));
        assertTrue(matcher.match("*", "aa"));
        assertTrue(matcher.match("**", "a"));
        assertTrue(matcher.match("*****", "a"));
        assertTrue(matcher.match("****", "aaabbb"));

        assertTrue(matcher.match("a*", "a"));
        assertTrue(matcher.match("*b", "b"));

        assertTrue(matcher.match("*b", "ab"));
        assertTrue(matcher.match("*b", "bb"));
        assertFalse(matcher.match("*b", "ba"));
        assertTrue(matcher.match("*b", "abab"));
        assertTrue(matcher.match("a*c", "ac"));
        assertTrue(matcher.match("a*c", "abbbc"));
        assertTrue(matcher.match("a*cd*", "abcd"));
        assertTrue(matcher.match("a*cd*", "abbbcdeee"));

        assertFalse(matcher.match("?", "/"));
        assertFalse(matcher.match("*", "/"));
        assertFalse(matcher.match("*", "abc/123"));


        assertTrue(matcher.match("/*", "/"));
        assertTrue(matcher.match("/???", "/abc"));
        assertTrue(matcher.match("?/???", "1/abc"));

        assertFalse(matcher.match("/*", "abc"));
        assertTrue(matcher.match("/*", "/abc"));
        assertFalse(matcher.match("/*", "/abc/123"));

        assertTrue(matcher.match("/*/*", "/abc/123"));
        assertFalse(matcher.match("/*/*", "/abc/123/456"));
        assertFalse(matcher.match("/*/0*", "/abc/123"));
        assertTrue(matcher.match("/*/?/*", "/abc/d/123"));
        assertTrue(matcher.match("/*/?ef/*", "/abc/def/123"));

        assertTrue(matcher.match("/**", "/"));
        assertTrue(matcher.match("/**", "/abc/123"));
        assertTrue(matcher.match("/**", "/abc/123/456.abc"));
        assertTrue(matcher.match("/**/", "/abc/123"));
        assertTrue(matcher.match("/**", "/abc/123/456"));
        assertFalse(matcher.match("/*/", "/abc/123/456"));
        assertTrue(matcher.match("/**/", "/abc/123/456"));
        assertTrue(matcher.match("/**/*", "/abc/123/456"));
        assertTrue(matcher.match("/**/*/", "/abc/123/456"));

        assertFalse(matcher.match("/****", "/abc/123"));
        assertFalse(matcher.match("/**/?", "/abc/123/456"));
        assertTrue(matcher.match("/**/?", "/abc/1"));
        assertTrue(matcher.match("/**/???", "/abc/123"));
        assertTrue(matcher.match("/*/???", "/abc/123"));

        assertTrue(matcher.match("/**/*", "/abc/123/456"));
        assertTrue(matcher.match("/**/**", "/abc/123/456"));
        assertTrue(matcher.match("/**/**/**/**", "/abc/123/456/xyz"));

        assertFalse(matcher.match("/a**", "/abc/123/456"));
        assertTrue(matcher.match("/a**/**", "/abc/123/456"));

        assertFalse(matcher.match("/*/**Service", "/abc/123/456.Service"));
        assertFalse(matcher.match("**Service", "/abc/456.Service"));
        assertFalse(matcher.match("**Service", "/abc/456.Service"));
        assertFalse(matcher.match("/**Service", "/abc/456.Service"));
        assertTrue(matcher.match("/*/*Service", "/abc/456.Service"));

        assertTrue(matcher.match("/abc/123/456.Service", "/abc/123/456.Service"));
        assertTrue(matcher.match("/abc/123/456*Service", "/abc/123/456.Service"));
        assertTrue(matcher.match("/abc/123/*Service", "/abc/123/456.Service"));
        assertFalse(matcher.match("/abc/*Service", "/abc/123/456.Service"));
        assertTrue(matcher.match("/abc/**/*Service", "/abc/123/456.Service"));
        assertTrue(matcher.match("/abc/**/*Service", "/abc/123/456/789.Service"));

        assertTrue(matcher.match("/abc/**/*", "/abc/123/456.Service"));
        assertTrue(matcher.match("/abc/**", "/abc/123/456.Service"));
        assertTrue(matcher.match("/abc/**", "/abc/123/456/789.Service"));

        assertFalse(matcher.match("**Service1", "/abc/123/456.Service"));
        assertFalse(matcher.match("/**/*Service1", "/abc/123/456.Service"));
        assertTrue(matcher.match("/**/*Service", "/abc/123/456.Service"));
        assertTrue(matcher.match("/**/*Service*", "/abc/123/456.Service"));
        assertTrue(matcher.match("/**/**Service", "/abc/123/456.Service"));
    }

    @Test
    public void tesMatchStart() {
        AntPathMatcher matcher = new AntPathMatcher();

        assertTrue(matcher.matchStart("a", "a"));
        assertTrue(matcher.matchStart("abb", "abb"));

        assertTrue(matcher.matchStart("?", ""));
        assertTrue(matcher.matchStart("?", " "));
        assertTrue(matcher.matchStart("?", "a"));
        assertFalse(matcher.matchStart("?", "aa"));

        assertTrue(matcher.matchStart("???", "aaa"));
        assertFalse(matcher.matchStart("a?", "a"));
        assertFalse(matcher.matchStart("?b", "b"));

        assertTrue(matcher.matchStart("?b", "ab"));
        assertTrue(matcher.matchStart("?b", "bb"));
        assertFalse(matcher.matchStart("?b", "ba"));
        assertTrue(matcher.matchStart("a?c", "abc"));
        assertTrue(matcher.matchStart("a?cd?", "abcde"));


        assertTrue(matcher.matchStart("*", ""));
        assertTrue(matcher.matchStart("*", "   "));
        assertTrue(matcher.matchStart("*", "a"));
        assertTrue(matcher.matchStart("*", "aa"));
        assertTrue(matcher.matchStart("**", "a"));
        assertTrue(matcher.matchStart("*****", "a"));
        assertTrue(matcher.matchStart("****", "aaabbb"));

        assertTrue(matcher.matchStart("a*", "a"));
        assertTrue(matcher.matchStart("*b", "b"));

        assertTrue(matcher.matchStart("*b", "ab"));
        assertTrue(matcher.matchStart("*b", "bb"));
        assertFalse(matcher.matchStart("*b", "ba"));
        assertTrue(matcher.matchStart("*b", "abab"));
        assertTrue(matcher.matchStart("a*c", "ac"));
        assertTrue(matcher.matchStart("a*c", "abbbc"));
        assertTrue(matcher.matchStart("a*cd*", "abcd"));
        assertTrue(matcher.matchStart("a*cd*", "abbbcdeee"));


        assertFalse(matcher.matchStart("?", "/"));
        assertFalse(matcher.matchStart("*", "/"));
        assertFalse(matcher.matchStart("*", "abc/123"));


        assertTrue(matcher.matchStart("/*", "/"));
        assertTrue(matcher.matchStart("/???", "/abc"));
        assertTrue(matcher.matchStart("?/???", "1/abc"));

        assertFalse(matcher.matchStart("/*", "abc"));
        assertTrue(matcher.matchStart("/*", "/abc"));
        assertFalse(matcher.matchStart("/*", "/abc/123"));

        assertTrue(matcher.matchStart("/*/*", "/abc/123"));
        assertFalse(matcher.matchStart("/*/*", "/abc/123/456"));
        assertFalse(matcher.matchStart("/*/0*", "/abc/123"));
        assertTrue(matcher.matchStart("/*/?/*", "/abc/d/123"));
        assertTrue(matcher.matchStart("/*/?ef/*", "/abc/def/123"));

        assertTrue(matcher.matchStart("/**", "/"));
        assertTrue(matcher.matchStart("/**", "/abc/123"));
        assertTrue(matcher.matchStart("/**/", "/abc/123"));
        assertTrue(matcher.matchStart("/**", "/abc/123/456"));
        assertFalse(matcher.matchStart("/*/", "/abc/123/456"));
        assertTrue(matcher.matchStart("/**/", "/abc/123/456"));
        assertTrue(matcher.matchStart("/**/*", "/abc/123/456"));
        assertTrue(matcher.matchStart("/**/*/", "/abc/123/456"));

        assertFalse(matcher.matchStart("/****", "/abc/123"));
        assertTrue(matcher.matchStart("/**/?", "/abc/123/456"));
        assertTrue(matcher.matchStart("/**/?", "/abc/1"));
        assertTrue(matcher.matchStart("/**/???", "/abc/123"));
        assertTrue(matcher.matchStart("/*/???", "/abc/123"));

        assertTrue(matcher.matchStart("/**/*", "/abc/123/456"));
        assertTrue(matcher.matchStart("/**/**", "/abc/123/456"));
        assertTrue(matcher.matchStart("/**/**/**/**", "/abc/123/456/xyz"));

        assertFalse(matcher.matchStart("/a**", "/abc/123/456"));
        assertTrue(matcher.matchStart("/a**/**", "/abc/123/456"));

        assertFalse(matcher.matchStart("/*/**Service", "/abc/123/456.Service"));
        assertFalse(matcher.matchStart("**Service", "/abc/456.Service"));
        assertFalse(matcher.matchStart("/**Service", "/abc/456.Service"));
        assertTrue(matcher.matchStart("/*/*Service", "/abc/456.Service"));

        assertTrue(matcher.matchStart("/**/*Service", "/abc/123/456.Service"));
        assertTrue(matcher.matchStart("/**/*Service*", "/abc/123/456.Service"));
        assertTrue(matcher.matchStart("/**/**Service", "/abc/123/456.Service"));

    }
}
