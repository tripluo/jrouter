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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 字符串工具类。
 */
public class StringUtil {

    /**
     *
     * 判断CharSequence是否为空。
     *
     * @param cs 待检测的CharSequence。
     *
     * @return CharSequence为null或者为空则返回{@code true}。
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     *
     * 判断CharSequence是否不为空。
     *
     * @param cs 待检测的CharSequence。
     *
     * @return CharSequence不为null且不为空则返回{@code true}。
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return !(cs == null || cs.length() == 0);
    }

    /**
     * 判断CharSequence是否为空或空白。
     *
     * @param cs 待检测的CharSequence。
     *
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
     *
     * @param cs 待检测的CharSequence。
     *
     * @return CharSequence为null、空或者空白则返回{@code true}。
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !StringUtil.isBlank(cs);
    }

    /**
     *
     * 去除字符串首尾的空格和特定字符。
     *
     * @param src 原字符串。
     * @param ch 指定字符。
     *
     * @return 去除首尾空白和指定字符后的字符串。
     */
    public static String trim(String src, char ch) {
        int begin = 0;
        int end = src.length() - 1;
        while (begin < end) {
            char c = src.charAt(begin);
            if (c == ch || Character.isWhitespace(c)) {
                begin++;
            } else {
                break;
            }
        }
        while (end >= begin) {
            char c = src.charAt(end);
            if (c == ch || Character.isWhitespace(c)) {
                end--;
            } else {
                break;
            }
        }
        return src.substring(begin, end + 1);
    }

    /**
     * Count the occurrences of the substring in specified string.
     *
     * @param str string to search in. Return 0 if this is null.
     * @param sub string to search for. Return 0 if this is null.
     *
     * @return count of the occurrences of the substring in specified string.
     */
    static int countOccurrencesOf(String str, String sub) {
        if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
            return 0;
        }
        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }

    /**
     * Tokenize the given String into a String array via a StringTokenizer. Trims tokens and omits
     * empty tokens. <p>The given delimiters string is supposed to consist of any number of
     * delimiter characters. Each of those characters can be used to separate tokens. A delimiter is
     * always a single character; for multi-character delimiters, consider using
     * <code>delimitedListToStringArray</code>
     *
     * @param str the String to tokenize
     * @param delimiters the delimiter characters, assembled as String (each of those characters is
     * individually considered as delimiter).
     * @return an array of the tokens
     * @see java.util.StringTokenizer
     * @see java.lang.String#trim()
     */
    static String[] tokenizeToStringArray(String str, String delimiters) {
        return tokenizeToStringArray(str, delimiters, true, true);
    }

    /**
     * Tokenize the given String into a String array via a StringTokenizer. <p>The given delimiters
     * string is supposed to consist of any number of delimiter characters. Each of those characters
     * can be used to separate tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using
     * <code>delimitedListToStringArray</code>
     *
     * @param str the String to tokenize
     * @param delimiters the delimiter characters, assembled as String (each of those characters is
     * individually considered as delimiter)
     * @param trimTokens trim the tokens via String's
     * <code>trim</code>
     * @param ignoreEmptyTokens omit empty tokens from the result array (only applies to tokens that
     * are empty after trimming; StringTokenizer will not consider subsequent delimiters as token in
     * the first place).
     * @return an array of the tokens (
     * <code>null</code> if the input String was
     * <code>null</code>)
     * @see java.util.StringTokenizer
     * @see java.lang.String#trim()
     */
    static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
            boolean ignoreEmptyTokens) {
        if (str == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return tokens.toArray(new String[tokens.size()]);
    }
}
