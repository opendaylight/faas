/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import java.util.Collection;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.spi.FabricListener;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.faas.fabric.vxlan.res.ResourceManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.AddToVxlanFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.FabricVxlanDeviceAdapterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.RmFromVxlanFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.VtepAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicRouterAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicSwitchAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.FabricKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;

public class DistributedFabricListener implements AutoCloseable, FabricListener {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedFabricListener.class);

    private final InstanceIdentifier<FabricNode> fabricIId;
    private final FabricId fabricid;
    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;

    private ListeningExecutorService executor;
    private EndPointManager epMgr;
    private final FabricContext fabricCtx;

    public DistributedFabricListener (InstanceIdentifier<FabricNode> fabricIId,
    						final DataBroker dataProvider,
                             final RpcProviderRegistry rpcRegistry,
                             final FabricContext fabricCtx) {
    	this.fabricIId = fabricIId;
    	this.fabricid = new FabricId(fabricIId.firstKeyOf(Node.class).getNodeId());
        this.dataBroker = dataProvider;
        this.rpcRegistry = rpcRegistry;

        executor = fabricCtx.executor;

        this.fabricCtx = fabricCtx;
        epMgr = new EndPointManager(dataProvider, rpcRegistry, fabricCtx);
    }

    @Override
    public void close() throws Exception {
        epMgr.close();
    }

    private FabricVxlanDeviceAdapterService getVlanDeviceAdapter() {
        return rpcRegistry.getRpcService(FabricVxlanDeviceAdapterService.class);
    }

    @Override
    public void fabricCreated(FabricNode fabric) {

        List<DeviceNodes> devices = fabric.getFabricAttribute().getDeviceNodes();
        if (devices != null) {
            for (DeviceNodes deviceNode : devices) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) deviceNode.getDeviceRef().getValue();

                deviceAdded(deviceIId);
            }
        }

        ResourceManager.initResourceManager(fabricid);
    }

    @Override
    public void deviceAdded(final InstanceIdentifier<Node> deviceIId) {

        AddToVxlanFabricInputBuilder builder = new AddToVxlanFabricInputBuilder();
        builder.setNodeId(deviceIId);
        builder.setFabricId(fabricIId.firstKeyOf(Node.class).getNodeId());
        getVlanDeviceAdapter().addToVxlanFabric(builder.build());

        ReadOnlyTransaction rt = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Node>,ReadFailedException> readFuture = rt.read(LogicalDatastoreType.OPERATIONAL, deviceIId);
        Futures.addCallback(readFuture, new FutureCallback<Optional<Node>>(){

            @Override
            public void onSuccess(Optional<Node> result) {
                Node device = result.get();
                FabricCapableDevice augment = device.getAugmentation(FabricCapableDevice.class);
                IpAddress vtep = null;
                if (augment != null) {
                    VtepAttribute vtepAttr = augment.getAttributes().getAugmentation(VtepAttribute.class);
                    if (vtepAttr != null) {
                        vtep = vtepAttr.getVtep().getIp();
                    }
                }

               DeviceContext devCtx = fabricCtx.addDeviceSwitch(deviceIId, vtep);
               Collection<LogicSwitchContext> lswCtxs = fabricCtx.getLogicSwitchCtxs();
               for (LogicSwitchContext lswCtx : lswCtxs) {
            	   lswCtx.checkAndSetNewMember(DeviceKey.newInstance(deviceIId), vtep);
                   devCtx.createBridgeDomain(lswCtx);
               }
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("unexecpted exception", t);
            }}, executor);

    }

    @Override
    public void deviceRemoved(final InstanceIdentifier<Node> deviceIId) {

    	NodeId deviceId = deviceIId.firstKeyOf(Node.class).getNodeId();
    	
        RmFromVxlanFabricInputBuilder builder = new RmFromVxlanFabricInputBuilder();
        builder.setNodeId(deviceIId);
        builder.setFabricId(fabricIId.firstKeyOf(Node.class).getNodeId());
        getVlanDeviceAdapter().rmFromVxlanFabric(builder.build());

        InstanceIdentifier<DeviceNodes> devicepath = fabricIId.builder().child(FabricAttribute.class)
                .child(DeviceNodes.class, new DeviceNodesKey(new NodeRef(deviceIId))).build();

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.delete(LogicalDatastoreType.OPERATIONAL, devicepath);
        trans.delete(LogicalDatastoreType.OPERATIONAL, deviceIId.augmentation(FabricCapableDevice.class).child(Config.class));
        MdSalUtils.wrapperSubmit(trans, executor);

        fabricCtx.removeDeviceSwitch(deviceId);
        Collection<LogicSwitchContext> lswCtxs = fabricCtx.getLogicSwitchCtxs();
        DeviceKey dkey = DeviceKey.newInstance(deviceIId);
        for (LogicSwitchContext lswCtx : lswCtxs) {
     	   lswCtx.removeMember(dkey);
        }
    }

    @Override
    public void fabricDeleted(Node node) {
        FabricNode fabric = node.getAugmentation(FabricNode.class);
        FabricId fabricid = new FabricId(node.getNodeId());
        List<DeviceNodes> devices = fabric.getFabricAttribute().getDeviceNodes();
        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.delete(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(FabricRenderedMapping.class).child(Fabric.class, new FabricKey(fabricid)));

        if (devices != null) {
            for (DeviceNodes deviceNode : devices) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) deviceNode.getDeviceRef().getValue();

                RmFromVxlanFabricInputBuilder builder = new RmFromVxlanFabricInputBuilder();
                builder.setNodeId(deviceIId);
                builder.setFabricId(node.getNodeId());
                getVlanDeviceAdapter().rmFromVxlanFabric(builder.build());

                wt.delete(LogicalDatastoreType.OPERATIONAL, deviceIId.augmentation(FabricCapableDevice.class).child(Config.class));
            }
        }
        MdSalUtils.wrapperSubmit(wt, executor);

        ResourceManager.freeResourceManager(new FabricId(node.getNodeId()));
        fabricCtx.close();
    }
    
	@Override
	public void aclUpdate(InstanceIdentifier<FabricAcl> iid, boolean delete) {
		InstanceIdentifier<TerminationPoint> tpiid = iid.firstIdentifierOf(TerminationPoint.class);
		String aclName = iid.firstKeyOf(FabricAcl.class).getFabricAclName();
		if (tpiid != null) {
			if (delete) {
				executor.submit(AclRenderer.newRmAclTask(dataBroker, tpiid, aclName));	
			} else {
				executor.submit(AclRenderer.newAddAclTask(dataBroker, tpiid, aclName));
			}
			return;
		}
		
		NodeId nodeId = iid.firstKeyOf(Node.class).getNodeId();
		if (fabricCtx.isValidLogicSwitch(nodeId)) {
			if (delete) {
				fabricCtx.getLogicSwitchCtx(nodeId).removeAcl(aclName);
			} else {
				fabricCtx.getLogicSwitchCtx(nodeId).addAcl(aclName);;
			}
		} else {
			LogicRouterContext lrCtx = fabricCtx.getLogicRouterCtx(nodeId);
			lrCtx.addAcl(aclName);;
			for (Long vni : lrCtx.getVnis()) {
				NodeId lsw = lrCtx.getGatewayPortByVni(vni).getLogicSwitch();
				LogicSwitchContext lswCtx = fabricCtx.getLogicSwitchCtx(lsw);
				if (lswCtx != null) {
					if (delete) {
						lswCtx.removeVrfAcl(aclName);
					} else {
						lswCtx.addVrfAcl(aclName);
					}
				}
			}
		}
	}

	@Override
	public void logicSwitchCreated(NodeId nodeId, Node lse) {
		// for distributed Fabric, we add logic switch to all device

		long segmentId = lse.getAugmentation(LogicSwitchAugment.class).getLswAttribute().getSegmentId();
		LogicSwitchContext lswCtx = fabricCtx.addLogicSwitch(nodeId, segmentId);

		Collection<DeviceContext> devices = fabricCtx.getDeviceCtxs();
		if (devices != null) {
			for (DeviceContext devCtx : devices) {
				lswCtx.checkAndSetNewMember(devCtx.getKey(), devCtx.getVtep());
				devCtx.createBridgeDomain(lswCtx);
			}
		}
	}

	@Override
	public void logicSwitchRemoved(Node lsw) {

		LogicSwitchContext lswCtx = fabricCtx.getLogicSwitchCtx(lsw.getNodeId());
		
		Collection<DeviceContext> devices = fabricCtx.getDeviceCtxs();
		if (devices != null) {
			for (DeviceContext devCtx : devices) {
				devCtx.removeBridgeDomain(lswCtx);
			}
		}
		fabricCtx.removeLogicSwitch(lsw.getNodeId());
		lswCtx.close();
	}

	@Override
	public void logicRouterCreated(NodeId nodeId, Node lr) {
		long vrfctx = lr.getAugmentation(LogicRouterAugment.class).getLrAttribute().getVrfCtx();
		fabricCtx.addLogicRouter(nodeId, vrfctx);
	}

	@Override
	public void logicRouterRemoved(Node lr) {
		fabricCtx.removeLogicSwitch(lr.getNodeId());
	}

	@Override
	public void gatewayRemoved(NodeId lswId, NodeId lrId) {
		fabricCtx.unAssociateSwitchToRouter(lswId, lrId);
		
	}
}