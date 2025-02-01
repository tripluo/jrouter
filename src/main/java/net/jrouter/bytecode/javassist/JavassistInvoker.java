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

import net.jrouter.Invoker;

import java.lang.reflect.Method;

/**
 * Invoker接口的封装，本质未实现方法。
 *
 * @see JavassistMethodInvokerFactory#newInstance
 */
public class JavassistInvoker implements Invoker {

    @Override
    public <T> T invoke(Method method, Object obj, Object... params) {
        throw new UnsupportedOperationException("Not implemented here.");
    }

}
