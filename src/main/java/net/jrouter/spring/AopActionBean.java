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

package net.jrouter.spring;

import java.util.ArrayList;
import java.util.List;
import net.jrouter.config.AopAction;
import net.jrouter.util.CollectionUtil;

/**
 * Actions' aop for springframework's bean.
 */
public class AopActionBean extends AopAction {

    private static final long serialVersionUID = 1L;

    /** 分隔符 */
    private final char[] sep = {',', ';'};

    /**
     * 设置指定的拦截栈集合。
     *
     * @param interceptorNames 拦截栈集合的字符串名称，支持','或';'分隔。
     */
    public void setInterceptorNames(String interceptorNames) {
        List<String> list = new ArrayList<>(4);
        CollectionUtil.stringToCollection(interceptorNames, list, sep);
        setInterceptors(list);
    }

    /**
     * 设置指定的拦截栈集合。
     *
     * @param interceptorStackNames 拦截栈集合，支持','或';'分隔。
     */
    public void setInterceptorStackNames(String interceptorStackNames) {
        List<String> list = new ArrayList<>(4);
        CollectionUtil.stringToCollection(interceptorStackNames, list, sep);
        setInterceptorStacks(list);
    }

    /**
     * 设置aop的类型。
     *
     * @param typeName aop的类型名称。
     *
     * @see Type
     */
    public void setTypeName(String typeName) {
        setType(Type.parseCode(typeName));
    }
}
