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
public class IdGenerateException extends EnodeException {

    private static final long serialVersionUID = -8951926710590746149L;

    public IdGenerateException() {
        super();
    }

    public IdGenerateException(Throwable throwable) {
        super(throwable);
    }

    public IdGenerateException(String msg) {
        super(msg);
    }

    public IdGenerateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
