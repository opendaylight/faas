/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.exception;

public class InvalidResourceNodeException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidResourceNodeException() {
        super();
    }

    public InvalidResourceNodeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super("Invalid Resource Node found!", cause, enableSuppression, writableStackTrace);
    }

    public InvalidResourceNodeException(String message, Throwable cause) {
        super("Invalid Resource Node found!", cause);
    }

    public InvalidResourceNodeException(String message) {
        super("Invalid Resource Node found!");
    }

    public InvalidResourceNodeException(Throwable cause) {
        super(cause);
    }

}
