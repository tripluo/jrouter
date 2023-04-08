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

package net.jrouter.bytecode.javassist;

import java.lang.reflect.Method;
import java.util.List;
import net.jrouter.ActionInvocation;
import net.jrouter.interceptor.DemoInterceptor;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * JavassistMethodCheckerTest.
 */
public class JavassistMethodCheckerTest {

    /**
     * Test of check method, of class JavassistMethodChecker.
     */
    @Test
    public void testCheck() throws Exception {
        Method method = DemoInterceptor.class.getDeclaredMethod("test", ActionInvocation.class);
        assertTrue(new JavassistMethodChecker("").check(method));
        assertTrue(new JavassistMethodChecker("**").check(method));
        assertFalse(new JavassistMethodChecker("*").check(method));
        assertFalse(new JavassistMethodChecker("NoMethod").check(method));
        assertTrue(new JavassistMethodChecker("**.**").check(method));
        assertTrue(new JavassistMethodChecker("net.jrouter.ActionInvocation.invoke(**)|net.jrouter.ActionInvocation.invokeActionOnly(**)").check(method));
        assertTrue(new JavassistMethodChecker("net.jrouter.ActionInvocation.invoke").check(method));
        assertTrue(new JavassistMethodChecker("net.jrouter.ActionInvocation.invoke(java.lang.Object[])").check(method));
        assertFalse(new JavassistMethodChecker("net.jrouter.ActionInvocation.invoke()").check(method));
        assertFalse(new JavassistMethodChecker("net.jrouter.ActionInvocation.invokeActionOnly(**)").check(method));
        assertTrue(new JavassistMethodChecker("org.junit.Assert.assertEquals").check(method));
        assertTrue(new JavassistMethodChecker("org.junit.Assert.assertEquals(**)").check(method));
        assertTrue(new JavassistMethodChecker("org.junit.Assert.assertEquals(*,*)").check(method));
        assertTrue(new JavassistMethodChecker("org.junit.Assert.assertEquals(*, *)").check(method));
        assertTrue(new JavassistMethodChecker("org.junit.Assert.assertEquals(Object, Object)").check(method));
        assertTrue(new JavassistMethodChecker("org.junit.Assert.assertEquals(Object,java.lang.Object)").check(method));
        assertTrue(new JavassistMethodChecker("org.junit.Assert.assertEquals(Object, java.lang.Object)").check(method));
        assertTrue(new JavassistMethodChecker("net.jrouter.ActionInvocation.invoke & org.junit.Assert.assertEquals").check(method));
        assertTrue(new JavassistMethodChecker("net.jrouter.ActionInvocation.invoke & org.junit.Assert.assertEquals | NoMethod").check(method));
        assertTrue(new JavassistMethodChecker("net.jrouter.ActionInvocation.invoke|NoMethod2|NoMethod3").check(method));
        assertFalse(new JavassistMethodChecker("**&net.jrouter.ActionInvocation.invoke|NoMethod2|NoMethod3").check(method));
        assertTrue(new JavassistMethodChecker("**&**.**|NoMethod").check(method));
        assertTrue(new JavassistMethodChecker("**|NoMethod2|NoMethod3").check(method));
        assertFalse(new JavassistMethodChecker("NoMethod|NoMethod2|NoMethod3").check(method));
    }

    /**
     * Test of toClassNames method, of class JavassistMethodChecker.
     */
    @Test
    public void test_toClassNames() throws Exception {
        Method method = JavassistMethodChecker.class.getDeclaredMethod("toClassNames", String.class);
        method.setAccessible(true);
        assertArrayEquals(new String[]{}, toClassNames(method, ""));
        assertArrayEquals(new String[]{}, toClassNames(method, "()"));
        assertArrayEquals(new String[]{"java.lang.String"}, toClassNames(method, "(Ljava/lang/String;)"));
        assertArrayEquals(new String[]{"java.lang.String[]"}, toClassNames(method, "([Ljava/lang/String;)"));
        assertArrayEquals(new String[]{"net.jrouter.ActionInvocation"}, toClassNames(method, "Lnet/jrouter/ActionInvocation;"));
        assertArrayEquals(new String[]{"net.jrouter.ActionInvocation"}, toClassNames(method, "(Lnet/jrouter/ActionInvocation;)Ljava/lang/Object;"));
        assertArrayEquals(new String[]{"int[][]", "java.util.List", "java.lang.String[][][]"}, toClassNames(method, "([[ILjava/util/List;[[[Ljava/lang/String;)"));

    }

    // invoke JavassistMethodChecker#toClassNames
    private static String[] toClassNames(Method m, String descriptor) throws Exception {
        return ((List<String>) m.invoke(null, descriptor)).toArray(new String[0]);
    }
}
