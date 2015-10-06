/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.exception;

public class NotimplementedYetException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotimplementedYetException() {
        super();
    }

    public NotimplementedYetException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotimplementedYetException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotimplementedYetException(String message) {
        super(message);
    }

    public NotimplementedYetException(Throwable cause) {
        super(cause);
    }

}
