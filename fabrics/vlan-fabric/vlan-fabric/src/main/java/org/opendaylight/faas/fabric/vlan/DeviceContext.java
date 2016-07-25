/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabric.utils.IpAddressUtils;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdPortKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Bdif;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdifBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdifKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomainKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Vrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.VrfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.VrfKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vlan.rev160615.VlanVrfRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.AccessType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DeviceContext {

    private final DataBroker databroker;

    private final InstanceIdentifier<Node> deviceIId;

    Set<String> localBD = Sets.newHashSet();

    Map<Integer, GatewayPort> bdifs = Maps.newHashMap();
    Map<Integer, Integer> vrfs = Maps.newHashMap();

    private final ExecutorService executor;

    DeviceContext(DataBroker databroker, String mgntIp, InstanceIdentifier<Node> deviceIId, ExecutorService executor) {
        this.databroker = databroker;
        this.deviceIId = deviceIId;
        this.executor = executor;
    }

    public DeviceKey getKey() {
        return DeviceKey.newInstance(deviceIId);
    }

    public void createBridgeDomain(LogicSwitchContext switchCtx) {
        int vlan = switchCtx.getVlan();
        localBD.add(createBdId(vlan));
        syncToDom(vlan, false);

        LogicRouterContext vrfctx = switchCtx.getVrfCtx();
        if (vrfctx != null) {
            createBdif(vlan, vrfctx);
        }
    }

    public void removeBridgeDomain(LogicSwitchContext switchCtx) {
        int vlan = switchCtx.getVlan();
        localBD.remove(createBdId(vlan));
        this.syncToDom(vlan, true);

        LogicRouterContext vrfctx = switchCtx.getVrfCtx();
        if (vrfctx != null) {
            GatewayPort gw = vrfctx.getGatewayPortByVlan(vlan);
            if (gw != null) {
                // FIXME
            }
        }
    }

    /**
     * Create a new Bridge Domain Port.
     * @param vni vni
     * @param tpid physical termination point
     * @param accessType access type
     * @param seg access segment
     * @return String name of Bridge Domain Port
     */
    public String createBdPort(long vni, TpId tpid, AccessType accessType, long seg) {
        String bdPortId = createBdPortId(tpid,  seg);

        BdPortBuilder builder = new BdPortBuilder();
        builder.setBdPortId(bdPortId);
        builder.setAccessType(accessType);
        builder.setAccessTag(seg);
        builder.setRefTpId(tpid);
        builder.setBdid(this.createBdId(vni));

        WriteTransaction trans = databroker.newWriteOnlyTransaction();
        InstanceIdentifier<BdPort> bdIId = deviceIId.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(BdPort.class, new BdPortKey(bdPortId));
        trans.put(LogicalDatastoreType.OPERATIONAL, bdIId, builder.build(), true);
        MdSalUtils.wrapperSubmit(trans, executor);

        return bdPortId;
    }

    void createBdif(int vlan, LogicRouterContext vrfctx) {
        GatewayPort gw = vrfctx.getGatewayPortByVlan(vlan);

        bdifs.put(vlan, gw);
        syncToDom(gw, false);
    }

    void removeBdif(long vni, GatewayPort gw) {
        bdifs.remove(vni);

        syncToDom(gw, true);
    }

   public InstanceIdentifier<VlanVrfRoute> createVrfRouteIId(int vrf) {
       return deviceIId
               .augmentation(FabricCapableDevice.class)
               .child(Config.class)
               .child(Vrf.class, new VrfKey(createVrfId(vrf)))
               .augmentation(VlanVrfRoute.class);

   }

   private void syncToDom(int vlan, boolean delete) {
        String bdid = createBdId(vlan);
        InstanceIdentifier<BridgeDomain> bdIId = deviceIId.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(BridgeDomain.class, new BridgeDomainKey(bdid));

        BridgeDomainBuilder builder = new BridgeDomainBuilder();
        builder.setId(bdid);
        builder.setSegment((long) vlan);
        builder.setKey(new BridgeDomainKey(bdid));

        WriteTransaction trans = databroker.newWriteOnlyTransaction();
        if (delete) {
            trans.delete(LogicalDatastoreType.OPERATIONAL, bdIId);
        } else {
            trans.put(LogicalDatastoreType.OPERATIONAL, bdIId, builder.build());
        }
        MdSalUtils.wrapperSubmit(trans, executor);

    }

    private void syncToDom(GatewayPort gw, boolean delete) {
        Integer vrf = gw.getVrf();

        String bdifid = createBdIfId(gw.getVlan());
        InstanceIdentifier<Bdif> bdifIId = deviceIId.augmentation(FabricCapableDevice.class)
                        .child(Config.class).child(Bdif.class, new BdifKey(bdifid));

        BdifBuilder builder = new BdifBuilder();
        builder.setBdid(createBdId(gw.getVlan()));
        builder.setId(bdifid);
        builder.setKey(new BdifKey(bdifid));
        builder.setIpAddress(IpAddressUtils.getIpAddress(gw.getIp()));
        builder.setMask(IpAddressUtils.getMask(gw.getIp()));
        builder.setVrf(gw.getVrf().intValue());

        WriteTransaction trans = databroker.newWriteOnlyTransaction();
        if (delete) {
            trans.delete(LogicalDatastoreType.OPERATIONAL, bdifIId);

            int gwCnt = vrfs.get(vrf) - 1;
            if (gwCnt == 0) {
                vrfs.remove(vrf);
                String vrfId = createVrfId(gw.getVrf());
                InstanceIdentifier<Vrf> vrfIId = deviceIId.augmentation(FabricCapableDevice.class)
                        .child(Config.class).child(Vrf.class, new VrfKey(vrfId));

                trans.delete(LogicalDatastoreType.OPERATIONAL, vrfIId);
            } else {
                vrfs.put(vrf, gwCnt);
            }
        } else {
            trans.put(LogicalDatastoreType.OPERATIONAL, bdifIId, builder.build());

            if (!vrfs.containsKey(gw.getVrf())) {
                String vrfId = createVrfId(gw.getVrf());
                InstanceIdentifier<Vrf> vrfIId = deviceIId.augmentation(FabricCapableDevice.class)
                        .child(Config.class).child(Vrf.class, new VrfKey(vrfId));

                VrfBuilder vrfBuilder = new VrfBuilder().setId(vrfId).setName(vrfId).setVrfCtx(gw.getVrf());
                trans.put(LogicalDatastoreType.OPERATIONAL, vrfIId, vrfBuilder.build());

                vrfs.put(gw.getVrf(), 1);
            } else {
                int gwCnt = vrfs.get(gw.getVrf()) + 1;
                vrfs.put(gw.getVrf(), gwCnt);
            }
        }

        MdSalUtils.wrapperSubmit(trans, executor);
    }

    private String createBdId(long vni) {
        return String.format("bd:%d", vni);
    }

    private String createBdPortId(TpId tpid, long segment) {
        return String.format("tp:%s - seg:%d", tpid.getValue(), segment);
    }

    private String createBdIfId(long vni) {
        return String.format("bdif:%d", vni);
    }

    private String createVrfId(int vrfCtx) {
        return String.format("vrf:%d", vrfCtx);
    }

}
