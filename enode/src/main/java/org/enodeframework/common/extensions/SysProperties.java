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
package org.enodeframework.common.extensions;

/**
 * @author anruence@gmail.com
 */
public class SysProperties {
    /**
     * 聚合根方法执行时声明的name
     */
    public static final String AGGREGATE_ROOT_HANDLE_METHOD_NAME_PREFIX = "handle";
    public static final String ITEMS_COMMAND_RESULT_KEY = "COMMAND_RESULT";
    
    public static final String MESSAGE_TYPE_KEY = "MESSAGE_TYPE";
    public static final String MESSAGE_TAG_KEY = "ENODE_TAG";
    public static final String ITEMS_COMMAND_REPLY_ADDRESS_KEY = "COMMAND_REPLY_ADDRESS";

    public static final String CHANNEL_TAG_TPL = "%s#%s";

}
