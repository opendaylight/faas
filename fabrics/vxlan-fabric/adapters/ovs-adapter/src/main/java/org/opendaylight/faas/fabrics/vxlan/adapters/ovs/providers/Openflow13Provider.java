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
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineL2Forwarding;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineL3Forwarding;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineL3Routing;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineTrafficClassifier;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBdIf;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdpaterAction;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.AccessLists;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.Acl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.AclKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricOptions.TrafficBehavior;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Openflow13Provider {
    //private static final Logger LOG = LoggerFactory.getLogger(Openflow13Provider.class);

    private PipelineTrafficClassifier trafficClassifier;
    private PipelineArpHandler arpHandler;
    private PipelineL3Routing l3Routing;
    private PipelineL3Forwarding l3Forwarding;
    private PipelineAclHandler aclHandler;
    private PipelineL2Forwarding l2Forwarding;

    private DataBroker databroker = null;

    public Openflow13Provider(DataBroker databroker) {
        this.databroker = databroker;
    }

    public void initialOpenflowPipeline(Node node) {

        trafficClassifier = new PipelineTrafficClassifier(databroker);
        trafficClassifier.programDefaultPipelineRule(node);

        arpHandler = new PipelineArpHandler(databroker);
        arpHandler.programDefaultPipelineRule(node);

        l3Routing = new PipelineL3Routing(databroker);
        l3Routing.programDefaultPipelineRule(node);

        l3Forwarding = new PipelineL3Forwarding(databroker);
        l3Forwarding.programDefaultPipelineRule(node);

        aclHandler = new PipelineAclHandler(databroker);
        aclHandler.programDefaultPipelineRule(node);

        l2Forwarding = new PipelineL2Forwarding(databroker);
        l2Forwarding.programDefaultPipelineRule(node);

    }

//    private long getDpid(Node node) {
//        long dpid = OvsSouthboundUtils.getDataPathId(node);
//        if (dpid == 0) {
//            LOG.warn("getDpid: dpid not found: {}", node);
//        }
//        return dpid;
//    }

    //The host route is located in this Device
    public void addLocalHostRouteInDevice(Long dpidLong, Long ofPort, Long gpeTunnelOfPort, HostRoute hostRoute)
    {
        Long segmentationId = hostRoute.getVni();
        String macAddress = hostRoute.getMac().getValue();
        IpAddress ipAddress = hostRoute.getIp();

        trafficClassifier.programLocalInPort(dpidLong, segmentationId, ofPort, macAddress, true);
        trafficClassifier.programDropSrcIface(dpidLong, ofPort, true);

        arpHandler.programStaticArpEntry(dpidLong, segmentationId, macAddress, ipAddress, AdpaterAction.ADD);

        l3Forwarding.programForwardingTableEntry(dpidLong, segmentationId, ipAddress, macAddress, AdpaterAction.ADD);

        l2Forwarding.programLocalUcastOut(dpidLong, segmentationId, ofPort, macAddress, true);
        l2Forwarding.programRemoteBcastOutToLocalPort(dpidLong, segmentationId, ofPort, true);
        l2Forwarding.programLocalBcastToLocalPort(dpidLong, segmentationId, ofPort, true);
        //l2Forwarding.programLocalBcastToTunnelPort(dpidLong, segmentationId, ofPort, dstTunIpAddress, true);

        if(gpeTunnelOfPort != 0l) {
            IpAddress dstTunIp = hostRoute.getDestVtep();

            l2Forwarding.programSfcTunnelOut(dpidLong, segmentationId, gpeTunnelOfPort, dstTunIp, true);
        }

    }

    //The host route is located in The remove Device, add some flows in this device for the remote host route
    public void addRemoteHostRouteInDevice(Long dpidLong, Long tunnelOfPort, Long gpeTunnelOfPort, HostRoute hostRoute)
    {
        Long segmentationId = hostRoute.getVni();
        String macAddress = hostRoute.getMac().getValue();
        IpAddress ipAddress = hostRoute.getIp();

        IpAddress dstTunIp = hostRoute.getDestVtep();

        arpHandler.programStaticArpEntry(dpidLong, segmentationId, macAddress, ipAddress, AdpaterAction.ADD);

        l3Forwarding.programForwardingTableEntry(dpidLong, segmentationId, ipAddress, macAddress, AdpaterAction.ADD);

        if(gpeTunnelOfPort != 0l) {
            l2Forwarding.programSfcTunnelOut(dpidLong, segmentationId, gpeTunnelOfPort, dstTunIp, true);
        }

        l2Forwarding.programTunnelOut(dpidLong, segmentationId, tunnelOfPort, macAddress, dstTunIp, true);

        //Because in Device context, it is difficult to find the peer's vtep ip, so add flood to tunnel port function here
        // 20151125 remove: l2Forwarding.programLocalBcastToTunnelPort(dpidLong, segmentationId, tunnelOfPort, dstTunIp, true);

    }

    //For the condition: Add a new vni in a fabric capable device
    public void addBridgeDomainInDevice(Long dpidLong, Long tunnelOfPort, Long segmentationId) {
        trafficClassifier.programTunnelIn(dpidLong, segmentationId, tunnelOfPort, true);

//        if (dstTunIpAddress != null)
//            l2Forwarding.programLocalBcastToTunnelPort(dpidLong, segmentationId, tunnelOfPort, dstTunIpAddress, true);


    }


    public void addBdifInDevice(Long dpidLong, List<AdapterBdIf> bdIfs, AdapterBdIf newBdIf) {

        Long newBdIfSegId = newBdIf.getVni();
        String newBdIfMac = newBdIf.getMacAddress().getValue();
        IpAddress newBdIfIp = newBdIf.getIpAddress();
        int newBdIfMask = newBdIf.getMask();

        arpHandler.programStaticArpEntry(dpidLong, newBdIfSegId, newBdIfMac, newBdIfIp, AdpaterAction.ADD);

        for (AdapterBdIf bdIf : bdIfs) {
            if ((bdIf.getVrf() == newBdIf.getVrf()) ) {
                Long bdIfSegId = bdIf.getVni();
                String bdifMac = bdIf.getMacAddress().getValue();
                IpAddress bdIfIp = bdIf.getIpAddress();
                int bdIfMask = bdIf.getMask();

                if (bdIfSegId != newBdIfSegId) {
                    l3Routing.programRouterInterface(dpidLong, bdIfSegId, newBdIfSegId, newBdIfMac, newBdIfIp, newBdIfMask, AdpaterAction.ADD);
                    l3Routing.programRouterInterface(dpidLong, newBdIfSegId, bdIfSegId, bdifMac, bdIfIp, bdIfMask, AdpaterAction.ADD);
                }
            }
        }

    }

    public void addVniMembersInDevice(Long dpidLong, Long tunnelOfPort, Long segmentationId, IpAddress dstTunIp) {
        //Add remote tunnel IP to broadcast group belongs to this Bridge Domain(segmentationId)
        l2Forwarding.programLocalBcastToTunnelPort(dpidLong, segmentationId, tunnelOfPort, dstTunIp, true);
    }

    public void setTrafficBehavior(Long dpidLong, TrafficBehavior trafficBehavior) {
        aclHandler.programTrafficBehaviorRule(dpidLong, trafficBehavior);
    }

    public void addAclsInDevice(Long dpidLong, FabricAcl fabricAcl) {
        String ietfAclName = fabricAcl.getFabricAclName();
        InstanceIdentifier<Acl> aclIID = InstanceIdentifier.create(AccessLists.class).child(Acl.class, new AclKey(ietfAclName));

        Acl acl = MdsalUtils.read(LogicalDatastoreType.OPERATIONAL, aclIID, databroker);
        if (acl == null)
            return;

        aclHandler.programAclEntry(dpidLong, acl, true);
    }

    public static NodeBuilder createNodeBuilder(String nodeId) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder;
    }
}
