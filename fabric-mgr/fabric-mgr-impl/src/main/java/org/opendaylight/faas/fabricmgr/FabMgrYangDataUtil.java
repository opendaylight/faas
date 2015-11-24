/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.VcNetNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.VcNetNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.network.topology.topology.node.vc.node.attributes.NetNodeAttributesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.FlagIdentity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.TopologyTypes1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.UndefinedFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.VcNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.VcNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.vc.node.attributes.VcNodeAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.vc.node.attributes.VcNodeAttributesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.vcontainer.topology.type.VcontainerTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.TopologyTypes;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.ldnode.rev151010.VcLdNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.ldnode.rev151010.VcLdNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.ldnode.rev151010.network.topology.topology.node.vc.node.attributes.LdNodeAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.ldnode.rev151010.network.topology.topology.node.vc.node.attributes.LdNodeAttributesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;

public class FabMgrYangDataUtil {

    public static final String VC_TOPOLOGY_ID = "faas:vcontainers";
    private static final String VC_LD_NODE_NAME = "faas:vc:ldnode";
    private static final String VC_NET_NODE_NAME = "faas:vc:netnode";
    private static final String VC_LINK_NAME = "faas:vc:link";
    private static final String VC_NODE_TP_WEST = "faas:vc:node:tp:west";
    private static final String VC_NODE_TP_EAST = "faas:vc:node:tp:east";

    public static final InstanceIdentifier<Topology> DOM_VCS_PATH = InstanceIdentifier.create(NetworkTopology.class)
        .child(Topology.class, new TopologyKey(new TopologyId(new Uri(VC_TOPOLOGY_ID))));

    /*
     * VContainer
     */
    public static InstanceIdentifier<Topology> vcTopology(String tenantId) {
        return InstanceIdentifier.builder(NetworkTopology.class)
            .child(Topology.class, new TopologyKey(new TopologyId(new Uri(tenantId))))
            .build();
    }

    public static InstanceIdentifier<VcontainerTopology> vcTopologyType(String tenantId) {
        TopologyId topoId = new TopologyId(new Uri(tenantId));
        TopologyKey topoKey = new TopologyKey(topoId);
        InstanceIdentifier<NetworkTopology> nt = InstanceIdentifier.create(NetworkTopology.class);
        InstanceIdentifier<Topology> topo = nt.child(Topology.class, topoKey);
        InstanceIdentifier<TopologyTypes> topoType = topo.child(TopologyTypes.class);
        InstanceIdentifier<TopologyTypes1> augTopoType = topoType.augmentation(TopologyTypes1.class);
        InstanceIdentifier<VcontainerTopology> vcTopoType = augTopoType.child(VcontainerTopology.class);
        return vcTopoType;
    }

    public static InstanceIdentifier<Topology> topologyPath(TopologyKey topoKey) {
        return InstanceIdentifier.create(NetworkTopology.class).child(Topology.class, topoKey);
    }

    public static InstanceIdentifier<Node> vcLdNodePath(InstanceIdentifier<Topology> topoPath) {
        return nodePath(topoPath, VC_LD_NODE_NAME);
    }

    public static InstanceIdentifier<Node> vcNetNodePath(InstanceIdentifier<Topology> topoPath) {
        return nodePath(topoPath, VC_NET_NODE_NAME);
    }

    public static InstanceIdentifier<Node> nodePath(InstanceIdentifier<Topology> topoPath, String nodeName) {
        NodeId nodeId = new NodeId(nodeName);
        NodeKey nodeKey = new NodeKey(nodeId);
        InstanceIdentifierBuilder<Topology> topoPathBuilder = topoPath.builder();
        InstanceIdentifierBuilder<Node> nodePathBuilder = topoPathBuilder.child(Node.class, nodeKey);
        InstanceIdentifier<Node> nodePath = nodePathBuilder.build();
        return nodePath;
    }

    public static Node createBasicVcLdNode() {
        /*
         * Node <-- VcNode <>-- VcNodeAttributes <-- VcLdNode <>-- LdNodeAttributes
         */
        LdNodeAttributesBuilder ldNodeAttrBuilder = new LdNodeAttributesBuilder();
        ldNodeAttrBuilder.setIsLdNodeEmpty(true);
        VcLdNodeBuilder vcLdNodeBuilder = new VcLdNodeBuilder();
        vcLdNodeBuilder.setLdNodeAttributes(ldNodeAttrBuilder.build());

        VcNode baseVcNode = createBasicVcNode(VC_LD_NODE_NAME);
        VcNodeAttributesBuilder vcNodeAttrBuilder = new VcNodeAttributesBuilder(baseVcNode.getVcNodeAttributes());
        vcNodeAttrBuilder.addAugmentation(VcLdNode.class, vcLdNodeBuilder.build());

        VcNodeBuilder vcNodeBuilder = new VcNodeBuilder();
        vcNodeBuilder.setVcNodeAttributes(vcNodeAttrBuilder.build());

        Node baseNode = createBasicNode(VC_LD_NODE_NAME);
        NodeBuilder nodeBuilder = new NodeBuilder(baseNode);
        nodeBuilder.addAugmentation(VcNode.class, vcNodeBuilder.build());

        return nodeBuilder.build();
    }

    public static Node createBasicVcNetNode() {
        /*
         * Node <-- VcNode <>-- VcNodeAttributes <-- VcNetNode <>-- NetNodeAttributes
         */
        NetNodeAttributesBuilder netNodeAttrBuilder = new NetNodeAttributesBuilder();
        netNodeAttrBuilder.setIsNetNodeEmpty(true);
        VcNetNodeBuilder vcNetNodeBuilder = new VcNetNodeBuilder();
        vcNetNodeBuilder.setNetNodeAttributes(netNodeAttrBuilder.build());

        VcNode baseVcNode = createBasicVcNode(VC_NET_NODE_NAME);
        VcNodeAttributesBuilder vcNodeAttrBuilder = new VcNodeAttributesBuilder(baseVcNode.getVcNodeAttributes());
        vcNodeAttrBuilder.addAugmentation(VcNetNode.class, vcNetNodeBuilder.build());

        VcNodeBuilder vcNodeBuilder = new VcNodeBuilder();
        vcNodeBuilder.setVcNodeAttributes(vcNodeAttrBuilder.build());

        Node baseNode = createBasicNode(VC_NET_NODE_NAME);
        NodeBuilder nodeBuilder = new NodeBuilder(baseNode);
        nodeBuilder.addAugmentation(VcNode.class, vcNodeBuilder.build());

        return nodeBuilder.build();
    }

    public static VcNode createBasicVcNode(String nodeIdStr) {

        List<Class<? extends FlagIdentity>> flagList = new ArrayList<Class<? extends FlagIdentity>>();
        VcNodeAttributesBuilder vcAttrBuilder = new VcNodeAttributesBuilder();
        vcAttrBuilder.setName(nodeIdStr);
        vcAttrBuilder.setFlag(flagList);

        VcNodeBuilder vcNodeBuilder = new VcNodeBuilder();
        vcNodeBuilder.setVcNodeAttributes(vcAttrBuilder.build());

        return vcNodeBuilder.build();
    }

    public static Node createBasicNode(String nodeIdStr) {
        NodeId nodeId = new NodeId(nodeIdStr);
        NodeKey nodeKey = new NodeKey(nodeId);
        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setKey(nodeKey);
        nodeBuilder.setNodeId(nodeId);
        List<SupportingNode> childNodeList = new ArrayList<SupportingNode>();
        nodeBuilder.setSupportingNode(childNodeList);
        List<TerminationPoint> ports = new ArrayList<TerminationPoint>();
        TerminationPoint tpWest = createTp(VC_NODE_TP_WEST);
        ports.add(tpWest);
        TerminationPoint tpEast = createTp(VC_NODE_TP_EAST);
        ports.add(tpEast);
        nodeBuilder.setTerminationPoint(ports);

        return nodeBuilder.build();
    }

    public static TerminationPoint createTp(String tpIdStr) {
        TpId tpId = new TpId(tpIdStr);

        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setTpId(tpId);
        List<TpId> childPortList = new ArrayList<TpId>();
        tpBuilder.setTpRef(childPortList);

        return tpBuilder.build();
    }

    public static InstanceIdentifier<Link> vcLinkPath(InstanceIdentifier<Topology> topoPath) {
        return linkPath(topoPath, VC_LINK_NAME);
    }

    public static InstanceIdentifier<Link> linkPath(InstanceIdentifier<Topology> topoPath, String linkIdStr) {
        LinkId linkId = new LinkId(linkIdStr);
        LinkKey linkKey = new LinkKey(linkId);
        InstanceIdentifierBuilder<Topology> topoPathBuilder = topoPath.builder();
        InstanceIdentifierBuilder<Link> linkPathBuilder = topoPathBuilder.child(Link.class, linkKey);
        InstanceIdentifier<Link> linkPath = linkPathBuilder.build();
        return linkPath;
    }

    public static Link createBasicVcLink(Node ldNode, Node netNode) {
        LinkId linkId = new LinkId(VC_LINK_NAME);
        LinkKey linkKey = new LinkKey(linkId);
        LinkBuilder linkBuilder = new LinkBuilder();

        linkBuilder.setLinkId(linkId);
        linkBuilder.setKey(linkKey);

        DestinationBuilder destBuilder = new DestinationBuilder();
        NodeId ldNodeId = ldNode.getNodeId();
        destBuilder.setDestNode(ldNodeId);
        TpId sourceTpId = new TpId("VC_NODE_TP_WEST");
        destBuilder.setDestTp(sourceTpId);
        linkBuilder.setDestination(destBuilder.build());

        SourceBuilder sourceBuilder = new SourceBuilder();
        NodeId netNodeId = netNode.getNodeId();
        sourceBuilder.setSourceNode(netNodeId);
        TpId destTpId = new TpId("VC_NODE_TP_EAST");
        sourceBuilder.setSourceTp(destTpId);
        linkBuilder.setSource(sourceBuilder.build());

        return linkBuilder.build();
    }
}
