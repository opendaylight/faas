/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers;

import java.util.Map;

import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBdIf;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OvsSouthboundUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricOptions.TrafficBehavior;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.Maps;

public class DeviceContext {

    private InstanceIdentifier<Node> myIId;

    private IpAddress vtep;

    private long vtep_ofPort;

    private long gpe_vtep_ofPort;

    private long dpid;

    private String bridgeName;

    private TrafficBehavior trafficBehavior = TrafficBehavior.Normal;

    // bdif id to AdapterBdIf map
    Map<String, AdapterBdIf> bdifCache = Maps.newHashMap();

    DeviceContext(Node node, InstanceIdentifier<Node> nodeIid) {
        myIId = nodeIid;

        vtep = OvsSouthboundUtils.getVtepIp(node);

        vtep_ofPort = 0l;

        gpe_vtep_ofPort = 0l;
        //vtep_ofPort = OvsSouthboundUtils.getVxlanTunnelOFPort(node);

        dpid = OvsSouthboundUtils.getDataPathId(node);

        bridgeName = OvsSouthboundUtils.getBridgeName(node);
    }

    public InstanceIdentifier<Node> getMyIId() {
        return myIId;
    }

    public void setMyIId(InstanceIdentifier<Node> myIId) {
        this.myIId = myIId;
    }

    public IpAddress getVtep() {
        return vtep;
    }

    public void setVtep(IpAddress vtep) {
        this.vtep = vtep;
    }

    public long getVtep_ofPort() {
        return vtep_ofPort;
    }

    public void setVtep_ofPort(long vtep_ofPort) {
        this.vtep_ofPort = vtep_ofPort;
    }

    public long getDpid() {
        return dpid;
    }

    public void setDpid(long dpid) {
        this.dpid = dpid;
    }

    public String getBridgeName() {
        return bridgeName;
    }

    public void setBridgeName(String bridgeName) {
        this.bridgeName = bridgeName;
    }

    public Map<String, AdapterBdIf> getBdifCache() {
        return bdifCache;
    }

    public void setBdifCache(Map<String, AdapterBdIf> bdifCache) {
        this.bdifCache = bdifCache;
    }

    public void addBdifToCache(AdapterBdIf adapterBdIf) {
        bdifCache.put(adapterBdIf.getId(), adapterBdIf);
    }

    public void deleteBdifFromCache(AdapterBdIf adapterBdIf) {
        bdifCache.remove(adapterBdIf.getId());
    }

    public long getGpe_vtep_ofPort() {
        return gpe_vtep_ofPort;
    }

    public void setGpe_vtep_ofPort(long gpe_vtep_ofPort) {
        this.gpe_vtep_ofPort = gpe_vtep_ofPort;
    }

    void setTrafficBehavior(TrafficBehavior newBehavior) {
    	this.trafficBehavior = newBehavior;
    }

    public TrafficBehavior getTrafficBehavior() {
        return trafficBehavior;
    }

}
