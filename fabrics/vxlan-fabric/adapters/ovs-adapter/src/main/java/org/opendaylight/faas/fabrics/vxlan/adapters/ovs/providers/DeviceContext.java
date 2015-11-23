package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers;

import java.util.Map;

import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBdIf;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBridgeDomain;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OvsSouthboundUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.Maps;

public class DeviceContext {

    private InstanceIdentifier<Node> myIId;

    private IpAddress vtep;

    private long vtep_ofPort;

    private long dpid;

    private String bridgeName;

    // bdif id to AdapterBdIf map
    Map<String, AdapterBdIf> bdifCache = Maps.newHashMap();

    DeviceContext(Node node, InstanceIdentifier<Node> nodeIid) {
        myIId = nodeIid;

        vtep = OvsSouthboundUtils.getVtepIp(node);

        vtep_ofPort = 0l;
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


}
