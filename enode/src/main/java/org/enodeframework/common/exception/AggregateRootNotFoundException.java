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
package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class AggregateRootNotFoundException extends EnodeException {
    private static final String EXCEPTION_MESSAGE = "aggregate root [type=%s,id=%s] not found.";

    public AggregateRootNotFoundException() {
        super();
    }

    public AggregateRootNotFoundException(String msg) {
        super(msg);
    }

    public AggregateRootNotFoundException(String id, Class<?> type) {
        super(String.format(EXCEPTION_MESSAGE, type.getName(), id));
    }

    public AggregateRootNotFoundException(Throwable cause) {
        super(cause);
    }

    public AggregateRootNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
