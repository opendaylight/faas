/*
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabricmgr.api.VcontainerServiceProviderAPI;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3InputBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricMgrProvider implements AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(FabricMgrProvider.class);
    private final ExecutorService threadPool;
    private final VcontainerServiceProvider vcProvider;
    private final VcNetNodeServiceProvider netNodeServiceProvider;
    private Map<Uuid, List<NodeId>> vcResourceStore; // tenantId-vfabricList lookup map

    public FabricMgrProvider(final DataBroker dataProvider, final RpcProviderRegistry rpcRegistry,
            final NotificationService notificationService) {
        super();
        FabMgrDatastoreDependency.setDataProvider(dataProvider);
        FabMgrDatastoreDependency.setRpcRegistry(rpcRegistry);
        FabMgrDatastoreDependency.setNotificationService(notificationService);

        int numCPU = Runtime.getRuntime().availableProcessors();
        this.threadPool = Executors.newFixedThreadPool(numCPU * 2);
        this.vcProvider = new VcontainerServiceProvider(this.threadPool);
        this.vcProvider.initialize();
        this.netNodeServiceProvider = new VcNetNodeServiceProvider(this.threadPool);
        this.netNodeServiceProvider.initialize();

        this.vcResourceStore = new HashMap<Uuid, List<NodeId>>();

        VcontainerServiceProviderAPI.setFabricMgrProvider(this);

        LOG.info("FABMGR: FabricMgrProvider has Started");
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> arg0) {

    }

    @Override
    public void close() throws Exception {
        this.vcProvider.close();
        this.threadPool.shutdown();
    }

    public void createLneLayer2(Uuid tenantId, CreateLneLayer2Input lneInput) {
        CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder(lneInput);
        List<NodeId> vfabricIdList = this.vcResourceStore.get(tenantId);
        if (vfabricIdList != null && vfabricIdList.isEmpty() == false) {
            builder.setVfabricId(vfabricIdList.get(0));
        } else {
            LOG.info("FABMGR: ERROR: createLneLayer2: vfabricIdList is null: {}", tenantId.getValue());
        }

        this.netNodeServiceProvider.createLneLayer2(builder.build());
    }

    public void createLneLayer3(Uuid tenantId, CreateLneLayer3Input lne3Input) {
        CreateLneLayer3InputBuilder builder = new CreateLneLayer3InputBuilder(lne3Input);
        List<NodeId> vfabricIdList = this.vcResourceStore.get(tenantId);
        if (vfabricIdList != null && vfabricIdList.isEmpty() == false) {
            builder.setVfabricId(vfabricIdList.get(0));
        } else {
            LOG.info("FABMGR: ERROR: createLneLayer3: vfabricIdList is null: {}", tenantId.getValue());
        }

        this.netNodeServiceProvider.createLneLayer3(builder.build());
    }

    public Map<Uuid, List<NodeId>> getVcResourceStore() {
        return vcResourceStore;
    }

    public void setVcResourceStore(Map<Uuid, List<NodeId>> vcResourceStore) {
        this.vcResourceStore = vcResourceStore;
    }

    public void addVfabricsToResourceStore(TenantId tenantId, List<NodeId> vfabricIdList) {
        List<NodeId> curVfabIdList = this.vcResourceStore.get(tenantId);
        if(curVfabIdList == null)
        {
            this.vcResourceStore.put(tenantId, vfabricIdList);
        }
        else
        {
            curVfabIdList.addAll(vfabricIdList);
        }
    }
}
