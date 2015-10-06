/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

public enum LinkType {
    ACCESS("access"), DOT1QTUNNEL("dot1q-tunnel"), HYBRID("hybrid"), TRUNK("trunk");

    String value;

    LinkType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
