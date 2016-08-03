/*
 * Copyright (c) 2015, 2016 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.cache;

/**
 * The key identifed a unique rendered link between rendered switch A and rendered switch B.
 * This may not be optimal, because for different topology, it may use different physical link if
 * multiple link exist between those two rendred switches. physical link info may be required.
 * We will look into the optimization part later.
 *
 */
public class RenderedLinkKey<T> {
    private final  T aswitch;
    private final  T bswitch;

    public T getAswitch() {
        return aswitch;
    }

    public T getBswitch() {
        return bswitch;
    }

    public RenderedLinkKey(T aswitch, T bswitch) {
        super();
        this.aswitch = aswitch;
        this.bswitch = bswitch;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aswitch == null) ? 0 : aswitch.hashCode());
        result = prime * result + ((bswitch == null) ? 0 : bswitch.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RenderedLinkKey other = (RenderedLinkKey) obj;
        if (aswitch == null) {
            if (other.aswitch != null)
                return false;
        } else if (!aswitch.equals(other.aswitch))
            return false;
        if (bswitch == null) {
            if (other.bswitch != null)
                return false;
        } else if (!bswitch.equals(other.bswitch))
            return false;
        return true;
    }
}
