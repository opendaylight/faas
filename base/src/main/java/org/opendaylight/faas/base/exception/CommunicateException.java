/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.exception;

public class CommunicateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CommunicateException(String message, Throwable obj) throws Throwable {
        super(message);
        throw obj;
    }

    public CommunicateException(String message) {
        super(message);

    }

    public CommunicateException() {
        super("communication error!");
    }
}
