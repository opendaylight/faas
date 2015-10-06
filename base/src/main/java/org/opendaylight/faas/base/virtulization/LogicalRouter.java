/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.faas.base.data.PortConfig;
import org.opendaylight.faas.base.data.RouteTarget;
import org.opendaylight.faas.base.data.RoutingTableEntry;
import org.opendaylight.faas.base.data.VlanIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogicalRouter {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalRouter.class);
    private static String VPNINSTANCE_PREFIX = "NIM";
    private static int MAXIUM_VPN_NAME_LENGTH = 31;
    private Set<String> vrfLocationList_m; // VRF location can be either
                                           // centralized single
                                           // location, or distributed
                                           // multiple locations.
    private String routeDistinguisher_m;
    private List<RouteTarget> routeTargets;
    private Map<String, LogicalSwitch> lswList_m;
    private String name_m;
    private String id_m;
    private String tenantId_m;
    private String vdcId_m;
    private String systemId_m;
    private String vpnId_m;
    private Map<String, List<RoutingTableEntry>> routingTableStore_m; // one RT per VRF location.
    private Map<String, List<VlanIf>> L3TunnelEndStore_m; // one VLANIF list per VRF
                                                          // location.

    public LogicalRouter() {
        vrfLocationList_m = new HashSet<String>();
        routeTargets = new ArrayList<RouteTarget>();
        lswList_m = new HashMap<String, LogicalSwitch>();
        this.routingTableStore_m = new HashMap<String, List<RoutingTableEntry>>();
        this.L3TunnelEndStore_m = new HashMap<String, List<VlanIf>>();
    }

    public LogicalRouter(String lrName, String tenantId, String vdcId) {
        this();
        this.name_m = lrName;
        this.id_m = lrName;
        this.systemId_m = lrName;
        this.tenantId_m = tenantId;
        this.vdcId_m = vdcId;
        this.routeDistinguisher_m = "65000:100";

        /**
         * Get around the limitation of VPN instance name length
         * restriction. we need to figure out a way to make this
         * hack more graceful
         */
        this.vpnId_m = this.getVPNIdForVdcId(vdcId);
    }

    private String getVPNIdForVdcId(String tenantID) {
        String vpnid = tenantID;
        if (vpnid.length() > MAXIUM_VPN_NAME_LENGTH) {
            /**
             * 3 + 17 + "-" + <=10 . hashcode length is less than or equals to
             * 10. i.e. 2**32 = 4294967296
             */
            vpnid = vpnid.replace("-", "");
            if (vpnid.length() > MAXIUM_VPN_NAME_LENGTH) {
                vpnid = vpnid.substring(0, 16);
                vpnid = VPNINSTANCE_PREFIX + vpnid + "-" + String.valueOf(tenantID.hashCode());
            }
        }

        return vpnid;
    }

    public String getRouteDistinguisher() {
        return routeDistinguisher_m;
    }

    public void setRouteDistinguisher(String routeDistinguisher) {
        this.routeDistinguisher_m = routeDistinguisher;
    }

    public List<RouteTarget> getRouteTargets() {
        return routeTargets;
    }

    public void setRouteTargets(List<RouteTarget> routeTargets) {
        this.routeTargets = routeTargets;
    }

    public Set<String> getVrfLocationList() {
        return vrfLocationList_m;
    }

    public void setVrfLocationList(Set<String> locations) {
        this.vrfLocationList_m = locations;
    }

    public Map<String, LogicalSwitch> getLswList() {
        return lswList_m;
    }

    public Set<String> getLswIds() {
        if (this.lswList_m == null || this.lswList_m.isEmpty()) {
            return null;
        }

        return this.lswList_m.keySet();
    }

    public String getName() {
        return name_m;
    }

    public void setName(String name) {
        this.name_m = name;
    }

    public String getId() {
        return id_m;
    }

    public void setId(String id) {
        this.id_m = id;
    }

    public String getTenantId() {
        return tenantId_m;
    }

    public void setTenantId(String tenantId) {
        this.tenantId_m = tenantId;
    }

    public String getVdcId() {
        return vdcId_m;
    }

    public void setVdcId(String vdcId) {
        this.vdcId_m = vdcId;
    }

    public String getSystemId() {
        return systemId_m;
    }

    public void setSystemId(String systemId) {
        this.systemId_m = systemId;
    }

    public String getVpnId() {
        return vpnId_m;
    }

    public void setVpnId(String vpnId) {
        this.vpnId_m = vpnId;
    }

    public RoutingTableEntry addRoutingEntry(String nodeId, String destIpAddr, String destIpNetMask,
            String nextHopIpAddr) {
        if (nodeId == null || destIpAddr == null || destIpNetMask == null || nextHopIpAddr == null) {
            LOG.error("NETNODE: ERROR: addRoutingEntry(): input parameter is null");
            return null;
        }

        if (this.vrfLocationList_m.contains(nodeId) == false) {
            LOG.error("NETNODE: ERROR: addRoutingEntry(): cannot find VRF: nodeId={}", nodeId);
            return null;
        }

        List<RoutingTableEntry> routingEntries = this.routingTableStore_m.get(nodeId);
        if (routingEntries == null) {
            routingEntries = new ArrayList<RoutingTableEntry>();
            this.routingTableStore_m.put(nodeId, routingEntries);
        }

        RoutingTableEntry entry = new RoutingTableEntry(this.vpnId_m, destIpAddr, destIpNetMask, nextHopIpAddr);
        routingEntries.add(entry);

        return entry;
    }

    public Map<String, List<RoutingTableEntry>> getRoutingTableStore() {
        return routingTableStore_m;
    }

    public VlanIf addLinkTerminator(PortConfig port, String vlanIfIpAddr, String vlanIfIpMask, L2Resource l2Label) {
        if (port == null || vlanIfIpAddr == null || vlanIfIpMask == null || l2Label == null) {
            LOG.error("NETNODE: ERROR: addLinkTerminator(): input parameter is null");
            return null;
        }

        String nodeId = port.getNetworkElementID();

        if (this.vrfLocationList_m.contains(nodeId) == false) {
            LOG.error("NETNODE: ERROR: addLinkTerminator(): cannot find VRF: nodeId={}", nodeId);
            return null;
        }

        List<VlanIf> tunnelEndList = this.L3TunnelEndStore_m.get(nodeId);
        if (tunnelEndList == null) {
            tunnelEndList = new ArrayList<VlanIf>();
            this.L3TunnelEndStore_m.put(nodeId, tunnelEndList);
        }

        VlanIf vlanif = new VlanIf(nodeId, port.getPortID(), l2Label.getResourceId(), vlanIfIpAddr, vlanIfIpMask,
                this.vpnId_m);
        tunnelEndList.add(vlanif);

        return vlanif;
    }

    public Map<String, List<VlanIf>> getL3TunnelEndStore() {
        return L3TunnelEndStore_m;
    }

    public void setL3TunnelEndStore(Map<String, List<VlanIf>> l3TunnelEndStore) {
        L3TunnelEndStore_m = l3TunnelEndStore;
    }

    public void addLogicalSwitch(String lswId, LogicalSwitch lsw) {
        if (lswId == null) {
            LOG.error("NETNODE: ERROR: addLogicalSwitch(): lswId is null");
            return;
        }

        if (lsw == null) {
            LOG.error("NETNODE: ERROR: addLogicalSwitch(): lsw is null");
            return;
        }

        if (this.lswList_m.containsKey(lswId) == false) {
            this.lswList_m.put(lswId, lsw);
        } else {
            LOG.error("NETNODE: ERROR: addLogicalSwitch(): lsw already exists: lswId={}", lswId);
            return;
        }
    }
}
