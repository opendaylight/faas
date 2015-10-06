/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.HashMap;

public class LinkMap extends HashMap<String, BundleLink<String>> {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (BundleLink<String> link : this.values()) {
            builder.append(" Link from Node[" + link.getFrom() + "] ----  to Node [" + link.getTo() + "]\n");
            for (BundleLink<String> channel : link.getSubLinks()) {
                builder.append(" ---- Channel from port " + channel.getFrom() + " ---- to port " + channel.getTo()
                        + "\n");
            }
        }

        return builder.toString();
    }
}
