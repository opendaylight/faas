/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabric.vxlan.adapter.ce;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricOptions.TrafficBehavior;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.Maps;

public class CEDeviceContext {

    private InstanceIdentifier<Node> myIId;

    private String bridgeName;

    private IpAddress vtep;

    private Long vtepOFPort;

    private TrafficBehavior trafficBehavior = TrafficBehavior.Normal;

    private boolean aclRedirectCapability = false;

    // bdif id to AdapterBdIf map
    Map<String, AdapterBdIf> bdifCache = Maps.newHashMap();

    //Vrf to list of BDs
    Map<Integer, List<Long>> vrfBdCache = Maps.newHashMap();

    CEDeviceContext(Node node, InstanceIdentifier<Node> nodeIid) {
        myIId = nodeIid;
        vtep = Utility.getVtepIp(node);
        vtepOFPort = null;
        bridgeName = Utility.getBridgeName(node);
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

    public Long getVtep_ofPort() {
        return vtepOFPort;
    }

    public void setVtep_ofPort(Long vtep_ofPort) {
        this.vtepOFPort = vtep_ofPort;
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

    public synchronized void addBdifToCache(AdapterBdIf adapterBdIf) {
        bdifCache.put(adapterBdIf.getId(), adapterBdIf);
    }

    public boolean isVrfExists(Integer id) {
        return this.vrfBdCache.containsKey(id);
    }

    public synchronized void deleteBdifFromCache(AdapterBdIf adapterBdIf) {
        bdifCache.remove(adapterBdIf.getId());
    }

    public synchronized void addBdifToVrfCache(Integer vrfId, Long bdVNIId) {
        if (isVrfExists(vrfId)) {
            vrfBdCache.get(vrfId).add(bdVNIId);
        }
        else {
            List<Long> l = new ArrayList<>();
            l.add(bdVNIId);
            vrfBdCache.put(vrfId, l);
        }

    }

    public synchronized void removeBdifFromVrfCache(Integer vrfId, Long bdVNIId) {
        if (isVrfExists(vrfId)) {
            vrfBdCache.get(vrfId).remove(bdVNIId);
            if (vrfBdCache.get(vrfId).isEmpty()) {
                vrfBdCache.remove(vrfId);
            }
        }
    }

    void setTrafficBehavior(TrafficBehavior newBehavior) {
        this.trafficBehavior = newBehavior;
    }

    public TrafficBehavior getTrafficBehavior() {
        return trafficBehavior;
    }

    public boolean isAclRedirectCapability() {
        return aclRedirectCapability;
    }

    public void setAclRedirectCapability(boolean aclRedirectCapability) {
        this.aclRedirectCapability = aclRedirectCapability;
    }

}
