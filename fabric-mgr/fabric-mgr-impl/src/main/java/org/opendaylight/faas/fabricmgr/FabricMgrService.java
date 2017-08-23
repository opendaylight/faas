/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import java.util.Map;
import java.util.concurrent.Future;
import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.faas.uln.cache.LogicalRouterMappingInfo;
import org.opendaylight.faas.uln.cache.RenderedRouter;
import org.opendaylight.faas.uln.cache.UserLogicalNetworkCache;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.VcLneId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmLneLayer3InputBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Provides fabric resource management functions.
 *
 * @author Thomas Pantelis
 */
public interface FabricMgrService {

    /**
     * createLneLayer2 - create a layer 2 logical network device. i.e. logical switch.
     *
     * @param tenantId - tenant identifier
     * @param fabricId - fabric identifier
     * @param lsw - the user level logical switch which may contains multiple rendered logical switches.
     * @param uln - the user logical network info
     * @return the rendered logical switch ID on the given fabric.
     */
    NodeId createLneLayer2(Uuid tenantId, NodeId fabricId, Uuid lsw, UserLogicalNetworkCache uln);

    /**
     * To create a logical router on a given fabric for a tenant.
     * @param tenantId - tenant identifier.
     * @param fabricId - fabric identifier.
     * @param uln - the user logical network info
     * @return the rendered logical router identifier on the given fabric.
     */
    NodeId createLneLayer3(Uuid tenantId, String fabricId, UserLogicalNetworkCache uln);

    NodeId setupExternalGW(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouterMappingInfo lr, IpAddress gatewayIP,
            IpPrefix prefix, int tag);

    /**
     *
     * Connects all distributed logical routers into one big logical router
     * all the routes or host routes or prefix needs to be correctly configured on each
     * logical router on each participant fabric
     *
     *
     * @param tenantID - tenant identifier.
     * @param uln - User logical network info.
     * @param rmaps - RenderedRouter on Fabric mapping table
     */

    void connectAllDVRs(Uuid tenantID, UserLogicalNetworkCache uln, Map<String, RenderedRouter> rmaps);

    void updateRoutes(Uuid tenantID, UserLogicalNetworkCache uln, Map<String, RenderedRouter> maps);

    TpId createLogicalPortOnLneLayer2(Uuid tenantId, NodeId vfabricId, NodeId lswId);

    Uuid createLrLswGateway(Uuid tenantId, NodeId vfabricId, NodeId lrId, NodeId lswId, IpAddress gatewayIpAddr,
            IpPrefix ipPrefix);

    void createAcl(Uuid tenantId, String fabricId, NodeId nodeId, String aclName);

    void setIPMapping(Uuid tenantId, NodeId fabricId, NodeId lrId, TpId tpid, IpAddress pubIP, IpAddress priip);

    Uuid attachEpToLneLayer2(Uuid tenantId, NodeId fabricId, NodeId lswId, TpId lswLogicalPortId, EndpointAttachInfo endpoint);

    void removeAcl(Uuid tenantId, String fabricId, NodeId nodeId, String aclName);

    void removeLrLswGateway(Uuid tenantId, String fabricId, NodeId lrId, IpAddress gatewayIpAddr);

    void removeLneLayer2(Uuid tenantId, String fabricId, NodeId lswId);

    void removeLneLayer3(Uuid tenantId, String fabricId, NodeId lrId);

    void unregisterEpFromLneLayer2(Uuid tenantId, NodeId fabricId, NodeId lswId, Uuid epUuid);

}
