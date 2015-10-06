/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

import java.util.ArrayList;
import java.util.List;

final public class Topology {

    final private List<Link> linkList = new ArrayList<Link>();
    final private List<NetNode> nodeList = new ArrayList<NetNode>();
    final private PathCalcAlg alg = PathCalcAlg.stp;

    // make a defensive copy
    public List<NetNode> getNodeList() {
        return nodeList;
    }

    public Topology() {
        super();
    }

    @Override
    public String toString() {
        return "Topology [linkList=" + linkList + ", nodeList=" + nodeList + "]";
    }

    public List<String> getLinkListIDs() {
        List<String> ids = new ArrayList<String>();
        for (Link link : linkList) {
            ids.add(link.getId());
        }

        return ids;
    }

    public List<Link> getLinkList() {
        return linkList;
    }

    public void removeLink(Link link) {
        this.linkList.remove(link);
    }

    public void addLink(Link link) {
        this.linkList.add(link);
    }

    public void addAllLinks(List<Link> links) {
        this.linkList.addAll(links);
    }

    public void removeNode(NetNode node) {
        this.nodeList.remove(node);
    }

    public void addNode(NetNode node) {
        this.nodeList.add(node);
    }

    public void addAllNodes(List<NetNode> nodeIDs) {
        this.nodeList.addAll(nodeIDs);
    }

    public PathCalcAlg getAlg() {
        return alg;
    }
}
