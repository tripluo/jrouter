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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * CollectionUtilTest。
 */
public class CollectionUtilTest {

    /**
     * Test of stringToCollection method, of class CollectionUtil.
     */
    @Test
    public void testStringToCollection() {

        char[] sep = {',', '|'};

        assertArrayEquals(new String[]{""}, CollectionUtil.stringToCollection("", null).toArray());
        assertArrayEquals(new String[]{"a"}, CollectionUtil.stringToCollection("a", null).toArray());
        assertArrayEquals(new String[]{"a,bb"}, CollectionUtil.stringToCollection("a,bb", null).toArray());

        assertArrayEquals(new String[]{}, CollectionUtil.stringToCollection("", null, sep).toArray());
        assertArrayEquals(new String[]{}, CollectionUtil.stringToCollection("  ", null, sep).toArray());
        assertArrayEquals(new String[]{}, CollectionUtil.stringToCollection(" , ,, ", null, sep).toArray());

        assertArrayEquals(new String[]{"a"}, CollectionUtil.stringToCollection("a", null, sep).toArray());
        assertArrayEquals(new String[]{"a"}, CollectionUtil.stringToCollection("  a   ", null, sep).toArray());
        assertArrayEquals(new String[]{"a"}, CollectionUtil.stringToCollection(",a", null, sep).toArray());
        assertArrayEquals(new String[]{"a"}, CollectionUtil.stringToCollection("a,", null, sep).toArray());
        assertArrayEquals(new String[]{"a"}, CollectionUtil.stringToCollection("  a,,,", null, sep).toArray());


        assertArrayEquals(new String[]{"a", "b"}, CollectionUtil.stringToCollection("a,b", null, sep).toArray());
        assertArrayEquals(new String[]{"a", "b"}, CollectionUtil.stringToCollection("a,, b   ", null, sep).toArray());
        assertArrayEquals(new String[]{"a", "bb"}, CollectionUtil.stringToCollection("a,bb", null, sep).toArray());
        assertArrayEquals(new String[]{"a", "bb"}, CollectionUtil.stringToCollection(" a,bb ,", null, sep).toArray());
        assertArrayEquals(new String[]{"a", "bb"}, CollectionUtil.stringToCollection(" a, , bb ,,", null, sep).toArray());

        assertArrayEquals(new String[]{"a", "b", "c"}, CollectionUtil.stringToCollection("a,b,c", null, sep).toArray());
        assertArrayEquals(new String[]{"a", "b", "c"}, CollectionUtil.stringToCollection("  a , b ,  c", null, sep).toArray());

        assertArrayEquals(new String[]{"a", "bb", "xzy"}, CollectionUtil.stringToCollection("a,bb,xzy", null, sep).toArray());
        assertArrayEquals(new String[]{"a", "bb", "xzy"}, CollectionUtil.stringToCollection("a, bb, xzy", null, sep).toArray());
        assertArrayEquals(new String[]{"a", "bb", "xzy"}, CollectionUtil.stringToCollection("a,  bb,  xzy,,,,", null, sep).toArray());

        assertArrayEquals(new String[]{"abc", "bb", "x"}, CollectionUtil.stringToCollection("abc,  bb,x", null, sep).toArray());

        assertArrayEquals(new String[]{"a", "bb", "xzy"}, CollectionUtil.stringToCollection("a,bb|xzy", null, sep).toArray());
        assertArrayEquals(new String[]{"a", "bb", "xzy"}, CollectionUtil.stringToCollection("a, bb | xzy ,,||", null, sep).toArray());

    }

    /**
     * Test of stringToMap method, of class CollectionUtil.
     */
    @Test
    public void testStringToMap() {

        char[] sep = {',', '|'};

        assertEquals("{}", CollectionUtil.stringToMap("", null).toString());
        assertEquals("{}", CollectionUtil.stringToMap("a", null).toString());
        assertEquals("{}", CollectionUtil.stringToMap("a=a", null).toString());
        assertEquals("{}", CollectionUtil.stringToMap("", null, sep).toString());
        assertEquals("{}", CollectionUtil.stringToMap("a", null, sep).toString());
        assertEquals("{}", CollectionUtil.stringToMap("a==a", null, sep).toString());

        assertEquals("{a=a}", CollectionUtil.stringToMap("  a=a", null, sep).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap("a=a   ", null, sep).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap(",a=a", null, sep).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap("a=a,  ", null, sep).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap("a=a ,,,", null, sep).toString());

        assertEquals("{bb=bb}", CollectionUtil.stringToMap("bb=bb", null, sep).toString());
        assertEquals("{bb=ccc}", CollectionUtil.stringToMap("a=bb=ccc", null, sep).toString());
        assertEquals("{a=a, bb=bb}", CollectionUtil.stringToMap("a=a, bb=bb, c=xyz==xyz,", null, sep).toString());

        assertEquals("{a=a, bb=bb, c=c}", CollectionUtil.stringToMap("a=a,  bb=bb,  c=c  ", null, sep).toString());
        assertEquals("{a=a, bb=bb, c=c}", CollectionUtil.stringToMap("a=a  ,, bb=bb,c=c,", null, sep).toString());
        assertEquals("{a=a, bb=bb, xyz=xyz}", CollectionUtil.stringToMap("a=a,bb=bb,xyz=xyz", null, sep).toString());
        assertEquals("{a=a, bb=bb, xyz=xyz}", CollectionUtil.stringToMap(" a=a, bb=bb, xyz=xyz,", null, sep).toString());
        assertEquals("{a=a, bb=bb, xyz=xyz}", CollectionUtil.stringToMap(" a:a, bb:bb, xyz:xyz,", null, sep).toString());
        assertEquals("{aaa=aaa, bb=bb, xyz=xyz}", CollectionUtil.stringToMap("aaa=aaa, bb=bb, xyz=xyz", null, sep).toString());
        assertEquals("{aaa=aaa, bb=bb, xyz=xyz}", CollectionUtil.stringToMap("aaa=aaa|||bb=bb, xyz=xyz", null, sep).toString());
        assertEquals("{aaa=aaa, bb=bb, xyz=xyz}", CollectionUtil.stringToMap("aaa:aaa | bb=bb, xyz:xyz ,||", null, sep).toString());

    }
}
