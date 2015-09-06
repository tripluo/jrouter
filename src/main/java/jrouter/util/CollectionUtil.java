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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 集合工具类。
 */
public class CollectionUtil {

    /**
     * An empty immutable {@code Object} array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * An empty immutable {@code Class} array.
     */
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    /**
     * An empty immutable {@code String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * An empty immutable {@code long} array.
     */
    public static final long[] EMPTY_LONG_ARRAY = new long[0];

    /**
     * An empty immutable {@code Long} array.
     */
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];

    /**
     * An empty immutable {@code int} array.
     */
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * An empty immutable {@code Integer} array.
     */
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];

    /**
     * An empty immutable {@code short} array.
     */
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];

    /**
     * An empty immutable {@code Short} array.
     */
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];

    /**
     * An empty immutable {@code byte} array.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * An empty immutable {@code Byte} array.
     */
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];

    /**
     * An empty immutable {@code double} array.
     */
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    /**
     * An empty immutable {@code Double} array.
     */
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];

    /**
     * An empty immutable {@code float} array.
     */
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

    /**
     * An empty immutable {@code Float} array.
     */
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];

    /**
     * An empty immutable {@code boolean} array.
     */
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

    /**
     * An empty immutable {@code Boolean} array.
     */
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];

    /**
     * An empty immutable {@code char} array.
     */
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /**
     * An empty immutable {@code Character} array.
     */
    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];

    /**
     * 判断数组是否为空。
     *
     * @param <T> 数组元素的类型。
     * @param a 待检测的数组。
     *
     * @return 数组为null或者为空则返回{@code true}。
     */
    public static <T> boolean isEmpty(T[] a) {
        return a == null || a.length == 0;
    }

    /**
     * 判断数组是否不为空。
     *
     * @param <T> 数组元素的类型。
     * @param a 待检测的数组。
     *
     * @return 数组不为null且不为空则返回{@code true}。
     */
    public static <T> boolean isNotEmpty(T[] a) {
        return !(a == null || a.length == 0);
    }

    /**
     *
     * 判断Collection是否为空。
     *
     * @param collection 待检测的Collection。
     *
     * @return Collection为null或者为空则返回{@code true}。
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     *
     * 判断Collection是否不为空。
     *
     * @param collection 待检测的Collection。
     *
     * @return Collection不为null且不为空则返回{@code true}。
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !(collection == null || collection.isEmpty());
    }

    /**
     *
     * 判断Map是否不为空。
     *
     * @param map 待检测的Map。
     *
     * @return Map不为null且不为空则返回{@code true}。
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     *
     * 判断Map是否不为空。
     *
     * @param map 待检测的Map。
     *
     * @return Map不为null且不为空则返回{@code true}。
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !(map == null || map.isEmpty());
    }

    /**
     * 指定数字是否包含于数字数组中。
     *
     * @param element 指定的数字。
     * @param array 指定的数字集合。
     *
     * @return 数字数组中存在此数字返回 true，否则返回false。
     */
    public static boolean contains(int element, int... array) {
        for (int t : array) {
            if (element == t)
                return true;
        }
        return false;
    }

    /**
     * 指定字符是否包含于字符数组中。
     *
     * @param element 指定的字符。
     * @param array 指定的字符集合。
     *
     * @return 字符数组中存在此字符返回 true，否则返回false。
     */
    public static boolean contains(char element, char... array) {
        for (char t : array) {
            if (element == t)
                return true;
        }
        return false;
    }

    /**
     * 指定boolean是否包含于boolean数组中。
     *
     * @param element 指定的boolean。
     * @param array 指定的boolean集合。
     *
     * @return boolean数组中存在此boolean返回 true，否则返回false。
     */
    public static boolean contains(boolean element, boolean... array) {
        for (boolean t : array) {
            if (element == t)
                return true;
        }
        return false;
    }

    /**
     * 依照指定的字符解析字符串至指定类型的集合。
     * 如果指定的集合为 null，则默认设置为{@link ArrayList}集合类型；
     * 如果原字符串为 null，则直接返回集合；
     * 如果字符分隔数组为空，则直接返回包含原字符串的集合。
     *
     * @param <T> 指定的集合类型。
     * @param source 原字符串。
     * @param collection 指定类型的集合。
     * @param sep 指定的字符分隔数组。
     *
     * @return 解析后的字符串集合。
     */
    public static <T extends Collection<String>> T stringToCollection(final String source,
            Collection<String> collection, char... sep) {
        if (collection == null)
            collection = new ArrayList<String>();
        if (source == null) {
            return (T) collection;
        }
        if (sep.length == 0) {
            collection.add(source);
            return (T) collection;
        }
        int i = 0;
        int point = 0;
        int end = 0;
        for (; i < source.length(); i++) {
            //if separate
            if (contains(source.charAt(i), sep)) {
                //blank
                end = i - 1;
                while (Character.isWhitespace(source.charAt(point)))
                    point++;
                while (end > point && Character.isWhitespace(source.charAt(end)))
                    end--;
                if (point <= end)
                    collection.add(source.substring(point, end + 1));
                point = i + 1;
            }
        }
        //尾串
        if (i != point) {
            end = i - 1;
            while (point < i && Character.isWhitespace(source.charAt(point)))
                point++;
            while (end > point && Character.isWhitespace(source.charAt(end)))
                end--;
            if (point <= end)
                collection.add(source.substring(point, end + 1));
        }
        return (T) collection;
    }

    /**
     * 依照指定的字符解析字符串("key=value 或 key:value")至指定类型的键值映射。
     * 如果指定的映射为 null，则默认设置为{@link LinkedHashMap}映射类型；
     * 如果字符分隔数组为空，则直接返回映射。
     *
     * @param <T> 指定的映射类型。
     * @param source 原字符串。
     * @param map 指定类型的映射。
     * @param sep 指定的字符分隔数组。
     *
     * @return 解析后的字符串键值映射。
     */
    public static <T extends Map<String, String>> T stringToMap(final String source,
            Map<String, String> map, char... sep) {
        if (contains('=', sep) || contains(':', sep)) {
            throw new IllegalArgumentException("Separate array " + Arrays.toString(sep)
                    + " can't contain " + Arrays.toString(new char[]{'=', ':'}));
        }
        if (map == null)
            map = new LinkedHashMap<String, String>();
        if (StringUtil.isEmpty(source)) {
            return (T) map;
        }

        int i = 0;
        int point = 0;
        int end = 0;
        for (; i < source.length(); i++) {
            //if separate
            if (contains(source.charAt(i), sep)) {
                end = i - 1;
                while (Character.isWhitespace(source.charAt(point)))
                    point++;
                while (end > point && Character.isWhitespace(source.charAt(end)))
                    end--;
                if (point < end || (point == end && !contains(source.charAt(point), sep))) {
                    String[] kv = parseKeyValue(source, point, end);
                    map.put(kv[0], kv[1]);
                }
                point = i + 1;
            }
        }
        //尾串
        if (i != point) {
            end = i - 1;
            while (point < i && Character.isWhitespace(source.charAt(point)))
                point++;
            while (end > point && Character.isWhitespace(source.charAt(end)))
                end--;
            if (point < end || (point == end && !contains(source.charAt(point), sep))) {
                String[] kv = parseKeyValue(source, point, end);
                map.put(kv[0], kv[1]);
            }
        }
        return (T) map;
    }

    /**
     * 针对<code>stringToMap</code>方法的解析key:value对；左右索引已去空。
     *
     * @param source 原字符串。
     * @param beginIndex 起始索引（包括）。
     * @param endIndex - 结束索引（包括）。
     *
     * @return 指定的key:value对数组。
     *
     * @see #stringToMap(java.lang.String, java.util.Map, char...)
     */
    private static String[] parseKeyValue(String source, int beginIndex, int endIndex) {
        int point = -1;
        for (int i = beginIndex; i <= endIndex; i++) {
            char c = source.charAt(i);
            if (c == '=' || c == ':') {
                point = i;
                break;
            }
        }
        if (point == -1) {
            return new String[]{source.substring(beginIndex, endIndex + 1), ""};
        }
        int lp = point - 1;
        int rp = point + 1;
        while (lp > beginIndex && Character.isWhitespace(source.charAt(lp)))
            lp--;
        while (rp < endIndex && Character.isWhitespace(source.charAt(rp)))
            rp++;
        return new String[]{point == beginIndex ? "" : source.substring(beginIndex, lp + 1),
            point == endIndex ? "" : source.substring(rp, endIndex + 1)};
    }
}
