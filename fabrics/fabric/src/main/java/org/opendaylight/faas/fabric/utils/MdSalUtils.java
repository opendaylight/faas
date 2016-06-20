/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.utils;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import java.util.concurrent.ExecutorService;

import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.faas.fabric.general.Constants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MdSalUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MdSalUtils.class);

    public static InstanceIdentifier<Topology> createTopoIId(String topoId) {
        return InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(topoId)));
    }

    public static Topology newTopo(String topoId) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setKey(new TopologyKey(new TopologyId(topoId)));
        return builder.build();
    }

    public static InstanceIdentifier<Node> createNodeIId(String topoId, String nodeId) {
        return InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(topoId)))
                .child(Node.class, new NodeKey(new NodeId(nodeId)));
    }

    public static InstanceIdentifier<Node> createNodeIId(String topoId, NodeId nodeId) {
        return InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(topoId)))
                .child(Node.class, new NodeKey(nodeId));
    }

    public static InstanceIdentifier<Node> createNodeIId(FabricId topoId, NodeId nodeId) {
        return InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(topoId)))
                .child(Node.class, new NodeKey(nodeId));
    }

    public static InstanceIdentifier<Node> createFNodeIId(FabricId fabricId) {
        return Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(fabricId));
    }

    public static InstanceIdentifier<FabricNode> createFabricIId(FabricId fabricId) {
        return Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(fabricId))
                .augmentation(FabricNode.class);
    }

    public static InstanceIdentifier<TerminationPoint> createLogicPortIId(FabricId fabricId, NodeId nodeId, TpId tpid) {
        return InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(fabricId)))
                .child(Node.class, new NodeKey(nodeId))
                .child(TerminationPoint.class, new TerminationPointKey(new TpId(tpid)));
    }

    public static InstanceIdentifier<Link> createLinkIId(FabricId fabricId, LinkId linkid) {
        return InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(fabricId)))
                .child(Link.class, new LinkKey(linkid));
    }

    public static InstanceIdentifier<TerminationPoint> createFabricPortIId(FabricId fabricId, TpId tpid) {
        return Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(fabricId))
                .child(TerminationPoint.class, new TerminationPointKey(new TpId(tpid)));
    }

    public static void wrapperSubmit(final WriteTransaction trans, ExecutorService executor) {
        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();
        Futures.addCallback(future, new FutureCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onFailure(Throwable th) {
                LOG.error("submit failed.", th);
            }
        }, executor);
    }

    public static void wrapperSubmit(final WriteTransaction trans) {
        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();
        Futures.addCallback(future, new FutureCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onFailure(Throwable th) {
                LOG.error("submit failed.", th);
            }
        });
    }
}
