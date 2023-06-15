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
package org.enodeframework.common.scheduling;

import org.enodeframework.common.function.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author anruence@gmail.com
 */
public class Worker {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private final Object lockObject = new Object();
    private final String actionName;
    private final Action action;
    private Status status;
    private Thread thread;

    public Worker(String actionName, Action action) {
        this.actionName = actionName;
        this.action = action;
        this.status = Status.Initial;
    }

    public Worker start() {
        synchronized (lockObject) {
            if (status.equals(Status.Running)) {
                return this;
            }
            status = Status.Running;
            thread = new Thread(this::loop, String.format("%s.Worker", actionName));
            thread.setDaemon(true);
            thread.start();
            return this;
        }
    }

    public Worker stop() {
        synchronized (lockObject) {
            if (status.equals(Status.StopRequested)) {
                return this;
            }
            status = Status.StopRequested;
            logger.info("Worker thread shutdown, thread id:{}", thread.getName());
            thread.interrupt();
            return this;
        }
    }

    private void loop() {
        while (this.status == Status.Running) {
            try {
                action.apply();
            } catch (Exception ex) {
                logger.error("Worker thread has exception, actionName: {}", actionName, ex);
            }
        }
    }

    enum Status {
        Initial,
        Running,
        StopRequested
    }
}
