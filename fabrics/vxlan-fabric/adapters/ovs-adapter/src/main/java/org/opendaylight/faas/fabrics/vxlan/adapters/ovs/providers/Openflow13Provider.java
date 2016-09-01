/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers;

import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineAclHandler;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineArpHandler;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineInboundNat;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineL2Forwarding;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineL3Forwarding;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineL3Routing;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineMacLearning;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineOutboundNat;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineTrafficClassifier;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBdIf;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.MdsalUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfMatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.AccessLists;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.Ipv4Acl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.Acl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.AclKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricOptions.TrafficBehavior;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.AccessType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.port.functions.port.function.function.type.ip.mapping.IpMappingEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.rib.VxlanRouteAug;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Openflow13Provider {

    private PipelineTrafficClassifier trafficClassifier;
    private PipelineMacLearning macLearning;
    private PipelineArpHandler arpHandler;
    private PipelineInboundNat inboundNat;
    private PipelineL3Routing l3Routing;
    private PipelineL3Forwarding l3Forwarding;
    private PipelineAclHandler aclHandler;
    private PipelineOutboundNat outboundNat;
    private PipelineL2Forwarding l2Forwarding;

    private DataBroker databroker = null;

    public Openflow13Provider(DataBroker databroker) {
        this.databroker = databroker;
    }

    public void initialOpenflowPipeline(Node node) {

        trafficClassifier = new PipelineTrafficClassifier(databroker);
        trafficClassifier.programDefaultPipelineRule(node);

        macLearning = new PipelineMacLearning(databroker);
        macLearning.programDefaultPipelineRule(node);

        arpHandler = new PipelineArpHandler(databroker);
        arpHandler.programDefaultPipelineRule(node);

        inboundNat = new PipelineInboundNat(databroker);
        inboundNat.programDefaultPipelineRule(node);

        l3Routing = new PipelineL3Routing(databroker);
        l3Routing.programDefaultPipelineRule(node);

        l3Forwarding = new PipelineL3Forwarding(databroker);
        l3Forwarding.programDefaultPipelineRule(node);

        aclHandler = new PipelineAclHandler(databroker);
        aclHandler.programDefaultPipelineRule(node);

        outboundNat = new PipelineOutboundNat(databroker);
        outboundNat.programDefaultPipelineRule(node);

        l2Forwarding = new PipelineL2Forwarding(databroker);
        l2Forwarding.programDefaultPipelineRule(node);

    }

    // The host route is located in this Device
    public void updateLocalHostRouteInDevice(Long dpid, boolean isAclRedirectCapability, Long ofPort, Long gpeTunnelOfPort, Long vlanId,
            HostRoute hostRoute, boolean writeFlow) {
        Long segmentationId = hostRoute.getVni();
        String macAddress = hostRoute.getMac().getValue();
        IpAddress ipAddress = hostRoute.getIp();

        trafficClassifier.programLocalInPort(dpid, segmentationId, vlanId, ofPort, macAddress, writeFlow);
        // trafficClassifier.programDropSrcIface(dpid, ofPort, writeFlow);

        arpHandler.programStaticArpEntry(dpid, segmentationId, macAddress, ipAddress, writeFlow);

        l3Forwarding.programForwardingTableEntry(dpid, segmentationId,
                OfMatchUtils.iPv4PrefixFromIPv4Address(ipAddress.getIpv4Address().getValue()), macAddress, writeFlow);

        l2Forwarding.programLocalUcastOut(dpid, segmentationId, vlanId, ofPort, macAddress, writeFlow);

        if (vlanId == null) {
            l2Forwarding.programBcastToLocalPort(dpid, segmentationId, ofPort, writeFlow);
        }
        if (gpeTunnelOfPort != null) {
            IpAddress dstTunIp = hostRoute.getDestVtep();

            if (isAclRedirectCapability)
                l2Forwarding.programSfcTunnelOut(dpid, segmentationId, gpeTunnelOfPort, macAddress, dstTunIp, writeFlow);
        }

    }

    // The host route is located in The remove Device, add some flows in this
    // device for the remote host route
    public void updateRemoteHostRouteInDevice(Long dpid, boolean isAclRedirectCapability, Long tunnelOfPort, Long gpeTunnelOfPort, HostRoute hostRoute,
            boolean writeFlow) {
        Long segmentationId = hostRoute.getVni();
        String macAddress = hostRoute.getMac().getValue();
        IpAddress ipAddress = hostRoute.getIp();

        IpAddress dstTunIp = hostRoute.getDestVtep();

        arpHandler.programStaticArpEntry(dpid, segmentationId, macAddress, ipAddress, writeFlow);

        l3Forwarding.programForwardingTableEntry(dpid, segmentationId,
                OfMatchUtils.iPv4PrefixFromIPv4Address(ipAddress.getIpv4Address().getValue()), macAddress, writeFlow);

        if (gpeTunnelOfPort != null) {
            if (isAclRedirectCapability)
                l2Forwarding.programSfcTunnelOut(dpid, segmentationId, gpeTunnelOfPort, macAddress, dstTunIp, writeFlow);
        }

        l2Forwarding.programTunnelOut(dpid, segmentationId, tunnelOfPort, macAddress, dstTunIp, writeFlow);
    }

    // For the condition: Add a new vni in a fabric capable device
    public void updateBridgeDomainInDevice(Long dpid, Long tunnelOfPort, Long segmentationId, boolean writeFlow) {
        trafficClassifier.programTunnelIn(dpid, segmentationId, tunnelOfPort, writeFlow);
    }

    public void updateSfcTunnelInDevice(Long dpid, boolean isAclRedirectCapability, Long gpeTunnelOfPort,
            Long segmentationId, boolean writeFlow) {
        if (isAclRedirectCapability)
            aclHandler.programGpeTunnelInEntry(dpid, segmentationId, gpeTunnelOfPort, writeFlow);
    }

    public void updateBdifInDevice(Long dpid, List<AdapterBdIf> bdIfs, AdapterBdIf newBdIf, boolean writeFlow) {

        Long newBdIfSegId = newBdIf.getVni();
        String newBdIfMac = newBdIf.getMacAddress().getValue();
        IpAddress newBdIfIp = newBdIf.getIpAddress();
        int newBdIfMask = newBdIf.getMask();

        arpHandler.programStaticArpEntry(dpid, newBdIfSegId, newBdIfMac, newBdIfIp, writeFlow);

        for (AdapterBdIf bdIf : bdIfs) {
            if ((bdIf.getVrf() == newBdIf.getVrf())) {
                Long bdIfSegId = bdIf.getVni();
                String bdifMac = bdIf.getMacAddress().getValue();
                IpAddress bdIfIp = bdIf.getIpAddress();
                int bdIfMask = bdIf.getMask();

                if (bdIfSegId != newBdIfSegId) {
                    l3Routing.programRouterInterface(dpid, bdIfSegId, newBdIfSegId, newBdIfMac, newBdIfIp, newBdIfMask,
                            writeFlow);
                    l3Routing.programRouterInterface(dpid, newBdIfSegId, bdIfSegId, bdifMac, bdIfIp, bdIfMask,
                            writeFlow);
                }
            }
        }

    }

    public void updateVniMembersInDevice(Long dpid, Long tunnelOfPort, Long segmentationId, IpAddress dstTunIp,
            boolean writeFlow) {
        l2Forwarding.programBcastToTunnelPort(dpid, segmentationId, tunnelOfPort, dstTunIp, writeFlow);
    }

    public void updateTrafficBehavior(Long dpid, TrafficBehavior trafficBehavior, boolean writeFlow) {
        aclHandler.programTrafficBehaviorRule(dpid, trafficBehavior, writeFlow);

        aclHandler.programDefaultPermitVni(dpid, writeFlow);

        if (trafficBehavior == TrafficBehavior.PolicyDriven) {
            // let Arp packet goto next table
            aclHandler.programArpPacketAllow(dpid, writeFlow);
        }
    }

    public void updateBridgeDomainAclsInDevice(Long dpid, Long segmentationId, FabricAcl fabricAcl, boolean writeFlow) {
        String ietfAclName = fabricAcl.getFabricAclName();
        InstanceIdentifier<Acl> aclIID = InstanceIdentifier.builder(AccessLists.class)
                .child(Acl.class, new AclKey(ietfAclName, Ipv4Acl.class)).build();
        Acl acl = MdsalUtils.read(LogicalDatastoreType.CONFIGURATION, aclIID, databroker);
        if (acl == null)
            return;

        aclHandler.programBridgeDomainAclEntry(dpid, segmentationId, acl, writeFlow);
    }

    public void updateBridgePortAclsInDevice(Long dpid, Long bridgePort, FabricAcl fabricAcl, boolean writeFlow) {
        String ietfAclName = fabricAcl.getFabricAclName();
        InstanceIdentifier<Acl> aclIID = InstanceIdentifier.builder(AccessLists.class)
                .child(Acl.class, new AclKey(ietfAclName, Ipv4Acl.class)).build();

        Acl acl = MdsalUtils.read(LogicalDatastoreType.CONFIGURATION, aclIID, databroker);
        if (acl == null)
            return;

        aclHandler.programBridgePortAclEntry(dpid, bridgePort, acl, writeFlow);
    }

    // If Bridge Domain Port has vlan tag, do vlan/vni mapping
    public void updateBdPortInDevice(Long dpid, Long ofPort, Long segmentationId, BdPort bdPort, boolean writeFlow) {
        if (bdPort.getAccessType() == AccessType.Vlan) {
            if (bdPort.getAccessTag() != null) {
                trafficClassifier.programVlanInPort(dpid, bdPort.getAccessTag(), segmentationId, ofPort, writeFlow);
                l2Forwarding.programBcastToVlanPort(dpid, segmentationId, ofPort, bdPort.getAccessTag(), writeFlow);
            }
        }
    }

    // Render the rib route entry on this device
    public void updateRouteInLocalDevice(Long dpid, String gwMacAddress, Route routeEntry, boolean writeFlow) {
        l3Routing.programStaticRoutingInLocalDevice(dpid, gwMacAddress, routeEntry.getDestinationPrefix(),
                routeEntry.getAugmentation(VxlanRouteAug.class).getOutgoingVni(), writeFlow);
    }

    public void updateRouteInRemoteDevice(Long dpid, String gwMacAddress, Route routeEntry, boolean writeFlow) {
        l3Routing.programStaticRoutingInRemoteDevice(dpid, gwMacAddress, routeEntry.getDestinationPrefix(),
                routeEntry.getAugmentation(VxlanRouteAug.class).getOutgoingVni(), writeFlow);
    }

    public void updateNexthopInLocalDevice(Long dpid, String nexthopMacAddress, Route routeEntry, boolean writeFlow) {
        l3Forwarding.programForwardingTableEntry(dpid, routeEntry.getAugmentation(VxlanRouteAug.class).getOutgoingVni(),
                routeEntry.getDestinationPrefix(), nexthopMacAddress, writeFlow);
    }

    public void updateNexthopInRemoteDevice(Long dpid, Long tunnelOfPort, IpAddress dstTunIpAddress,
            boolean writeFlow) {
        l2Forwarding.programNexthopTunnelOut(dpid, tunnelOfPort, dstTunIpAddress, writeFlow);
    }

    public void updateIpMappingInDevice(Long dpid, Long floatingSegmentId, String gwMacAddress,
            IpMappingEntry ipMapping, boolean writeFlow) {
        String floatingIp = ipMapping.getExternalIp().getValue();
        String fixedIp = ipMapping.getInternalIp().getValue();

        inboundNat.programFloatingIpToFixedIp(dpid, floatingIp, floatingSegmentId, fixedIp, writeFlow);

        outboundNat.programFixedIpToFloatingIp(dpid, floatingSegmentId, fixedIp, gwMacAddress, floatingIp, writeFlow);
    }

    public static NodeBuilder createNodeBuilder(String nodeId) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder;
    }
}
