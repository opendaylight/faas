/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr.api;

import org.opendaylight.faas.fabricmgr.FabricMgrProvider;
import org.opendaylight.faas.uln.cache.UserLogicalNetworkCache;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  A top-level Java class mimicking static class behavior
 *
 */

public final class VContainerServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(VContainerServiceProvider.class);
    private static FabricMgrProvider fabricMgrProvider;

    // Make it static like
    private VContainerServiceProvider() {
    }

    /**
     * To create a logical switch for a tenant.
     * @param tenantId - tenant identifier
     * @param lne2Input - input for logical switch creation
     * @return the id of the logical switch created.
     */
    public static NodeId createLneLayer2(Uuid tenantId, CreateLneLayer2Input lne2Input, UserLogicalNetworkCache cache) {
        return fabricMgrProvider.createLneLayer2(tenantId, lne2Input, cache);
    }

    /**
     * To remove a logical switch for a tenant.
     * @param tenantId - tenant identifier
     * @param lswId - logical switch identifier
     */
    public static void removeLneLayer2(Uuid tenantId, NodeId fabricId,  NodeId lswId) {
        fabricMgrProvider.removeLneLayer2(tenantId, fabricId, lswId);
    }

    /**
     * To create a logical router for a tenant.
     * @param tenantId : tenant UUID identifier
     * @param lne3Input : logical router parameters
     * @return the node identifier of th elogical router
     */
    public static NodeId createLneLayer3(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, CreateLneLayer3Input lne3Input) {
        return fabricMgrProvider.createLneLayer3(tenantId, fabricId, uln, lne3Input);
    }

    /**
     * To remove a logical router for a tenant.
     * @param tenantId : tenant UUID
     * @param lrId : the node identifier of logical router to be removed.
     */
    public static void removeLneLayer3(Uuid tenantId, NodeId fabricId, NodeId lrId) {
        fabricMgrProvider.removeLneLayer3(tenantId, fabricId,  lrId);
    }

    /**
     * To create a logical port on a logical switch.
     * @param tenantId : tenant identifier
     * @param lswId : logical switch identifier
     * @return the port id
     */
    public static TpId createLogicalPortOnLneLayer2(Uuid tenantId, NodeId lswId) {
        return fabricMgrProvider.createLogicalPortOnLneLayer2(tenantId, lswId);
    }

    /**
     * Bind an end point's physical location /physical port to a logical port on a given logical switch.
     * @param tenantId - tenant identifier.
     * @param lswId - logical switch identifier
     * @param lswLogicalPortId - logical port on the given logical switch
     * @param endpoint - end point attributes
     * @return the uuid of the end point
     */
    public static Uuid attachEpToLneLayer2(Uuid tenantId, NodeId lswId, TpId lswLogicalPortId,
            EndpointAttachInfo endpoint) {
        return fabricMgrProvider.attachEpToLneLayer2(tenantId, lswId, lswLogicalPortId,
                endpoint);
    }

    /**
     * Detach an end point from a given logical switch, this function is the reverse process of the attachEpToLneLayer2.
     * {@code attachEpToLneLayer2}.
     * @param tenantId : the tenant identifier
     * @param lswId : the logical swtich identifer
     * @param epUuid : the end point UUID
     */
    public static void unregisterEpFromLneLayer2(Uuid tenantId, NodeId fabricId, NodeId lswId, Uuid epUuid) {
        fabricMgrProvider.unregisterEpFromLneLayer2(tenantId, fabricId, lswId, epUuid);
    }

    public static void setFabricMgrProvider(FabricMgrProvider fabricMgrProvider) {
        VContainerServiceProvider.fabricMgrProvider = fabricMgrProvider;
    }

    public static FabricMgrProvider getFabricMgrProvider() {
        return fabricMgrProvider;
    }

    /**
     * To create a gateway on a logical router for a given logical switch.
     * @param tenantId: tenant identifier
     * @param lrId: logical router identifier
     * @param lswId: logical switch identifier
     * @param gatewayIpAddr : the ip address of the gateway
     * @param ipPrefix: the ip prefix ?
     */
    public static void createLrLswGateway(Uuid tenantId, NodeId lrId, NodeId lswId, IpAddress gatewayIpAddr,
            IpPrefix ipPrefix) {
        fabricMgrProvider.createLrLswGateway(tenantId, lrId, lswId, gatewayIpAddr,
                ipPrefix);
    }

    /**
     * To remove a gateway from a logical router.
     * @param tenantId : tenant identifier
     * @param lrId : logical router identifier
     * @param gatewayIpAddr : gateway IP address.
     */
    public static void removeLrLswGateway(Uuid tenantId, NodeId lrId, IpAddress gatewayIpAddr) {
        fabricMgrProvider.removeLrLswGateway(tenantId, lrId, gatewayIpAddr);
    }

    /**
     * To create an ACL.
     * @param tenantId : tenant identifier
     * @param nodeId : node ID
     * @param aclName : acl entry name
     */
    public static void createAcl(Uuid tenantId, NodeId nodeId, String aclName) {
        fabricMgrProvider.createAcl(tenantId, nodeId, aclName);
    }

    /**
     * To remove an ACL.
     * @param tenantId : tenant identifier
     * @param nodeId : node identifier
     * @param aclName : acl entry name
     */
    public static void removeAcl(Uuid tenantId, NodeId fabricId, NodeId nodeId, String aclName) {
        fabricMgrProvider.removeAcl(tenantId, fabricId, nodeId, aclName);
    }
}
