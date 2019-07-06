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

package net.jrouter.annotation;

/**
 * Scope表示了{@link Action}的调用范围。
 * Action调用可为单例(每次都调用同一对象的方法)或非单例(每次都调用新对象的方法)。
 *
 * @see Action#scope()
 */
public enum Scope {

    /**
     * 单例。
     */
    SINGLETON,
    /**
     * 非单例。
     */
    PROTOTYPE

}
