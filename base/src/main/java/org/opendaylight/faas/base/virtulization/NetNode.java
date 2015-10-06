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
import org.opendaylight.faas.base.exception.InvalidResourceNodeException;

public class NetNode {

    final protected NetNodeType type;

    /**
     * User friendly display name
     */
    private String name;

    /**
     * Global unique id
     */
    private String id;

    /**
     * ID recognized on the device
     */
    private String systemID;

    /**
     * who created this object
     */
    final private String createdBy;

    /**
     * Who owns it
     */
    final private String tenantID;
    private String vdcId_m;

    private Topology topology = new Topology();
    private List<VIF> evifs = new ArrayList<VIF>();

    /**
     * ID of the Node which topology the NetNode is part of
     */
    private String parentID;

    /**
     * The resource netNode
     */
    final private String resourceNetNodeID;

    public static NetNode createUnknownNetNode(String id, List<VIF> vifs, String owner, String parentID,
            String adapterURL) {

        NetNodeBuilder builder = new NetNodeBuilder(id, NetNodeType.UNKNOWN);
        builder.setParentID(parentID).setName(id).setAbstractedBy(owner);
        builder.setEVIFs(vifs).setResourceNetNodeID(adapterURL);
        return builder.build();
    }

    public static NetNode createVFabricNetNode(String vFabName, String fabricNodeId, List<VIF> vifs, String tenantID) {
        NetNodeBuilder builder = new NetNodeBuilder(fabricNodeId + ":" + vFabName, NetNodeType.VFABRIC);
        builder.setName(vFabName)
            .setParentID(tenantID)
            .setAbstractedBy(tenantID)
            .setEVIFs(vifs)
            .setResourceNetNodeID(fabricNodeId);
        builder.setTenantID(tenantID);

        NetNode node = builder.build();

        for (VIF vif : vifs) {
            vif.setOwningServiceNetNodeID(node.getId());
        }

        return node;
    }

    public static NetNode createVDC(String vdcName, String resourceNetNodeID, List<VIF> vifs, String owner,
            String tenantID, DataContainer dc) throws InvalidResourceNodeException {
        NetNodeBuilder builder = new NetNodeBuilder(vdcName, NetNodeType.VPNETWORK);
        builder.setName(vdcName)
            .setParentID(owner)
            .setAbstractedBy(owner)
            .setEVIFs(vifs)
            .setResourceNetNodeID(resourceNetNodeID);
        builder.setTenantID(tenantID);

        NetNode rnode = dc.getNodeList().get(resourceNetNodeID);
        if (rnode == null) {
            throw new InvalidResourceNodeException();
        }
        builder.setTopology(rnode.getTopology());
        NetNode node = builder.build();

        for (VIF vif : vifs) {
            vif.setOwningServiceNetNodeID(node.getId());
        }

        return node;
    }

    public static NetNode createFabricNetNode(String name, String parentID, String id, String owner) {
        NetNodeBuilder builder = new NetNodeBuilder(id, NetNodeType.FABRIC);
        builder.setName(name).setParentID(parentID).setAbstractedBy(owner).setResourceNetNodeID(id);

        return builder.build();
    }

    
    public static NetNode createDCNetNode(String name, String parentID, String id, String owner) {

        NetNodeBuilder builder = new NetNodeBuilder(id, NetNodeType.PNETWORK);
        builder.setName(name).setParentID(parentID).setAbstractedBy(owner).setResourceNetNodeID(id);

        return builder.build();
    }

    public static NetNode createPartitionNetNode(String name, String parentID, String owner, String url) {

        NetNodeBuilder builder = new NetNodeBuilder(url, NetNodeType.PARTITION);
        builder.setName(name).setParentID(parentID).setAbstractedBy(owner);

        return builder.build();
    }

    public static NetNode createNetworkElement(String name, String parentID, String id, String owner) {
        NetNodeBuilder builder = new NetNodeBuilder(id, NetNodeType.PBRIDGE);
        builder.setParentID(parentID).setName(name).setAbstractedBy(owner);
        builder.setResourceNetNodeID(id);
        return builder.build();
    }

    public void setEVIFs(List<VIF> vifs) {
        this.evifs = vifs;
    }

    public void addEVIFs(List<VIF> vifs) {
        this.evifs.addAll(vifs);
    }

    public void addEVIF(VIF vif) {
        this.evifs.add(vif);
    }

    public void removeEVIF(VIF vif) {
        this.evifs.remove(vif);
    }

    
    synchronized public void initializeVIFS(List<String> excludeList) {
        for (NetNode node : topology.getNodeList()) {

            // could be further optimized.
            if (excludeList != null && excludeList.contains(node.getId()))
                continue;

            List<VIF> vifs = node.getEVIFs();
            for (VIF vif : vifs) {

                if (vif.getLinkID() == null || !topology.getLinkListIDs().contains(vif.getLinkID())) {
                    this.evifs.add(vif);
                }
            }
        }
    }

    public List<VIF> getEVIFs() {
        return evifs;
    }

    
    public void addNodeIdToEvifs(String nodeId) {
        for (VIF vif : this.evifs) {
            vif.addNetNodeID(nodeId);
        }
    }

    // Topology operations
    public void removeNode(NetNode node) {
        topology.removeNode(node);
    }

    public void removeLink(Link link) {
        topology.removeLink(link);
    }

    public void addNetNode(NetNode node) {
        topology.addNode(node);
    }

    public void addAllNetNodes(List<NetNode> nodeList) {
        topology.addAllNodes(nodeList);
    }

    public String getResourceNetNodeID() {
        return resourceNetNodeID;
    }

    public String getSystemID() {
        return systemID;
    }

    public void setSystemID(String systemID) {
        this.systemID = systemID;
    }

    public String getComposer() {
        return createdBy;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public NetNodeType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public Topology getTopology() {
        return topology;
    }

    public void setTopology(Topology topo) {
        this.topology = topo;
    }

    public String getTenantID() {
        return tenantID;
    }

    public String getVdcId() {
        return this.vdcId_m;
    }

    public void setVdcId(String vdcId) {
        this.vdcId_m = vdcId;
    }

    public String getOwner() {
        return createdBy;
    }

    public List<String> getAllLinks() {
        List<String> links = this.topology.getLinkListIDs();
        for (NetNode node : this.topology.getNodeList()) {
            links.addAll(node.getAllLinks());
        }
        return links;
    }

    public List<NetNode> getAllPhysicalLeaveNetnodes() throws Throwable {
        List<NetNode> leaves = new ArrayList<NetNode>();
        for (NetNode node : topology.getNodeList()) {
            if (node.type == NetNodeType.PBRIDGE) {
                leaves.add(node);
            } else {
                List<NetNode> ids = node.getAllPhysicalLeaveNetnodes();
                leaves.addAll(ids);
            }
        }
        return leaves;
    }

    @Override
    public String toString() {
        return "NetNode [type=" + type + ", name=" + name + ", id=" + id + ", composer=" + createdBy + "]"
                + ", tenantID=" + tenantID;
    }

    protected NetNode(NetNodeType type, String name, String id, String systemID, String abstractedBy,
            Topology topology, List<VIF> evifs, String parentID, String resourceNetNodeID, String tenantID, String vdcId) {
        super();
        this.type = type;
        this.name = name;
        this.id = id;
        this.systemID = systemID;
        this.createdBy = abstractedBy;
        this.topology = topology;
        this.evifs = evifs;
        this.parentID = parentID;
        this.resourceNetNodeID = resourceNetNodeID;
        this.tenantID = tenantID;
        this.vdcId_m = vdcId;
    }

    protected NetNode(NetNode node) {
        super();
        this.type = node.getType();
        this.name = node.getName();
        this.id = node.getId();
        this.createdBy = node.getAbstractedBy();
        this.topology = node.getTopology();
        this.evifs = node.getEVIFs();
        this.parentID = node.getParentID();
        this.resourceNetNodeID = node.getResourceNetNodeID();
        this.tenantID = node.getTenantID();
        this.vdcId_m = node.getVdcId();
    }

    public String getAbstractedBy() {
        return createdBy;
    }

    public static class NetNodeBuilder {

        private NetNodeType type;
        private String name;
        private String id;
        private String systemID;
        private String abstractedBy;
        private String tenantID;
        private String vdcId_m;
        private Topology topology = new Topology();
        private List<VIF> evifs = new ArrayList<VIF>();

        // ID of the parent Node in the abstraction tree.
        private String parentID;
        private String resourceNetNodeID;

        public NetNodeBuilder(String id, NetNodeType type) {
            this.id = id;
            this.type = type;
        }

        public void setVdcID(String vdcId) {
            this.vdcId_m = vdcId;
        }

        public NetNodeBuilder setType(NetNodeType type) {
            this.type = type;
            return this;
        }

        public NetNodeBuilder setTenantID(String tenantID) {
            this.tenantID = tenantID;
            return this;
        }

        public NetNodeBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public NetNodeBuilder setAbstractedBy(String abstractedBy) {
            this.abstractedBy = abstractedBy;
            return this;
        }

        public NetNodeBuilder setParentID(String parentID) {
            this.parentID = parentID;
            return this;
        }

        public NetNodeBuilder setResourceNetNodeID(String resourceNetNodeID) {
            this.resourceNetNodeID = resourceNetNodeID;
            return this;
        }

        public NetNodeBuilder setEVIFs(List<VIF> vifs) {
            this.evifs = vifs;
            return this;
        }

        public void setTopology(Topology topology) {
            this.topology = topology;
        }

        public void setSystemID(String id) {
            this.systemID = id;
        }

        NetNode build() {
            return new NetNode(type, name, id, systemID, abstractedBy, topology, evifs, parentID, resourceNetNodeID,
                    tenantID, vdcId_m);
        }
    }

    public void setId(String id) {
        this.id = id;
    }
}
