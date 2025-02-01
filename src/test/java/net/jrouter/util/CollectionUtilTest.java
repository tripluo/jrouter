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

        char[] sep = { ',', '|' };

        assertArrayEquals(new String[0], CollectionUtil.stringToCollection(null, null, sep).toArray());
        assertArrayEquals(new String[] { "" }, CollectionUtil.stringToCollection("", null).toArray());
        assertArrayEquals(new String[] { "  " }, CollectionUtil.stringToCollection("  ", null).toArray());
        assertArrayEquals(new String[] { "a" }, CollectionUtil.stringToCollection("a", null).toArray());
        assertArrayEquals(new String[] { "a,bb" }, CollectionUtil.stringToCollection("a,bb", null).toArray());

        assertArrayEquals(new String[] {}, CollectionUtil.stringToCollection("", null, sep).toArray());
        assertArrayEquals(new String[] {}, CollectionUtil.stringToCollection("  ", null, sep).toArray());
        assertArrayEquals(new String[] {}, CollectionUtil.stringToCollection(",", null, sep).toArray());
        assertArrayEquals(new String[] {}, CollectionUtil.stringToCollection(" \r\n", null, sep).toArray());
        assertArrayEquals(new String[] {}, CollectionUtil.stringToCollection(" , ,, ", null, sep).toArray());

        assertArrayEquals(new String[] { "a" }, CollectionUtil.stringToCollection("a", null, sep).toArray());
        assertArrayEquals(new String[] { "aa" }, CollectionUtil.stringToCollection("aa", null, sep).toArray());
        assertArrayEquals(new String[] { "a" }, CollectionUtil.stringToCollection("  a   ", null, sep).toArray());
        assertArrayEquals(new String[] { "a" }, CollectionUtil.stringToCollection(",a \r", null, sep).toArray());
        assertArrayEquals(new String[] { "a" }, CollectionUtil.stringToCollection("a,", null, sep).toArray());
        assertArrayEquals(new String[] { "a" }, CollectionUtil.stringToCollection("  a,,,", null, sep).toArray());

        assertArrayEquals(new String[] { "a", "b" }, CollectionUtil.stringToCollection("a,b", null, sep).toArray());
        assertArrayEquals(new String[] { "a", "b" },
                CollectionUtil.stringToCollection("a,, b   ", null, sep).toArray());
        assertArrayEquals(new String[] { "a", "bb" }, CollectionUtil.stringToCollection("a,bb", null, sep).toArray());
        assertArrayEquals(new String[] { "a", "bb" },
                CollectionUtil.stringToCollection(" a,bb ,", null, sep).toArray());
        assertArrayEquals(new String[] { "a", "bb" },
                CollectionUtil.stringToCollection(" a, , bb ,,", null, sep).toArray());

        assertArrayEquals(new String[] { "a", "b", "c" },
                CollectionUtil.stringToCollection("a,b,c", null, sep).toArray());
        assertArrayEquals(new String[] { "a", "b", "c" },
                CollectionUtil.stringToCollection("  a , b ,  c", null, sep).toArray());

        assertArrayEquals(new String[] { "a", "bb", "xzy" },
                CollectionUtil.stringToCollection("a,bb,xzy", null, sep).toArray());
        assertArrayEquals(new String[] { "a", "bb", "xzy" },
                CollectionUtil.stringToCollection("a, bb, xzy", null, sep).toArray());
        assertArrayEquals(new String[] { "a", "bb", "xzy" },
                CollectionUtil.stringToCollection("a,  bb,  xzy,,,,", null, sep).toArray());

        assertArrayEquals(new String[] { "abc", "bb", "x" },
                CollectionUtil.stringToCollection("abc,  bb,x", null, sep).toArray());

        assertArrayEquals(new String[] { "a", "bb", "xzy" },
                CollectionUtil.stringToCollection("a,bb|xzy", null, sep).toArray());
        assertArrayEquals(new String[] { "a", "bb", "xzy" },
                CollectionUtil.stringToCollection("a, bb | xzy ,,||", null, sep).toArray());

    }

    /**
     * Test of stringToMap method, of class CollectionUtil.
     */
    @Test
    public void testStringToMap() {
        char[] sep = { ',', '|' };
        String[] nulls = { null, "", " ", "   ", ",", " ,", " ,", " ,  ", ",,", " ,,", " ,,  ", " ,,,,  " };
        for (String emtpy : nulls) {
            assertEquals("{}", CollectionUtil.stringToMap(emtpy, null, sep).toString());
        }
        String[] emtpys = { "=", " =", "   =", "= ", "=    ", "  =    ", ",=", "=,", " ,=", " , =", " =  ,", "=,," };
        for (String emtpy : emtpys) {
            assertEquals("{=}", CollectionUtil.stringToMap(emtpy, null, sep).toString());
        }

        assertEquals("{a=}", CollectionUtil.stringToMap("a", null).toString());
        assertEquals("{a,=}", CollectionUtil.stringToMap("a,", null).toString());
        assertEquals("{a=}", CollectionUtil.stringToMap("a,", null, sep).toString());
        assertEquals("{a=}", CollectionUtil.stringToMap(" a", null).toString());
        assertEquals("{a=}", CollectionUtil.stringToMap("   a", null).toString());
        assertEquals("{a=}", CollectionUtil.stringToMap("a ", null).toString());
        assertEquals("{a=}", CollectionUtil.stringToMap("a   ", null).toString());
        assertEquals("{a=}", CollectionUtil.stringToMap("  a   ", null).toString());
        assertEquals("{a=}", CollectionUtil.stringToMap("a=", null).toString());
        assertEquals("{a=}", CollectionUtil.stringToMap("a =", null).toString());
        assertEquals("{a=}", CollectionUtil.stringToMap(" a  =", null).toString());
        assertEquals("{=a}", CollectionUtil.stringToMap("=a", null).toString());
        assertEquals("{=a}", CollectionUtil.stringToMap("= a", null).toString());
        assertEquals("{=a}", CollectionUtil.stringToMap("=   a", null).toString());
        assertEquals("{=a}", CollectionUtil.stringToMap("=  a   ", null).toString());

        assertEquals("{a=a}", CollectionUtil.stringToMap("a=a", null).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap("a=a,", null, sep).toString());
        assertEquals("{a=}", CollectionUtil.stringToMap("a=,a", null, sep).toString());
        assertEquals("{a=, b=}", CollectionUtil.stringToMap("a= ,b", null, sep).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap("a = a", null).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap("  a=a", null, sep).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap("a=a   ", null, sep).toString());
        assertEquals("{==a}", CollectionUtil.stringToMap("==a", null, sep).toString());
        assertEquals("{==a}", CollectionUtil.stringToMap("= =a", null, sep).toString());
        assertEquals("{a==a}", CollectionUtil.stringToMap("a==a", null, sep).toString());
        assertEquals("{a==a}", CollectionUtil.stringToMap(" a  ==a", null, sep).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap(",a=a", null, sep).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap("a=a,  ", null, sep).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap("a=a ,,,", null, sep).toString());
        assertEquals("{a=a}", CollectionUtil.stringToMap(",,a=a ,,,", null, sep).toString());

        assertEquals("{a=a=}", CollectionUtil.stringToMap("a=a= ,,,", null, sep).toString());
        assertEquals("{a=a=, b=}", CollectionUtil.stringToMap("a=a= ,,  b ,", null, sep).toString());
        assertEquals("{a=a=, =}", CollectionUtil.stringToMap("a=a= ,,  = ,", null, sep).toString());

        assertEquals("{bb=bb}", CollectionUtil.stringToMap("bb=bb", null, sep).toString());
        assertEquals("{bb==bb}", CollectionUtil.stringToMap("bb==bb", null, sep).toString());
        assertEquals("{a=bb=ccc}", CollectionUtil.stringToMap("a=bb=ccc", null, sep).toString());
        assertEquals("{a=a, bb=bb, c=xyz==xyz}",
                CollectionUtil.stringToMap("a=a, bb=bb, c=xyz==xyz,", null, sep).toString());

        assertEquals("{a=a, bb=bb, c=c}", CollectionUtil.stringToMap("a=a,  bb=bb,  c=c  ", null, sep).toString());
        assertEquals("{a=a, bb=bb, c=c}", CollectionUtil.stringToMap("a=a  ,, bb=bb,c=c,", null, sep).toString());
        assertEquals("{a=a, bb=bb, xyz=xyz}", CollectionUtil.stringToMap("a=a,bb=bb,xyz=xyz", null, sep).toString());
        assertEquals("{a=a, bb=bb, xyz=xyz}",
                CollectionUtil.stringToMap(" a=a, bb=bb, xyz=xyz,", null, sep).toString());
        assertEquals("{a=a, bb=bb, xyz=xyz}",
                CollectionUtil.stringToMap(" a:a, bb:bb, xyz:xyz,", null, sep).toString());
        assertEquals("{aaa=aaa, bb=bb, xyz=xyz}",
                CollectionUtil.stringToMap("aaa=aaa, bb=bb, xyz=xyz", null, sep).toString());
        assertEquals("{aaa=aaa, bb=bb, xyz=xyz}",
                CollectionUtil.stringToMap("aaa=aaa|||bb=bb, xyz=xyz", null, sep).toString());
        assertEquals("{aaa=aaa, bb=bb, xyz=xyz}",
                CollectionUtil.stringToMap("aaa:aaa | bb=bb, xyz:xyz ,||", null, sep).toString());
        assertEquals("{aaa=aaa, bb=bb, xyz=xyz}",
                CollectionUtil.stringToMap("aaa : aaa | bb= bb, xyz :xyz ,||", null, sep).toString());

    }

    /**
     * Test of append method, of class CollectionUtil.
     */
    @Test
    public void testAppend() {
        assertArrayEquals(new Object[0], CollectionUtil.append(null));
        assertArrayEquals(new Object[0], CollectionUtil.append(new Object[0]));
        assertArrayEquals(new Object[] { 1, 2 }, CollectionUtil.append(new Object[] { 1, 2 }));
        assertSame(null, CollectionUtil.append(null, null));
        assertArrayEquals(new Object[] { 1, 2 }, CollectionUtil.append(new Object[] { 1, 2 }, new Object[0]));
        assertArrayEquals(new Object[] { 1, 2 }, CollectionUtil.append(new Object[] { 1, 2 }, null));
        assertArrayEquals(new Object[] { 1, 2, null },
                CollectionUtil.append(new Object[] { 1, 2 }, new Object[] { null }));
        assertArrayEquals(new Object[] { "1", "2" }, CollectionUtil.append(new Object[0], new Object[] { "1", "2" }));
        assertArrayEquals(new Object[] { "1", "2" }, CollectionUtil.append(new Object[0], "1", "2"));
        assertArrayEquals(new Object[] { 0, "1", "2" }, CollectionUtil.append(new Object[] { 0 }, "1", "2"));
        assertArrayEquals(new Object[] { 1, 2, "1", "2" },
                CollectionUtil.append(new Object[] { 1, 2 }, new Object[] { "1", "2" }));
        assertArrayEquals(new Object[] { 1, 2, "1", "2" }, CollectionUtil.append(new Object[] { 1, 2 }, "1", "2"));
    }

    /**
     * Test of convert method, of class CollectionUtil.
     */
    @Test
    public void testConvert() {
        assertArrayEquals(null, CollectionUtil.convert((int[]) null));
        assertArrayEquals(new int[0], CollectionUtil.convert(new boolean[0]));

        assertArrayEquals(new boolean[] { false, true }, CollectionUtil.convert(new int[] { 1 }));
        assertArrayEquals(new boolean[] { true, true, false, true }, CollectionUtil.convert(new int[] { 1, 0, 3 }));

        assertArrayEquals(new int[] { 1 }, CollectionUtil.convert(new boolean[] { false, true }));
        assertArrayEquals(new int[] { 0, 1, 3 }, CollectionUtil.convert(new boolean[] { true, true, false, true }));
    }

}
