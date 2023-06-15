/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.enodeframework.common.function;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author anruence@gmail.com
 */
public class DelayedTask {
    private static final ScheduledExecutorService EXECUTOR = new ScheduledThreadPoolExecutor(
        1,
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("DelayedThread-%d")
            .build());

    public static void startDelayedTask(Duration duration, Action action) {
        DelayedTask.EXECUTOR.schedule(action::apply, duration.toMillis(), TimeUnit.MILLISECONDS);
    }
}
