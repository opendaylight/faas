/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;


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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Bdif;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdifBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdifKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DeviceContext {

    private final DataBroker databroker;

    private final InstanceIdentifier<Node> deviceIId;

    Set<String> localBD = Sets.newHashSet();

    Map<Integer, GatewayPort> bdifs = Maps.newHashMap();

    private final ExecutorService executor;

    DeviceContext(DataBroker databroker, IpAddress vtep, InstanceIdentifier<Node> deviceIId, ExecutorService executor) {
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

        LogicRouterContext vrfctx = switchCtx.getVrfCtx();
        if (vrfctx != null) {
            createBDif(vlan, vrfctx);
        }
    }

    public void removeBridgeDomain(LogicSwitchContext switchCtx) {
        int vlan = switchCtx.getVlan();
        localBD.remove(createBdId(vlan));

        LogicRouterContext vrfctx = switchCtx.getVrfCtx();
        if (vrfctx != null) {
            GatewayPort gw = vrfctx.getGatewayPortByVlan(vlan);
            if (gw != null) {
                // FIXME
            }
        }
    }

    void createBDif(int vlan, LogicRouterContext vrfctx) {
        GatewayPort gw = vrfctx.getGatewayPortByVlan(vlan);

        bdifs.put(vlan, gw);
        syncToDom(gw, false);
    }

    void removeBDif(long vni, GatewayPort gw) {
        bdifs.remove(vni);

        syncToDom(gw, true);
    }

    private void syncToDom(GatewayPort gw, boolean delete) {
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
        } else {
            trans.put(LogicalDatastoreType.OPERATIONAL, bdifIId, builder.build());
        }
        MdSalUtils.wrapperSubmit(trans, executor);
    }

    private String createBdId(long vni) {
        return String.format("bd:%d", vni);
    }

    private String createBdIfId(long vni) {
        return String.format("bdif:%d", vni);
    }
}
