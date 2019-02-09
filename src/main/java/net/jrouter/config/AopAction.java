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
package net.jrouter.config;

import java.io.Serializable;
import java.util.List;
import net.jrouter.annotation.Action;

/**
 * 针对相应路径的所有{@link Action}，添加/修改其对应的拦截器集合。
 */
public class AopAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Action的路径匹配。
     */
    @lombok.Getter
    @lombok.Setter
    private String matches;

    /**
     * 依序指定的拦截栈集合。
     */
    @lombok.Getter
    @lombok.Setter
    private List<String> interceptorStacks;

    /**
     * 依序指定的拦截器集合。
     */
    @lombok.Getter
    @lombok.Setter
    private List<String> interceptors;

    /**
     * aop的类型。
     */
    @lombok.Getter
    @lombok.Setter
    private Type type;

    /**
     * 修改Action拦截器集合的aop操作类型。
     */
    public enum Type {

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
        @lombok.Getter
        private final String code;

        /**
         * 构造指定代码的aop类型。
         *
         * @param code 指定代码。
         */
        Type(String code) {
            this.code = code;
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
