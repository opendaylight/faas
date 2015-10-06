/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

//
// TODO, this class needs to be redesigned.
// I don't really like the "type" part. should be able to find better ways,
// for example inheritance?
//

public class Link {

    public enum Creator {
        system, user
    }

    private final Creator creator;

    public Creator getCreator() {
        return creator;
    }

    public static Link createSystemLink(VIF vIFA, VIF vIFB) {
        return new Link(vIFA, vIFB, "");
    }

    public static Link createUserLink(VIF vIFA, VIF vIFB) {
        return new Link(vIFA, vIFB);
    }

    public Link(VIF vIFA, VIF vIFB) {
        super();
        this.vIFA = vIFA;
        this.vIFB = vIFB;

        if (vIFA != null && vIFB != null) {
            if (vIFA.hashCode() > vIFB.hashCode())
                this.name = vIFA.getId() + "-" + vIFB.getId();
            else
                this.name = vIFB.getId() + "-" + vIFA.getId();
        } else {
            this.name = "unknown-error";
        }
        this.id = this.name;
        this.creator = Creator.user;
    }

    private Link(VIF vIFA, VIF vIFB, String unused) {
        super();
        this.vIFA = vIFA;
        this.vIFB = vIFB;
        if (vIFA.hashCode() > vIFB.hashCode())
            this.name = vIFA.getId() + "-" + vIFB.getId();
        else
            this.name = vIFB.getId() + "-" + vIFA.getId();

        this.id = this.name;
        this.creator = Creator.system;
    }

    public VIF getVIFA() {
        return vIFA;
    }

    public VIF getVIFB() {
        return vIFB;
    }

    final private VIF vIFA;

    public String getId() {
        return id;
    }

    final private VIF vIFB;

    final private String id;

    final private String name;

    public String getName() {
        return name;
    }

    private String netNodeId;

    public void setNetNodeId(String nodeId) {
        this.netNodeId = nodeId;
    }

    public String getNetNodeId() {
        return this.netNodeId;
    }

    public void addNetNodeToVif(String netNodeId) {
        this.vIFA.addNetNodeID(netNodeId);
        this.vIFB.addNetNodeID(netNodeId);
    }

}
