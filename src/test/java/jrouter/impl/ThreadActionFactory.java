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

import java.util.Map;
import jrouter.ActionInvocation;

/**
 * ThreadActionFactory restores the ActionInvocation pre thread。
 */
public class ThreadActionFactory extends DefaultActionFactory {

    public ThreadActionFactory(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    protected ActionInvocation createActionInvocation(String path, Object... params) {
        ActionInvocation ai = super.createActionInvocation(path, params);
        if (ThreadContext.get() == null)
            ThreadContext.set(new ThreadContext());
        ThreadContext.setActionInvocation(ai);
        return ai;
    }
}
