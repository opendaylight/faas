/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

public enum STPPortState {
    disabled(1), blocking(2), listening(3), learning(4), forwarding(5), broken(6);

    private int value;

    public int getValue() {
        return value;
    }

    private STPPortState(int value) {
        this.value = value;
    }

}
