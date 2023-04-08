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

package net.jrouter.interceptor;

import java.util.Date;
import net.jrouter.ActionInvocation;
import net.jrouter.annotation.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 示例拦截器。
 */
public class SampleInterceptor {

    /** LOG */
    private static final Logger LOG = LoggerFactory.getLogger(SampleInterceptor.class);

    /** 计时拦截器 */
    public static final String TIMER = "timer";

    /** 日志拦截器 */
    public static final String LOGGING = "logging";

    /**
     * 记录Action调用耗时。
     *
     * @param invocation Action运行时上下文。
     *
     * @return 拦截器处理后的Action调用结果。
     */
    @Interceptor(name = TIMER)
    public static Object timer(ActionInvocation invocation) {
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            // invoke
            result = invocation.invoke();
        } finally {
            if (LOG.isInfoEnabled()) {
                long executionTime = System.currentTimeMillis() - startTime;
                StringBuilder message = new StringBuilder(64);
                message.append("Executed action [").append(invocation.getActionPath());
                message.append("] took ").append(executionTime).append(" ms.");
                LOG.info(message.toString());
            }
        }
        return result;
    }

    /**
     * 记录Action起始结束时间。
     *
     * @param invocation Action运行时上下文。
     *
     * @return 拦截器处理后的Action调用结果。
     */
    @Interceptor(name = LOGGING)
    public static Object logging(ActionInvocation invocation) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Starting action [{}] at {}.", invocation.getActionPath(), new Date());
        }
        // invoke
        Object result = invocation.invoke();
        if (LOG.isInfoEnabled()) {
            LOG.info("Finishing action [{}] at {}.", invocation.getActionPath(), new Date());
        }
        return result;
    }
}
