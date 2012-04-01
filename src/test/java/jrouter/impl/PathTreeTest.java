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

import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * PathTree树路径的测试。
 */
public class PathTreeTest {

    private PathTree<String> tree;

    /**
     * 模拟的树路径如下，@表示有值，叶子一定有值
     *                          /(root)
     * xx   {k1}             aa
     * yy            b1      b2      b3           *               b4            b5
     * zz            c1      c2    c1   *         c1             {k1}            *
     *             d0   d1   d2    d1   d1(@)     d1(@)      d1(@)   d2(@)       *
     *                            *     {k2}       e1         e1      {k2}       *
     *                                                                           *
     */
    //模拟路径数据
    public static final String[] PATHS = {
        "/xx/yy/zz",
        "/{k1}",
        "/aa/b1/c1/d0",
        "/aa/b1/c1/d1",
        "/aa/b2/c2/d2",
        "/aa/b3/c1/d1/*",
        "/aa/b3/*/d1",
        "/aa/b3/*/d1/{k2}",
        "/aa/*/c1/d1",
        "/aa/*/c1/d1/e1",
        "/aa/b4/{k1}/d1",
        "/aa/b4/{k1}/d2",
        "/aa/b4/{k1}/d2/e1",
        "/aa/b4/{k1}/d2/{k2}",
        "/aa/b5/*/*/*/*"
    };

    @After
    public void tearDown() {
        tree.clear();
    }

    /**
     * 初始化节点的值。
     */
    @Before
    public void testBefore() {
        tree = new PathTree<String>();
        for (String p : PATHS) {
            //返回 null 表示添加新节点的原节点无相关联的值
            assertNull(tree.put(p, p));
        }
    }

    /**
     * 测试添加节点。
     */
    @Test
    public void testPut() {
        //添加已经存在相关联值的路径时，覆盖原有路径的值
        assertEquals("/xx/yy/zz", tree.put("/xx/yy/zz", "/xx/yy/zz"));
        assertEquals("/xx/yy/zz", tree.get("/xx/yy/zz"));
        assertEquals("/xx/yy/zz", tree.put("/xx/yy/zz", "another value"));
        assertEquals("another value", tree.get("/xx/yy/zz"));
        assertEquals("another value", tree.put("/xx/yy/zz", "another value again"));
        assertEquals("another value again", tree.get("/xx/yy/zz"));
        assertEquals("another value again", tree.put("/xx/yy/zz", "/xx/yy/zz"));
        assertEquals("/xx/yy/zz", tree.get("/xx/yy/zz"));

        assertEquals("/{k1}", tree.put("/*", "/{k1}"));
        assertEquals("/{k1}", tree.put("/*", "another value"));
        assertEquals("another value", tree.put("/*", "another value again"));

        assertEquals("/aa/b1/c1/d0", tree.put("/aa/b1/c1/d0", "another value"));
        assertEquals("/aa/b3/c1/d1/*", tree.put("/aa/b3/c1/d1/*", "another value"));
        assertEquals("another value", tree.put("/aa/b3/c1/d1/{aaa}", "another value again"));
        assertEquals("/aa/b4/{k1}/d2/{k2}", tree.put("/aa/b4/{k1}/d2/*", "another value"));
        assertEquals("another value", tree.put("/aa/b4/{k1}/d2/*", "another value again"));
        assertEquals("another value again", tree.put("/aa/b4/{k1}/d2/*", "another value again 2"));
    }

    /**
     * 测试获取节点。
     */
    @Test
    public void testGet() {

        assertEquals("/xx/yy/zz", tree.get("/xx/yy/zz"));
        assertEquals("/aa/b1/c1/d0", tree.get("/aa/b1/c1/d0"));
        assertEquals("/aa/b1/c1/d1", tree.get("/aa/b1/c1/d1"));
        assertEquals("/aa/b2/c2/d2", tree.get("/aa/b2/c2/d2"));

        assertEquals("/{k1}", tree.get("/zzz"));
        assertEquals("/{k1}", tree.get("/aa"));

        assertNull(tree.get("/zzz/null"));
        assertNull(tree.get("/xx/yy"));
        assertNull(tree.get("/aa/b1/c1"));
        assertNull(tree.get("/aa/b2/c2"));

        assertEquals("/aa/b3/*/d1", tree.get("/aa/b3/c1/d1"));
        assertEquals("/aa/b3/c1/d1/*", tree.get("/aa/b3/c1/d1/e1"));
        assertEquals("/aa/b3/c1/d1/*", tree.get("/aa/b3/c1/d1/null"));
        assertEquals("/aa/b3/*/d1/{k2}", tree.get("/aa/b3/null/d1/null"));

        assertEquals("/aa/*/c1/d1", tree.get("/aa/b2/c1/d1"));
        assertEquals("/aa/*/c1/d1/e1", tree.get("/aa/b1/c1/d1/e1"));
        assertEquals("/aa/*/c1/d1/e1", tree.get("/aa/b2/c1/d1/e1"));


        assertEquals("/aa/b4/{k1}/d1", tree.get("/aa/b4/c1/d1"));
        assertEquals("/aa/b4/{k1}/d2", tree.get("/aa/b4/c1/d2"));

        assertEquals("/aa/*/c1/d1/e1", tree.get("/aa/b4/c1/d1/e1"));
        assertEquals("/aa/b4/{k1}/d2/e1", tree.get("/aa/b4/c1/d2/e1"));
        assertEquals("/aa/b4/{k1}/d2/{k2}", tree.get("/aa/b4/c1/d2/null"));
        assertEquals("/aa/b4/{k1}/d2/{k2}", tree.get("/aa/b4/null/d2/null"));

        assertEquals("/aa/*/c1/d1/e1", tree.get("/aa/null/c1/d1/e1"));

        assertNull(tree.get("/aa/b1"));
        assertNull(tree.get("/aa/b2"));
        assertNull(tree.get("/aa/null/c1"));
        assertNull(tree.get("/aa/b1/c1/d1/null"));
        assertNull(tree.get("/aa/null/c1/d2"));
        assertNull(tree.get("/aa/null/c1/d2/null"));
        assertNull(tree.get("/aa/zzz/c1/d1/e1/null/null/null"));
    }

    /**
     * 测试匹配参数。
     */
    @Test
    public void testTreePathParameters() {
        Map<String, String> excepted = new HashMap<String, String>();
        Map<String, String> actual = new HashMap<String, String>();

        tree.get("/xx/yy/zz", actual);
        tree.get("/aa/b1/c1/d0", actual);
        tree.get("/aa/null/c1", actual);
        tree.get("/aa/zzz/c1/d1/e1/null/null/null", actual);
        assertEquals(excepted, actual);

        excepted.put("k1", "zzz");
        //"/*"
        assertTreePathParameters(excepted, "/zzz");

        excepted.put("k1", "zzzzz");
        assertTreePathParameters(excepted, "/zzzzz");

        //"/aa/b3/*/d1"
        excepted.put("*", "c1");
        assertTreePathParameters(excepted, "/aa/b3/c1/d1");
        excepted.put("*", "null");
        assertTreePathParameters(excepted, "/aa/b3/null/d1");

        //"/aa/b3/*/d1/{k2}"
        excepted.put("*", "null1");
        excepted.put("k2", "null2");
        assertTreePathParameters(excepted, "/aa/b3/null1/d1/null2");

        //"/aa/b4/c1/d2/null"
        excepted.put("k1", "c1");
        excepted.put("k2", "null");
        assertTreePathParameters(excepted, "/aa/b4/c1/d2/null");

        //"/aa/b5/*/*/*/*"
        excepted.put("*", "null1");
        excepted.put("*2", "null2");
        excepted.put("*3", "null3");
        excepted.put("*4", "null4");
        assertTreePathParameters(excepted, "/aa/b5/null1/null2/null3/null4");

    }

    /**
     * 测试期望的键值映射是否与调用Action路径后的路径匹配的键值映射一致。
     *
     * @param excepted 期望的键值映射。
     * @param path Action路径。
     */
    private void assertTreePathParameters(Map<String, String> excepted, String path) {
        Map<String, String> actual = new HashMap<String, String>();
        tree.get(path, actual);
        assertEquals(excepted, actual);
        //clear the excepted map at last
        excepted.clear();
    }

    /**
     * 测试路径的非完全匹配名称。
     *
     * @see PathTree#isMatchKay(java.lang.String)
     */
    @Test
    public void test_isMatchKay() throws Exception {
        java.lang.reflect.Method method = PathTree.class.getDeclaredMethod("isMatchKay", String.class);
        method.setAccessible(true);

        Assert.assertEquals(false, method.invoke(null, ""));
        Assert.assertEquals(false, method.invoke(null, "*"));
        Assert.assertEquals(false, method.invoke(null, "abc"));
        Assert.assertEquals(false, method.invoke(null, "{abc"));
        Assert.assertEquals(false, method.invoke(null, "abc}"));
        Assert.assertEquals(false, method.invoke(null, "{}"));

        Assert.assertEquals(true, method.invoke(null, "{abc}"));
        Assert.assertEquals(true, method.invoke(null, "[abc]"));
        Assert.assertEquals(true, method.invoke(null, "{abc)"));
        Assert.assertEquals(true, method.invoke(null, "{ abc  }"));
        Assert.assertEquals(true, method.invoke(null, "{*}"));
        Assert.assertEquals(true, method.invoke(null, "{ }"));
        Assert.assertEquals(true, method.invoke(null, "{{abc}"));
        Assert.assertEquals(true, method.invoke(null, "{{abc}}}"));
        Assert.assertEquals(true, method.invoke(null, "aa{{abc}}}xx"));
    }

    /**
     * 测试解析路径的非完全匹配名称。
     *
     * @see PathTree#getMatchKey(java.lang.String)
     */
    @Test
    public void test_getMatchKey() throws Exception {
        java.lang.reflect.Method method = PathTree.class.getDeclaredMethod("getMatchKey", String.class);
        method.setAccessible(true);

        Assert.assertEquals(null, method.invoke(null, ""));
        Assert.assertEquals(null, method.invoke(null, "*"));
        Assert.assertEquals(null, method.invoke(null, "abc"));
        Assert.assertEquals(null, method.invoke(null, "{abc"));
        Assert.assertEquals(null, method.invoke(null, "abc}"));
        Assert.assertEquals(null, method.invoke(null, "{}"));

        Assert.assertEquals("abc", method.invoke(null, "{abc}"));
        Assert.assertEquals("abc", method.invoke(null, "[abc]"));
        Assert.assertEquals("abc", method.invoke(null, "{abc)"));
        Assert.assertEquals(" abc  ", method.invoke(null, "{ abc  }"));
        Assert.assertEquals("*", method.invoke(null, "{*}"));
        Assert.assertEquals(" ", method.invoke(null, "{ }"));
        Assert.assertEquals("{abc", method.invoke(null, "{{abc}"));
        Assert.assertEquals("{abc}}", method.invoke(null, "{{abc}}}"));
        Assert.assertEquals("{abc}}", method.invoke(null, "aa{{abc}}}xx"));
    }
}
