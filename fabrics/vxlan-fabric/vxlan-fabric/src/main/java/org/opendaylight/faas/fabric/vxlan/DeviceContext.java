/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.AccessType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.port.functions.PortFunction;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DeviceContext {

    private final DataBroker databroker;

    private final IpAddress vtep;

    private final InstanceIdentifier<Node> deviceIId;

    Set<String> localBD = Sets.newHashSet();

    Map<Long, GatewayPort> bdifs = Maps.newHashMap();

    private final ExecutorService executor;

    DeviceContext(DataBroker databroker, IpAddress vtep, InstanceIdentifier<Node> deviceIId, ExecutorService executor) {
        this.databroker = databroker;
        this.vtep = vtep;
        this.deviceIId = deviceIId;
        this.executor = executor;
    }

    public IpAddress getVtep() {
        return vtep;
    }

    public DeviceKey getKey() {
        return DeviceKey.newInstance(deviceIId);
    }

    public void createBridgeDomain(LogicSwitchContext switchCtx) {
        long vni = switchCtx.getVni();
        localBD.add(createBdId(vni));
        syncToDom(vni, false);

        LogicRouterContext vrfctx = switchCtx.getVrfCtx();
        if (vrfctx != null) {
            createBdif(vni, vrfctx);
        }
    }

    public void removeBridgeDomain(LogicSwitchContext switchCtx) {
        long vni = switchCtx.getVni();
        localBD.remove(createBdId(vni));
        syncToDom(vni, true);

        LogicRouterContext vrfctx = switchCtx.getVrfCtx();
        if (vrfctx != null) {
            GatewayPort gw = vrfctx.getGatewayPortByVni(vni);
            if (gw != null) {
                removeBdif(vni, gw);
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

    void createBdif(long vni, LogicRouterContext vrfctx) {
        GatewayPort gw = vrfctx.getGatewayPortByVni(vni);

        bdifs.put(vni, gw);
        syncToDom(gw, false);
    }

    void addFunction2Bdif(long vni, PortFunction function) {
        String bdifid = createBdIfId(vni);
        InstanceIdentifier<Bdif> bdifIId = deviceIId.augmentation(FabricCapableDevice.class)
                        .child(Config.class).child(Bdif.class, new BdifKey(bdifid));

        BdifBuilder builder = new BdifBuilder().setKey(new BdifKey(bdifid)).setPortFunction(function);
        WriteTransaction wt = databroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, bdifIId, builder.build());

        MdSalUtils.wrapperSubmit(wt, executor);
    }

    void removeBdif(long vni, GatewayPort gw) {
        bdifs.remove(vni);

        syncToDom(gw, true);
    }

    private void syncToDom(long vni, boolean delete) {
        String bdid = createBdId(vni);
        InstanceIdentifier<BridgeDomain> bdIId = deviceIId.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(BridgeDomain.class, new BridgeDomainKey(bdid));

        BridgeDomainBuilder builder = new BridgeDomainBuilder();
        builder.setId(bdid);
        BridgeDomain1Builder augBuilder = new BridgeDomain1Builder();
        augBuilder.setVni(vni);
        builder.addAugmentation(BridgeDomain1.class, augBuilder.build());
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
        String bdifid = createBdIfId(gw.getVni());
        InstanceIdentifier<Bdif> bdifIId = deviceIId.augmentation(FabricCapableDevice.class)
                        .child(Config.class).child(Bdif.class, new BdifKey(bdifid));

        BdifBuilder builder = new BdifBuilder();
        builder.setBdid(createBdId(gw.getVni()));
        builder.setId(bdifid);
        builder.setKey(new BdifKey(bdifid));
        builder.setIpAddress(IpAddressUtils.getIpAddress(gw.getIp()));
        builder.setMacAddress(gw.getMac());
        builder.setMask(IpAddressUtils.getMask(gw.getIp()));
        builder.setVrf(gw.getVrf().intValue());

        WriteTransaction trans = databroker.newWriteOnlyTransaction();
        if (delete) {
            trans.delete(LogicalDatastoreType.OPERATIONAL, bdifIId);
        } else {
            trans.put(LogicalDatastoreType.OPERATIONAL, bdifIId, builder.build());
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
}
