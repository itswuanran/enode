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
package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class OnsMessageListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(OnsMessageListener.class);

    private final Map<String, MessageHandler> messageHandlerMap;

    public OnsMessageListener(Map<String, MessageHandler> messageHandlerMap) {
        this.messageHandlerMap = messageHandlerMap;
    }

    @Override
    public Action consume(Message msg, ConsumeContext consumeContext) {
        QueueMessage queueMessage = OnsTool.covertToQueueMessage(msg);
        MessageHandler messageHandler = messageHandlerMap.get(queueMessage.getType());
        if (messageHandler == null) {
            logger.error("No messageHandler for message: {}.", queueMessage);
            return Action.CommitMessage;
        }
        CountDownLatch latch = new CountDownLatch(1);
        messageHandler.handle(queueMessage, message -> {
            latch.countDown();
        });
        Task.await(latch);
        return Action.CommitMessage;
    }
}
