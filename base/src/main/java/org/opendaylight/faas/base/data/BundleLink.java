/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BundleLink<T> {

    private List<BundleLink<T>> subLinks = new ArrayList<BundleLink<T>>();
    private final String id = UUID.randomUUID().toString();
    final private boolean bidirectional;
    final private T from;
    final private T to;
    private long cost;

    public List<BundleLink<T>> getSubLinks() {
        return subLinks;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getId() {
        return id;
    }

    public static <T> String getLinkName(T from, T to) {
        if (from.hashCode() > to.hashCode())
            return from + "-" + to;
        else
            return to + "-" + from;
    }

    public String getLinkName() {
        return getLinkName(from, to);
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public T getFrom() {
        return from;
    }

    public T getTo() {
        return to;
    }

    public BundleLink(boolean bidirectional, T from, T to, ArrayList<BundleLink<T>> links) {
        super();

        subLinks = links;
        this.bidirectional = bidirectional;
        this.from = from;
        this.to = to;
    }

    public BundleLink(T from, T to) {
        super();
        this.from = from;
        this.to = to;
        this.bidirectional = true;
    }

    public BundleLink(boolean bidirectional, T from, T to) {
        super();
        this.bidirectional = bidirectional;
        this.from = from;
        this.to = to;
    }

}
