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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import net.jrouter.ActionFilter;
import net.jrouter.annotation.*;
import net.jrouter.util.CollectionUtil;
import net.jrouter.util.StringUtil;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 适配{@link RequestMapping}的{@code ActionFilter}实现。
 */
public class RequestMappingActionFilter implements ActionFilter {

    @Override
    public boolean accept(Object obj, Method method) {
        return method.isAnnotationPresent(Action.class) || method.isAnnotationPresent(RequestMapping.class);
    }

    @Override
    public Action getAction(Object obj, Method method) {
        final Action action = method.getAnnotation(Action.class);
        final boolean hasAction = (action != null);
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        if (mapping == null) {
            return action;
        } else {
            // use mapping's value/name, ignore action's value/name
            String[] values = mapping.value();
            if (CollectionUtil.isEmpty(values)) {
                values = mapping.path();
            }
            if (CollectionUtil.isEmpty(values)) {
                String name = mapping.name();
                if (StringUtil.isNotBlank(name)) {
                    values = new String[]{name};
                }
            }
            if (CollectionUtil.isEmpty(values)) {
                values = new String[]{method.getName()};
            }

            final String[] paths = values;
            return new Action() {

                @Override
                public String[] value() {
                    return paths;
                }

                @Override
                public String[] name() {
                    return paths;
                }

                @Override
                public String interceptorStack() {
                    return hasAction ? action.interceptorStack() : "";
                }

                @Override
                public String[] interceptors() {
                    return hasAction ? action.interceptors() : CollectionUtil.EMPTY_STRING_ARRAY;
                }

                @Override
                public Result[] results() {
                    return hasAction ? action.results() : new Result[0];
                }

                @Override
                public Scope scope() {
                    return hasAction ? action.scope() : Scope.SINGLETON;
                }

                @Override
                public Parameter[] parameters() {
                    return hasAction ? action.parameters() : new Parameter[0];
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return RequestMapping.class;
                }
            };
        }
    }

    @Override
    public Namespace getNamespace(Object obj, Method method) {
        return method.getDeclaringClass().getAnnotation(Namespace.class);
    }
}
