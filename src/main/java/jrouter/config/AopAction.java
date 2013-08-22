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
package jrouter.config;

import java.io.Serializable;
import java.util.List;

/**
 * 针对相应路径的所有Action，添加/修改其对应的拦截器集合。
 */
public class AopAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Action的路径匹配。
     */
    private String matches;

    /**
     * 依序指定的拦截栈集合。
     */
    private List<String> interceptorStacks;

    /**
     * 依序指定的拦截器集合。
     */
    private List<String> interceptors;

    /**
     * aop的类型。
     */
    private Type type;

    /**
     * 获取Action的路径匹配。
     *
     * @return Action的路径匹配。
     */
    public String getMatches() {
        return matches;
    }

    /**
     * 设置Action的路径匹配。
     *
     * @param matches Action的路径匹配。
     */
    public void setMatches(String matches) {
        this.matches = matches;
    }

    /**
     * 获取指定的拦截栈集合。
     *
     * @return 指定的拦截栈集合。
     */
    public List<String> getInterceptorStacks() {
        return interceptorStacks;
    }

    /**
     * 设置指定的拦截栈集合。
     *
     * @param interceptorStacks 指定的拦截栈集合。
     */
    public void setInterceptorStacks(List<String> interceptorStacks) {
        this.interceptorStacks = interceptorStacks;
    }

    /**
     * 获取指定的拦截器集合。
     *
     * @return 指定的拦截器集合。
     */
    public List<String> getInterceptors() {
        return interceptors;
    }

    /**
     * 设置指定的拦截器集合。
     *
     * @param interceptors 指定的拦截器集合。
     */
    public void setInterceptors(List<String> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * 获取aop的类型。
     *
     * @return aop的类型。
     */
    public Type getType() {
        return type;
    }

    /**
     * 设置aop的类型。
     *
     * @param type aop的类型。
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * 修改Action拦截器集合的aop操作类型。
     */
    public static enum Type {

        /**
         * 添加于已有拦截器集合之前。
         */
        ADD_BEFORE("add-before"),
        /**
         * 添加于已有拦截器集合之后。
         */
        ADD_AFTER("add-after"),
        /**
         * 覆盖已有拦截器集合。
         */
        OVERRIDE("override");

        //aop类型所表征的字符串代码
        private String code;

        /**
         * 构造指定代码的aop类型。
         *
         * @param code 指定代码。
         */
        private Type(String code) {
            this.code = code;
        }

        /**
         * 获取aop的类型代码。
         *
         * @return aop的类型代码。
         */
        public String getCode() {
            return code;
        }

        /**
         * 由指定代码返回aop类型。
         *
         * @param code op的类型代码。
         *
         * @return aop类型。
         */
        public static Type parseCode(String code) {
            if (ADD_BEFORE.getCode().equals(code)) {
                return ADD_BEFORE;
            } else if (ADD_AFTER.getCode().equals(code)) {
                return ADD_AFTER;
            } else if (OVERRIDE.getCode().equals(code)) {
                return OVERRIDE;
            }
            throw new IllegalArgumentException("No enum const " + Type.class + "." + code);
        }
    }

    @Override
    public String toString() {
        return "AopAction{" + "matches=" + matches + ", interceptorStacks=" + interceptorStacks + ", interceptors=" + interceptors + ", type=" + (type == null ? null : type.getCode()) + '}';
    }
}
