/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomainKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.VtepAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbBridgeAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbTerminationPointAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbTerminationPointAugmentationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.port._interface.attributes.Options;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.port._interface.attributes.OptionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.port._interface.attributes.OptionsKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class OvsSouthboundUtils {
    private static final Logger LOG = LoggerFactory.getLogger(OvsSouthboundUtils.class);
    //private MdsalUtils mdsalUtils = null;
    //private DataBroker databroker = null;

//    public OvsSouthboundUtils(DataBroker dataBroker) {
//        //mdsalUtils = new MdsalUtils(dataBroker);
//        this.databroker = dataBroker;
//    }

    public static OvsdbBridgeAugmentation getBridge(Node node) {
        return node.getAugmentation(OvsdbBridgeAugmentation.class);
    }

    public static Node getOvsdbBridgeNode(InstanceIdentifier<Node> nodeIid, DataBroker databroker) {
        Node node = MdsalUtils.read(LogicalDatastoreType.OPERATIONAL, nodeIid, databroker);
        OvsdbBridgeAugmentation bridge = getBridge(node);
        if (bridge != null) {
            return node;
        }
        return null;
    }

    public static String getBridgeName(Node node) {
        String bridgeName = null;
        OvsdbBridgeAugmentation bridge = getBridge(node);
        if (bridge != null) {
            bridgeName = bridge.getBridgeName().getValue();
        }
        return bridgeName;
    }

    public static long getDataPathId(Node node) {
        long dpid = 0L;
        String datapathId = getDatapathId(node);
        if (datapathId != null) {
            dpid = new BigInteger(datapathId.replaceAll(":", ""), 16).longValue();
        }
        return dpid;
    }

    public static String getDatapathId(Node node) {
        String datapathId = null;
        OvsdbBridgeAugmentation ovsdbBridgeAugmentation = node.getAugmentation(OvsdbBridgeAugmentation.class);
        if (ovsdbBridgeAugmentation != null && ovsdbBridgeAugmentation.getDatapathId() != null) {
            datapathId = node.getAugmentation(OvsdbBridgeAugmentation.class).getDatapathId().getValue();
        }
        return datapathId;
    }

    private static String generateTpName(String bridgeName, String tunnelType) {
    	return generateTpName(bridgeName, tunnelType, "");
    }

    private static String generateTpName(String bridgeName, String tunnelType, String prefix) {
    	return String.format("%s%s-%s", tunnelType, prefix, bridgeName);
    }

    private static List<OvsdbTerminationPointAugmentation> extractTerminationPointAugmentations( Node node ) {
        List<OvsdbTerminationPointAugmentation> tpAugmentations = new ArrayList<OvsdbTerminationPointAugmentation>();
        List<TerminationPoint> terminationPoints = node.getTerminationPoint();
        if(terminationPoints != null && !terminationPoints.isEmpty()){
            for(TerminationPoint tp : terminationPoints){
                OvsdbTerminationPointAugmentation ovsdbTerminationPointAugmentation =
                        tp.getAugmentation(OvsdbTerminationPointAugmentation.class);
                if (ovsdbTerminationPointAugmentation != null) {
                    tpAugmentations.add(ovsdbTerminationPointAugmentation);
                }
            }
        }
        return tpAugmentations;
    }

    private static OvsdbTerminationPointAugmentation extractTerminationPointAugmentation(Node bridgeNode, String portName) {
        if (bridgeNode.getAugmentation(OvsdbBridgeAugmentation.class) != null) {
            List<OvsdbTerminationPointAugmentation> tpAugmentations = extractTerminationPointAugmentations(bridgeNode);
            for (OvsdbTerminationPointAugmentation ovsdbTerminationPointAugmentation : tpAugmentations) {
                if (ovsdbTerminationPointAugmentation.getName().equals(portName)) {
                    return ovsdbTerminationPointAugmentation;
                }
            }
        }
        return null;
    }

    private static TerminationPoint readTerminationPoint(Node bridgeNode, String bridgeName, String portName, DataBroker databroker) {
        InstanceIdentifier<TerminationPoint> tpIid = MdsalUtils.createTerminationPointInstanceIdentifier(
                bridgeNode, portName);
        return MdsalUtils.read(LogicalDatastoreType.OPERATIONAL, tpIid, databroker);
    }

    private static Boolean isTunnelTerminationPointExist(Node bridgeNode, String bridgeName, String portName, DataBroker databroker){
        return readTerminationPoint(bridgeNode, bridgeName, portName, databroker) != null;
    }

    private static Boolean addTerminationPoint(Node bridgeNode, String bridgeName, String portName,
            String type, Map<String, String> options, DataBroker databroker) {
        InstanceIdentifier<TerminationPoint> tpIid = MdsalUtils.createTerminationPointInstanceIdentifier(
                bridgeNode, portName);
        OvsdbTerminationPointAugmentationBuilder tpAugmentationBuilder = new OvsdbTerminationPointAugmentationBuilder();

        tpAugmentationBuilder.setName(portName);
        if (type != null) {
            tpAugmentationBuilder.setInterfaceType(MdsalUtils.OVSDB_INTERFACE_TYPE_MAP.get(type));
        }

        List<Options> optionsList = new ArrayList<Options>();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            OptionsBuilder optionsBuilder = new OptionsBuilder();
            optionsBuilder.setKey(new OptionsKey(entry.getKey()));
            optionsBuilder.setOption(entry.getKey());
            optionsBuilder.setValue(entry.getValue());
            optionsList.add(optionsBuilder.build());
        }
        tpAugmentationBuilder.setOptions(optionsList);

        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setKey(InstanceIdentifier.keyOf(tpIid));
        tpBuilder.addAugmentation(OvsdbTerminationPointAugmentation.class, tpAugmentationBuilder.build());
        return MdsalUtils.put(LogicalDatastoreType.CONFIGURATION, tpIid, tpBuilder.build(), databroker);
    }

    private static Boolean addTunnelTerminationPoint(Node bridgeNode, String bridgeName, String portName, String type,
            Map<String, String> options, DataBroker databroker) {
        return addTerminationPoint(bridgeNode, bridgeName, portName, type, options, databroker);
    }

    //Add Vxlan Tunnel port

    public static boolean addVxlanTunnelPort (Node node, DataBroker databroker) {
        //yzy: read tunnelBridgeName from where??
        //String tunnelBridgeName = getIntegrationBridgeName();
        String tunnelBridgeName = getBridgeName(node);
        String tunnelType = "vxlan";
        String portName = generateTpName(tunnelBridgeName, tunnelType);
        LOG.info("addTunnelPort enter: portName: {}", portName);
        if (extractTerminationPointAugmentation(node, portName) != null
                || isTunnelTerminationPointExist(node, tunnelBridgeName, portName, databroker)) {
            LOG.info("Tunnel {} is present in {} of {}", portName, tunnelBridgeName, node.getNodeId().getValue());
            return true;
        }

        Map<String, String> options = Maps.newHashMap();
        options.put("local_ip", String.copyValueOf(getVtepIp(node).getValue()));
        options.put("key", "flow");
        options.put("remote_ip", "flow");
        options.put("dst_port", "4789");

        if (!addTunnelTerminationPoint(node, tunnelBridgeName, portName, tunnelType, options, databroker)) {
            LOG.error("Failed to insert Tunnel port {} in {}", portName, tunnelBridgeName);
            return false;
        }

        LOG.info("addTunnelPort exit: portName: {}", portName);
        return true;
    }

    public static boolean addVxlanGpeTunnelPort(Node node, DataBroker databroker) {
        String tunnelBridgeName = getBridgeName(node);
        String tunnelType = "vxlan";
        String portName = generateTpName(tunnelBridgeName, tunnelType, "gpe");
        LOG.info("addTunnelPort enter: portName: {}", portName);
        if (extractTerminationPointAugmentation(node, portName) != null
                || isTunnelTerminationPointExist(node, tunnelBridgeName, portName, databroker)) {
            LOG.info("Tunnel {} is present in {} of {}", portName, tunnelBridgeName, node.getNodeId().getValue());
            return true;
        }

        Map<String, String> options = Maps.newHashMap();
        options.put("local_ip", String.copyValueOf(getVtepIp(node).getValue()));
        options.put("key", "flow");
        options.put("remote_ip", "flow");
        options.put("nsi", "flow");
        options.put("nsp", "flow");
        options.put("nshc1", "flow");
        options.put("nshc2", "flow");
        options.put("nshc3", "flow");
        options.put("nshc4", "flow");
        options.put("dst_port", "6633");

        if (!addTunnelTerminationPoint(node, tunnelBridgeName, portName, tunnelType, options, databroker)) {
            LOG.error("Failed to insert Tunnel port {} in {}", portName, tunnelBridgeName);
            return false;
        }

        LOG.info("addTunnelPort exit: portName: {}", portName);
        return true;
    }

    public static IpAddress getVtepIp (Node bridgeNode) {
        return bridgeNode.getAugmentation(FabricCapableDevice.class).getAttributes()
        .getAugmentation(VtepAttribute.class).getVtep().getIp();
    }

    private static Boolean deleteTerminationPoint(Node bridgeNode, String portName, DataBroker databroker) {
        InstanceIdentifier<TerminationPoint> tpIid =
                MdsalUtils.createTerminationPointInstanceIdentifier(bridgeNode, portName);
        return MdsalUtils.delete(LogicalDatastoreType.CONFIGURATION, tpIid, databroker);
    }

    /* delete port from ovsdb port table */
    private static boolean deletePort(Node node, String bridgeName, String portName, DataBroker databroker) {
        return deleteTerminationPoint(node, portName, databroker);
    }

    public static boolean deleteVxlanTunnelPort(Node node, DataBroker databroker) {
        String tunnelType = "vxlan";
        String tunnelBridgeName = getBridgeName(node);
        String portName = generateTpName(tunnelBridgeName, tunnelType);
        return deletePort(node, tunnelBridgeName, portName, databroker);
    }

    public static boolean deleteVxlanGpeTunnelPort(Node node, DataBroker databroker) {
        String tunnelType = "vxlan";
        String tunnelBridgeName = getBridgeName(node);
        String portName = generateTpName(tunnelBridgeName, tunnelType, "gpe");
        return deletePort(node, tunnelBridgeName, portName, databroker);
    }

    private static List<TerminationPoint> extractTerminationPoints(Node node) {
        List<TerminationPoint> terminationPoints = new ArrayList<TerminationPoint>();
        OvsdbBridgeAugmentation ovsdbBridgeAugmentation = node.getAugmentation(OvsdbBridgeAugmentation.class);
        if (ovsdbBridgeAugmentation != null) {
            terminationPoints.addAll(node.getTerminationPoint());
        }
        return terminationPoints;
    }

    public static Long getOfPort(Node node, TpId tpid) {
        List<TerminationPoint> terminationPoints = extractTerminationPoints(node);
        Long ofPort = 0l;

        for (TerminationPoint terminationPoint : terminationPoints) {
            if (terminationPoint.getTpId() == tpid) {
                ofPort = terminationPoint.getAugmentation(OvsdbTerminationPointAugmentation.class).getOfport();
                return ofPort;
            }
        }

        return ofPort;
    }

    public static Long getOFPort(OvsdbTerminationPointAugmentation port) {
        Long ofPort = 0L;
        if (port.getOfport() != null) {
            ofPort = port.getOfport();
        }
        return ofPort;
    }

    public static Long getVxlanTunnelOFPort(InstanceIdentifier<Node> nodeIid, String tunnelBridgeName, DataBroker databroker) {
        Long ofPort = 0L;

        String tunnelType = "vxlan";
        String portName = generateTpName(tunnelBridgeName, tunnelType);

        ofPort = getOfPort(nodeIid, new TpId(portName), databroker);

        return ofPort;
    }

    public static Long getVxlanGpeTunnelOFPort(InstanceIdentifier<Node> nodeIid, String tunnelBridgeName, DataBroker databroker) {
        Long ofPort = 0L;

        String tunnelType = "vxlan";
        String portName = generateTpName(tunnelBridgeName, tunnelType, "gpe");

        ofPort = getOfPort(nodeIid, new TpId(portName), databroker);

        return ofPort;
    }

    public static String getTunnelIp(Node node) {
        String tunnelIp = null;
        FabricCapableDevice fabricCapableDevice = node.getAugmentation(FabricCapableDevice.class);

        if (fabricCapableDevice != null) {
            VtepAttribute vtepAttribute = fabricCapableDevice.getAttributes().getAugmentation(VtepAttribute.class);
            if (vtepAttribute != null) {
                tunnelIp = vtepAttribute.getVtep().getIp().getIpv4Address().getValue();
            }
        }

        return tunnelIp;
    }

    public static Long getOfPort(InstanceIdentifier<Node> nodeIid, TpId tpid, DataBroker databroker) {
        Long ofPort = 0l;
        InstanceIdentifier<TerminationPoint> tpIid = nodeIid.child(TerminationPoint.class, new TerminationPointKey(tpid));
        TerminationPoint teminationPoint = MdsalUtils.read(LogicalDatastoreType.OPERATIONAL, tpIid, databroker);
        if (teminationPoint != null) {
            OvsdbTerminationPointAugmentation port = teminationPoint.getAugmentation(OvsdbTerminationPointAugmentation.class);

            if (port != null) {
                ofPort = port.getOfport();
            }
        }
        return ofPort;
    }

    public static Long getBridgeDomainVni(InstanceIdentifier<Node> nodeIid, String bridgeDomainId, DataBroker databroker) {
        InstanceIdentifier<BridgeDomain> bridgeDomainIid =
                nodeIid.augmentation(FabricCapableDevice.class).child(Config.class).child(BridgeDomain.class, new BridgeDomainKey(bridgeDomainId));

        BridgeDomain bridgeDomain = MdsalUtils.read(LogicalDatastoreType.OPERATIONAL, bridgeDomainIid, databroker);

        Long segmentationId = bridgeDomain.getAugmentation(BridgeDomain1.class).getVni();

        return segmentationId;
    }
}
