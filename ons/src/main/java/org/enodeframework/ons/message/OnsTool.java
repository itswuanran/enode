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

import com.aliyun.openservices.ons.api.Message;
import org.enodeframework.queue.QueueMessage;

import java.nio.charset.StandardCharsets;

/**
 * @author anruence@gmail.com
 */
public class OnsTool {

    public static QueueMessage covertToQueueMessage(Message messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        String value = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        int length = value.length();
        // 格式为{}|1
        queueMessage.setBody(value.substring(0, length - 2));
        queueMessage.setType(value.charAt(length - 1));
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setTag(messageExt.getTag());
        queueMessage.setRouteKey(messageExt.getShardingKey());
        queueMessage.setKey(messageExt.getKey());
        return queueMessage;
    }

    public static Message covertToProducerRecord(QueueMessage queueMessage) {
        Message message = new Message(
            queueMessage.getTopic(),
            queueMessage.getTag(),
            queueMessage.getKey(),
            queueMessage.getBodyAndType().getBytes(StandardCharsets.UTF_8));
        message.setShardingKey(queueMessage.getRouteKey());
        return message;
    }
}
