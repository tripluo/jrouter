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

/**
 * 字符串工具类。
 */
public class StringUtil {

    /**
     * 判断CharSequence是否为空。
     * @param cs 待检测的CharSequence。
     * @return CharSequence为null或者为空则返回{@code true}。
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 判断CharSequence是否不为空。
     * @param cs 待检测的CharSequence。
     * @return CharSequence不为null且不为空则返回{@code true}。
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return !(cs == null || cs.length() == 0);
    }

    /**
     * 判断CharSequence是否为空或空白。
     * @param cs 待检测的CharSequence。
     * @return CharSequence为null、空或者空白则返回{@code true}。
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(cs.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断CharSequence是否不为空或空白。
     * @param cs 待检测的CharSequence。
     * @return CharSequence为null、空或者空白则返回{@code true}。
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !StringUtil.isBlank(cs);
    }

    /**
     * 去除字符串首尾的空格和特定字符。
     * @param src 原字符串。
     * @param ch 指定字符。
     * @return 去除首尾空白和指定字符后的字符串。
     */
    public static String trim(String src, char ch) {
        if (isEmpty(src)) {
            return src;
        }
        int begin = 0;
        int end = src.length() - 1;
        while (begin < end) {
            char c = src.charAt(begin);
            if (c == ch || Character.isWhitespace(c)) {
                begin++;
            }
            else {
                break;
            }
        }
        while (end >= begin) {
            char c = src.charAt(end);
            if (c == ch || Character.isWhitespace(c)) {
                end--;
            }
            else {
                break;
            }
        }
        return src.substring(begin, end + 1);
    }

    /**
     * 去除字符串首尾的空格和特定字符数组。
     * @param src 原字符串。
     * @param chs 指定字符数组。
     * @return 去除首尾空白和指定字符后的字符串。
     */
    public static String trim(String src, char... chs) {
        if (isEmpty(src)) {
            return src;
        }
        int begin = 0;
        int end = src.length() - 1;
        while (begin < end) {
            char c = src.charAt(begin);
            if (CollectionUtil.contains(c, chs) || Character.isWhitespace(c)) {
                begin++;
            }
            else {
                break;
            }
        }
        while (end >= begin) {
            char c = src.charAt(end);
            if (CollectionUtil.contains(c, chs) || Character.isWhitespace(c)) {
                end--;
            }
            else {
                break;
            }
        }
        return src.substring(begin, end + 1);
    }

}
