/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.enodeframework.queue;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class OutboundDeliveryContext implements Handler<AsyncResult<Void>> {

    public final JsonObject message;

    public OutboundDeliveryContext(JsonObject message) {
        this.message = message;
    }

    @Override
    public void handle(AsyncResult<Void> event) {

    }

    public void written(Throwable cause) {

    }

}
