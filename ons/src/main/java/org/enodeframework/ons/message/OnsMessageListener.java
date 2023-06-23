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
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import org.enodeframework.common.extensions.SysProperties;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.MessageHandler;
import org.enodeframework.queue.MessageHandlerHolder;
import org.enodeframework.queue.QueueMessage;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class OnsMessageListener implements MessageListener, BatchMessageListener {

    private final MessageHandlerHolder messageHandlerHolder;

    public OnsMessageListener(MessageHandlerHolder messageHandlerHolder) {
        this.messageHandlerHolder = messageHandlerHolder;
    }

    @Override
    public Action consume(Message msg, ConsumeContext consumeContext) {
        QueueMessage queueMessage = this.covertToQueueMessage(msg);
        MessageHandler messageHandler = messageHandlerHolder.chooseMessageHandler(queueMessage.getType());
        CountDownLatch latch = new CountDownLatch(1);
        messageHandler.handle(queueMessage, message -> {
            latch.countDown();
        });
        Task.await(latch);
        return Action.CommitMessage;
    }

    @Override
    public Action consume(List<Message> messages, ConsumeContext consumeContext) {
        CountDownLatch latch = new CountDownLatch(messages.size());
        messages.forEach(msg -> {
            QueueMessage queueMessage = this.covertToQueueMessage(msg);
            MessageHandler messageHandler = messageHandlerHolder.chooseMessageHandler(queueMessage.getType());
            messageHandler.handle(queueMessage, message -> {
                latch.countDown();
            });
        });
        Task.await(latch);
        return Action.CommitMessage;
    }

    private QueueMessage covertToQueueMessage(Message messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        String mType = messageExt.getUserProperties(SysProperties.MESSAGE_TYPE_KEY);
        queueMessage.setBody(messageExt.getBody());
        queueMessage.setType(mType);
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setTag(messageExt.getTag());
        queueMessage.setRouteKey(messageExt.getShardingKey());
        queueMessage.setKey(messageExt.getKey());
        return queueMessage;
    }
}
