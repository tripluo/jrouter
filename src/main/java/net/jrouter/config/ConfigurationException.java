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

/**
 * 标识jrouter解析配置发生错误时的异常。
 */
public class ConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造一个包含指定原因的ConfigurationException。
     *
     * @param cause 异常原因。
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个包含指定详细消息和原因的ConfigurationException。
     *
     * @param message 详细消息。
     * @param cause 异常原因。
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
