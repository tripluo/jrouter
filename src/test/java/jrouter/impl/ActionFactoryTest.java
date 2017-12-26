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
import jrouter.ActionProxy;
import jrouter.JRouterException;
import jrouter.SimpleAction;
import jrouter.annotation.Namespace;
import jrouter.bytecode.javassist.JavassistMethodInvokerFactory;
import jrouter.config.Configuration;
import jrouter.interceptor.DefaultInterceptorStack;
import jrouter.interceptor.SampleInterceptor;
import jrouter.result.DefaultResult;
import jrouter.result.DemoResult;
import jrouter.spring.RequestMappingActionFilter;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * ActionFactoryTest对应{@link SimpleAction}的测试。
 */
public class ActionFactoryTest {

    private PathActionFactory factory;

    /**
     * 初始化Configuration。
     */
    private Configuration initiateConfiguration() {
        return new Configuration().load("/jrouter_test.xml");
    }

    @Before
    public void init() {
        factory = initiateConfiguration().buildActionFactory();
        assertEquals("empty", factory.getDefaultInterceptorStack());
        assertEquals("empty", factory.getDefaultResultType());
        assertEquals(100000, factory.getActionCacheNumber());
        assertEquals(".", factory.getExtension());
        assertEquals('/', factory.getPathSeparator());
        assertEquals(MultiParameterConverterFactory.class, factory.getConverterFactory().getClass());
        assertEquals(RequestMappingActionFilter.class, factory.getActionFilter().getClass());

        assertNotNull(factory.getInterceptors().get(SampleInterceptor.LOGGING));
        assertNotNull(factory.getInterceptors().get(SampleInterceptor.TIMER));
    }

    @After
    public void tearDown() {
        factory.clear();
    }

    /**
     * 测试生成ActionFactory
     */
    @Test
    public void test_createActionFactory() {
        Map<String, Object> props = new HashMap<String, Object>();
        //set extension
        props.put("extension", ".do");
        factory = new PathActionFactory(props);

        //interceptor
        factory.addInterceptors(SampleInterceptor.class);

        //interceptor stack
        factory.addInterceptorStacks(DefaultInterceptorStack.class);

        //result
        factory.addResultTypes(DefaultResult.class);

        //aciotn
        factory.addActions(jrouter.SimpleAction.class);

        assertSame(null, factory.getDefaultInterceptorStack());
        assertSame(null, factory.getDefaultResultType());
        assertNotNull(factory.getInterceptors().get(SampleInterceptor.LOGGING));
        assertNotNull(factory.getInterceptors().get(SampleInterceptor.TIMER));
    }

    /**
     * 测试路径解析。
     *
     * @throws Exception 如果发生异常。
     *
     * @see PathActionFactory.ColonString#parseMatch(java.lang.String, java.lang.String[])
     */
    @Test
    public void test_parseMatch() throws Exception {
        java.lang.reflect.Method method = PathActionFactory.ColonString.class.getDeclaredMethod("parseMatch", String.class, String[].class);
        method.setAccessible(true);
        String[] emptyDefaults = new String[]{"", ""};

        String[] nullDefaults = new String[]{null, null};
        assertArrayEquals(new String[]{"", ""}, (String[]) method.invoke(null, "  ", nullDefaults));
        assertArrayEquals(new String[]{"", ""}, (String[]) method.invoke(null, "", nullDefaults));
        assertArrayEquals(new String[]{"abc", ""}, (String[]) method.invoke(null, "abc", nullDefaults));
        assertArrayEquals(new String[]{"abc", ""}, (String[]) method.invoke(null, "  abc", nullDefaults));
        assertArrayEquals(new String[]{"abc", ""}, (String[]) method.invoke(null, "abc ", nullDefaults));
        assertArrayEquals(new String[]{"abc", ""}, (String[]) method.invoke(null, " abc  ", nullDefaults));

        assertArrayEquals(new String[]{"1", ""}, (String[]) method.invoke(null, "1:", nullDefaults));
        assertArrayEquals(new String[]{"1", ""}, (String[]) method.invoke(null, "1 :", nullDefaults));
        assertArrayEquals(new String[]{"1", ""}, (String[]) method.invoke(null, "1 :  ", nullDefaults));
        assertArrayEquals(new String[]{"", "1"}, (String[]) method.invoke(null, ":1", nullDefaults));
        assertArrayEquals(new String[]{"", "1"}, (String[]) method.invoke(null, " :1", nullDefaults));
        assertArrayEquals(new String[]{"", "1"}, (String[]) method.invoke(null, ": 1  ", nullDefaults));
        assertArrayEquals(new String[]{"", "123"}, (String[]) method.invoke(null, ":123", nullDefaults));
        assertArrayEquals(new String[]{"", "123"}, (String[]) method.invoke(null, "  :123  ", nullDefaults));
        assertArrayEquals(new String[]{"", "123"}, (String[]) method.invoke(null, ":  123  ", nullDefaults));
        assertArrayEquals(new String[]{"", "123"}, (String[]) method.invoke(null, "   :  123   ", nullDefaults));
        assertArrayEquals(new String[]{"abc", "123"}, (String[]) method.invoke(null, "abc:123  ", nullDefaults));
        assertArrayEquals(new String[]{"abc", "123"}, (String[]) method.invoke(null, "abc    :123  ", nullDefaults));
        assertArrayEquals(new String[]{"abc", "123"}, (String[]) method.invoke(null, "   abc:123 ", nullDefaults));
        assertArrayEquals(new String[]{"abc", "123"}, (String[]) method.invoke(null, "   abc    :123 ", nullDefaults));
        assertArrayEquals(new String[]{"abc", "123"}, (String[]) method.invoke(null, "   abc    :   123 ", nullDefaults));
        assertArrayEquals(new String[]{"abc", ""}, (String[]) method.invoke(null, "   abc    :", nullDefaults));
        assertArrayEquals(emptyDefaults, (String[]) method.invoke(null, ":", nullDefaults));
        assertArrayEquals(emptyDefaults, (String[]) method.invoke(null, " : ", nullDefaults));
        assertArrayEquals(emptyDefaults, (String[]) method.invoke(null, "    :   ", nullDefaults));

        String defaultType = "defaultType";
        String defaultLoc = "defaultLoc";
        String[] defaults = new String[]{defaultType, defaultLoc};
        assertArrayEquals(defaults, (String[]) method.invoke(null, "  ", defaults));
        assertArrayEquals(defaults, (String[]) method.invoke(null, "", defaults));
        assertArrayEquals(new String[]{"abc", "defaultLoc"}, (String[]) method.invoke(null, "abc", defaults));
        assertArrayEquals(new String[]{"abc", "defaultLoc"}, (String[]) method.invoke(null, "  abc", defaults));
        assertArrayEquals(new String[]{"abc", "defaultLoc"}, (String[]) method.invoke(null, "abc ", defaults));
        assertArrayEquals(new String[]{"abc", "defaultLoc"}, (String[]) method.invoke(null, " abc  ", defaults));
        assertArrayEquals(new String[]{defaultType, "123"}, (String[]) method.invoke(null, ":123", defaults));
        assertArrayEquals(new String[]{defaultType, "123"}, (String[]) method.invoke(null, "  :123  ", defaults));
        assertArrayEquals(new String[]{defaultType, "123"}, (String[]) method.invoke(null, ":  123  ", defaults));
        assertArrayEquals(new String[]{defaultType, "123"}, (String[]) method.invoke(null, "   :  123   ", defaults));
        assertArrayEquals(new String[]{"abc", "123"}, (String[]) method.invoke(null, "abc:123  ", defaults));
        assertArrayEquals(new String[]{"abc", "123"}, (String[]) method.invoke(null, "abc    :123  ", defaults));
        assertArrayEquals(new String[]{"abc", "123"}, (String[]) method.invoke(null, "   abc:123 ", defaults));
        assertArrayEquals(new String[]{"abc", "123"}, (String[]) method.invoke(null, "   abc    :123 ", defaults));
        assertArrayEquals(new String[]{"abc", "123"}, (String[]) method.invoke(null, "   abc    :   123 ", defaults));
        assertArrayEquals(new String[]{"abc", defaultLoc}, (String[]) method.invoke(null, "   abc    :", defaults));
        assertArrayEquals(defaults, (String[]) method.invoke(null, ":", defaults));
        assertArrayEquals(defaults, (String[]) method.invoke(null, " : ", defaults));
        assertArrayEquals(defaults, (String[]) method.invoke(null, "    :   ", defaults));

    }

    /**
     * 测试Action路径的正确性。
     *
     * @see jrouter.SimpleAction#simple()
     * @see jrouter.SimpleAction#param(java.lang.String, int)
     * @see jrouter.SimpleAction#exception() ()
     */
    @Test
    public void test_simple() {
        //简单调用
        String url1 = "/test/simple";

        PathActionProxy ap = factory.getActions().get(url1);

        assertNotNull(ap);
        assertNotNull(ap.getAction());
        assertTrue(ap.getInterceptorProxies().isEmpty());
        assertNotNull(ap.getResults());
        assertTrue(factory.getMethodInvokerFactory() instanceof JavassistMethodInvokerFactory);

        ap.invoke();

        //带参数调用
        String url2 = "/test/param";
        ap = factory.getActions().get(url2);
        assertNotNull(ap);
        assertNotNull(ap.getAction());
        assertEquals(SampleInterceptor.LOGGING, ap.getInterceptorProxies().get(0).getName());
        assertEquals(SampleInterceptor.LOGGING, ap.getInterceptors().get(0).name());
        assertNotNull(ap.getResults());

        assertEquals("test100", factory.invokeAction(url2, "test", 100));
        assertEquals("test100100", factory.invokeAction(url2, "test100", 100));

        //调用发生异常
        String url3 = "/test/exception";
        ap = factory.getActions().get(url3);

        assertNotNull(ap);
        assertNotNull(ap.getAction());
        assertEquals(SampleInterceptor.LOGGING, ap.getInterceptorProxies().get(0).getName());
        assertEquals(SampleInterceptor.TIMER, ap.getInterceptorProxies().get(1).getName());
        assertEquals(SampleInterceptor.LOGGING, ap.getInterceptors().get(0).name());
        assertEquals(SampleInterceptor.TIMER, ap.getInterceptors().get(1).name());
        assertNotNull(ap.getResults());

        try {
            //测试调用时抛出异常
            ap.invoke();
            fail("no exception");
        } catch (InvocationProxyException e) {
            assertNotNull(e);
            assertSame(ap, e.getTarget());
        }
        try {
            //测试调用时抛出异常
            factory.invokeAction(url3);
            fail("no exception");
        } catch (InvocationProxyException e) {
            assertNotNull(e);
            //ActionFactory调用抛出异常，测试消除拦截器的递归调用对异常信息的扰乱。
            assertFalse(e.getCause() instanceof InvocationProxyException);
            assertSame(ap, e.getTarget());
        }

        String url4 = "/test/singleVarArgsArray";
        ap = factory.getActions().get(url4);
        assertNotNull(ap);
        //singleVarArgsArray
        assertEquals("[]", factory.invokeAction(url4));//change
        assertEquals("[null]", factory.invokeAction(url4, (Object) null));//change
        assertEquals("null", factory.invokeAction(url4, (Object[]) null));
        //assertEquals("null", factory.invokeAction(url4, new Object[]{null}));//change
        assertEquals("[null]", factory.invokeAction(url4, new Object[]{null}));//change
        //assertEquals("null", factory.invokeAction(url4, new Object[0]));
        assertEquals("[]", factory.invokeAction(url4, new Object[0]));//change
        assertEquals("[]", factory.invokeAction(url4, new Object[]{new String[]{}}));
        assertEquals("[null, null, null]", factory.invokeAction(url4, new Object[]{new String[]{null, null, null}}));
        assertEquals("[123, abc]", factory.invokeAction(url4, new Object[]{"123", "abc"}));//change
        assertEquals("[123, abc]", factory.invokeAction(url4, new Object[]{new String[]{"123", "abc"}}));
        assertEquals("[123, abc]", factory.invokeAction(url4, (Object[]) new String[]{"123", "abc"}));//change
        assertEquals("[null, null, null]", factory.invokeAction(url4, new Object[]{null, null, null}));//change
        assertEquals("[null, null, null]", factory.invokeAction(url4, null, null, null));//change
        assertEquals("[123]", factory.invokeAction(url4, "123"));//change
        assertEquals("[123, abc]", factory.invokeAction(url4, "123", "abc"));//change

        String url5 = "/test/varArgsArray";
        ap = factory.getActions().get(url5);
        assertNotNull(ap);
        //varArgsArray
        assertEquals("100[]", factory.invokeAction(url5, 100));//change
        //assertEquals("100null", factory.invokeAction(url5, 100, null));//change
        assertEquals("100[null]", factory.invokeAction(url5, 100, null));//change
        assertEquals("100[null, null, null]", factory.invokeAction(url5, 100, null, null, null));//change
        assertEquals("100[]", factory.invokeAction(url5, 100, new String[0]));
        assertEquals("100[]", factory.invokeAction(url5, 100, new String[]{}));
        assertEquals("100[]", factory.invokeAction(url5, 100, new String[]{""}));
        assertEquals("100[null]", factory.invokeAction(url5, 100, new String[]{null}));
        assertEquals("100[1, 2]", factory.invokeAction(url5, 100, new String[]{"1", "2"}));
        assertEquals("100[]", factory.invokeAction(url5, new Object[]{100, new String[]{}}));
        assertEquals("100[1, 2]", factory.invokeAction(url5, new Object[]{100, new String[]{"1", "2"}}));
        assertEquals("100[]", factory.invokeAction(url5, new Object[]{100, new String[0]}));
        //assertEquals("100null", factory.invokeAction(url5, new Object[]{100, null}));//change
        assertEquals("100[null]", factory.invokeAction(url5, new Object[]{100, null}));//change
        assertEquals("100[1]", factory.invokeAction(url5, 100, "1"));//change
        assertEquals("100[1, 2]", factory.invokeAction(url5, 100, "1", "2"));//change
        //set first parameter null
        assertEquals("null[]", factory.invokeAction(url5, (Object) null));//change
        assertEquals("null[]", factory.invokeAction(url5, (Object[]) null));//change

        String url6 = "/test/varArgsArray2";
        assertEquals("100200[]", factory.invokeAction(url6, 100, 200));//change
        assertEquals("100null[]", factory.invokeAction(url6, 100, null));//change
        assertEquals("100null[null, null]", factory.invokeAction(url6, 100, null, null, null));//change
        assertEquals("100200[]", factory.invokeAction(url6, 100, 200, new String[0]));
        assertEquals("100200[]", factory.invokeAction(url6, 100, 200, new String[]{}));
        assertEquals("100200[]", factory.invokeAction(url6, 100, 200, new String[]{""}));
        assertEquals("100200[null]", factory.invokeAction(url6, 100, 200, new String[]{null}));
        assertEquals("100200[1, 2]", factory.invokeAction(url6, 100, 200, new String[]{"1", "2"}));
        assertEquals("100200[]", factory.invokeAction(url6, new Object[]{100, 200, new String[]{}}));
        assertEquals("100200[1, 2]", factory.invokeAction(url6, new Object[]{100, 200, new String[]{"1", "2"}}));
        assertEquals("100200[]", factory.invokeAction(url6, new Object[]{100, 200, new String[0]}));
        assertEquals("100200[null]", factory.invokeAction(url6, new Object[]{100, 200, null}));//change
        assertEquals("100200[1]", factory.invokeAction(url6, 100, 200, "1"));//change
        assertEquals("100200[1, 2]", factory.invokeAction(url6, 100, 200, "1", "2"));//change
        //set previous parameter null
        assertEquals("100null[]", factory.invokeAction(url6, 100, (Object) null));//change
        assertEquals("100null[]", factory.invokeAction(url6, 100, (Object[]) null));//change
    }

    /**
     * 测试foward调用。
     *
     * @see jrouter.SimpleAction#forward()
     * @see jrouter.SimpleAction#forward2()
     */
    @Test
    public void test_forward() {
        String url1 = "/test/forward.do";
        String url2 = "/test/forward2.do";
        assertEquals(SimpleAction.SUCCESS, factory.invokeAction(url1));
        assertEquals(SimpleAction.SUCCESS, factory.invokeAction(url2));
    }

    /**
     * 测试路径全匹配'*'
     *
     * @see jrouter.SimpleAction#autoRender(java.lang.String)
     */
    @Test
    public void test_autoRender() {
        Map<String, Object> props = new HashMap<String, Object>();
        //默认结果处理直接返回调用结果
        props.put("defaultStringResultType", DefaultResult.EMPTY);

        String url1 = "/test/autoRender.do";
        //rebuild factory
        factory = initiateConfiguration().addActionFactoryProperties(props).buildActionFactory();

        //full match
        assertEquals(SimpleAction.SUCCESS, factory.invokeAction(url1, "test"));

        assertEquals(DefaultResult.EMPTY + ":abc", factory.invokeAction(url1, DefaultResult.EMPTY + ":abc"));
        assertEquals(":abc", factory.invokeAction(url1, ":abc"));
        assertEquals(":  abcd", factory.invokeAction(url1, ":  abcd"));
        assertEquals("  :abcd", factory.invokeAction(url1, "  :abcd"));
        assertEquals(" :   abcde", factory.invokeAction(url1, " :   abcde"));

        //forward
        props.put("defaultStringResultType", DefaultResult.FORWARD);
        //rebuild factory
        factory = initiateConfiguration().addActionFactoryProperties(props).buildActionFactory();
        assertEquals(SimpleAction.SUCCESS, factory.invokeAction(url1, ":/test/simple2"));
        assertEquals(SimpleAction.SUCCESS, factory.invokeAction(url1, "  : /test/simple2"));
        assertEquals(SimpleAction.SUCCESS, factory.invokeAction(url1, DefaultResult.FORWARD + ":/test/simple2"));
        assertEquals(SimpleAction.SUCCESS, factory.invokeAction(url1, DefaultResult.FORWARD + "  : /test/simple2"));
        assertEquals(SimpleAction.SUCCESS, factory.invokeAction(url1, DefaultResult.FORWARD + "  : /test/simple2  "));
        assertEquals(SimpleAction.SUCCESS, factory.invokeAction(url1, "   " + DefaultResult.FORWARD + "  :   /test/simple2  "));

        try {
            factory.invokeAction(url1, "notype:/nolocation");
            fail("no exception");
        } catch (JRouterException e) {
            assertNotNull(e);
        }
    }

    /**
     * 测试Action、Interceptor、Result中属性注入的正确性。注入的属性参加jrouter_test.xml配置文件。
     *
     * @see jrouter.SimpleAction#inject()
     * @see jrouter.interceptor.DemoInterceptor
     * @see jrouter.result.DemoResult
     */
    @Test
    public void test_inject() {
        String url1 = "/test/inject";
        //Action的scope为"PROTOTYPE"，进行多次测试
        assertEquals("admin100", factory.invokeAction(url1));
        assertEquals("admin100", factory.invokeAction(url1));
        assertEquals("admin100", factory.invokeAction(url1));

        String url2 = "/test/inject2";
        assertEquals("admin200", factory.invokeAction(url2));
        assertEquals("admin200", factory.invokeAction(url2));
        assertEquals("admin200", factory.invokeAction(url2));
    }

    /**
     * 测试全局结果对象。
     *
     * @see jrouter.SimpleAction#resultNotFound()
     * @see jrouter.result.DemoResult
     */
    @Test
    public void test_resultNotFound() {
        String url = "/test/resultNotFound";
        assertTrue(factory.getResults().containsKey(DemoResult.DEMO_RESULT_NOT_FOUND));
        assertTrue(factory.getResults().containsKey(DemoResult.DEMO_RESULT_EXCEPTION));
        assertEquals(DemoResult.DEMO_RESULT_NOT_FOUND + ":" + url, factory.invokeAction(url, DemoResult.DEMO_RESULT_NOT_FOUND));
        try {
            assertEquals(DemoResult.DEMO_RESULT_NOT_FOUND + ":" + url, factory.invokeAction(url, DemoResult.DEMO_RESULT_EXCEPTION));
            fail("no exception");
        } catch (InvocationProxyException e) {
            assertNotNull(e);
            assertTrue(e.getSource() instanceof RuntimeException);
        }
        assertEquals("unknown_result1", factory.invokeAction(url, "unknown_result1"));
        assertEquals("unknown_result2", factory.invokeAction(url, "unknown_result2"));
    }

    /**
     * 测试Action的初始属性。
     *
     * @see jrouter.SimpleAction#actionParameters()
     */
    @Test
    public void test_actionParameters() {
        String url = "/test/actionParameters";
        ActionProxy ap = factory.getActions().get(url);

        assertNotNull(ap.getActionParameters());
        assertSame(3, ap.getActionParameters().size());
        assertEquals("value1", ap.getActionParameter("test1"));
        assertEquals("value2", ap.getActionParameter("test2"));
        assertEquals("value3", ap.getActionParameter("test3"));
        assertArrayEquals(new String[]{"value3", "value33"}, ap.getActionParameterValues("test3"));
    }

    /**
     * 测试@Ignore。
     */
    @Test
    public void test_ignore() {
        String ignore = "test/ignore";
        ActionProxy ap = factory.getActions().get(ignore);
        assertNull(ap);
    }

    /**
     * 测试@Namespace的autoIncluded属性。
     *
     * @see Namespace#autoIncluded()
     */
    @Test
    public void test_autoIncluded() {
        String included = "test/autoIncluded";
        ActionProxy ap = factory.getActions().get(included);
        assertNotNull(ap);
        assertEquals(1L, factory.invokeAction(included));
    }

    /**
     * 测试ActionFactory的converterFactory属性。
     *
     * @see PathActionFactory#converterFactory
     * @see jrouter.impl.MultiParameterConverterFactory
     */
    @Test
    public void test_lastPadParameter() {
        String lastPadParameter = "/test/lastPadParameter";
        String lastPadParameter2 = "/test/lastPadParameter2";
        assertEquals(lastPadParameter, factory.invokeAction(lastPadParameter));
        assertEquals("path" + lastPadParameter2, factory.invokeAction(lastPadParameter2, "path"));
    }

    /**
     * 测试ActionFactory的actionFilter属性。
     *
     * @see jrouter.SimpleAction#actionFilter
     */
    @Test
    public void test_actionFilter() {
        String actionFilter = "/test/actionFilter";
        String actionFilter2 = "/actionFilter2";
        assertEquals("test", factory.invokeAction(actionFilter, "test"));
        assertEquals(actionFilter2, factory.invokeAction(actionFilter2));
    }
}
