/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabric.general;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.Endpoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.fabric.impl.rev150930.FabricsSetting;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public abstract class AbstractDataStoreManager extends AbstractDataBrokerTest {

    protected DataBroker dataBroker;

    protected void setOdlDataBroker() {
        dataBroker = getDataBroker();
    }

    @Override
    protected Iterable<YangModuleInfo> getModuleInfos() throws Exception {
        Builder<YangModuleInfo> moduleInfoSet = ImmutableSet.<YangModuleInfo>builder();

        //loadModuleInfos(Topology.class, moduleInfoSet);
        loadModuleInfos(FabricsSetting.class, moduleInfoSet);
        loadModuleInfos(FabricCapableDevice.class, moduleInfoSet);
        loadModuleInfos(Endpoints.class, moduleInfoSet);
        loadModuleInfos(FabricNode.class, moduleInfoSet);
        loadModuleInfos(LswAttribute.class, moduleInfoSet);
        loadModuleInfos(FabricAcl.class, moduleInfoSet);

        return moduleInfoSet.build();
    }

    public static void loadModuleInfos(Class<?> clazzFromModule, Builder<YangModuleInfo> moduleInfoSet)
            throws Exception {
        YangModuleInfo moduleInfo = BindingReflections.getModuleInfo(clazzFromModule);
        checkState(moduleInfo != null, "Module Info for %s is not available.", clazzFromModule);
        collectYangModuleInfo(moduleInfo, moduleInfoSet);
    }

    private static void collectYangModuleInfo(final YangModuleInfo moduleInfo,
            final Builder<YangModuleInfo> moduleInfoSet) throws IOException {
        moduleInfoSet.add(moduleInfo);
        for (YangModuleInfo dependency : moduleInfo.getImportedModules()) {
            collectYangModuleInfo(dependency, moduleInfoSet);
        }
    }

}
