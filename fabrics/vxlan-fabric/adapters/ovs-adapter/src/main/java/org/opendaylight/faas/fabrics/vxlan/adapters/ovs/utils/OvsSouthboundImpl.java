package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils;

import java.math.BigInteger;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.MdsalUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OvsSouthbound;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbBridgeAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbNodeAugmentation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;

public class OvsSouthboundImpl implements OvsSouthbound {
    private static final Logger LOG = LoggerFactory.getLogger(OvsSouthboundImpl.class);
    private DataBroker databroker = null;
    private static final String PATCH_PORT_TYPE = "patch";
    private MdsalUtils mdsalUtils = null;

    /**
     * Class constructor setting the data broker.
     *
     * @param dataBroker the {@link org.opendaylight.controller.md.sal.binding.api.DataBroker}
     */
    public OvsSouthboundImpl(DataBroker dataBroker) {
        this.databroker = dataBroker;
        mdsalUtils = new MdsalUtils(dataBroker);
    }

    @Override
    public OvsdbBridgeAugmentation getBridge(Node node) {
        return node.getAugmentation(OvsdbBridgeAugmentation.class);
    }

    @Override
    public String getBridgeName(Node node) {
        String bridgeName = null;
        OvsdbBridgeAugmentation bridge = getBridge(node);
        if (bridge != null) {
            bridgeName = bridge.getBridgeName().getValue();
        }
        return bridgeName;
    }

    @Override
    public long getDataPathId(Node node) {
        long dpid = 0L;
        String datapathId = getDatapathId(node);
        if (datapathId != null) {
            dpid = new BigInteger(datapathId.replaceAll(":", ""), 16).longValue();
        }
        return dpid;
    }

    @Override
    public String getDatapathId(Node node) {
        String datapathId = null;
        OvsdbBridgeAugmentation ovsdbBridgeAugmentation = node.getAugmentation(OvsdbBridgeAugmentation.class);
        if (ovsdbBridgeAugmentation != null && ovsdbBridgeAugmentation.getDatapathId() != null) {
            datapathId = node.getAugmentation(OvsdbBridgeAugmentation.class).getDatapathId().getValue();
        }
        return datapathId;
    }
}
