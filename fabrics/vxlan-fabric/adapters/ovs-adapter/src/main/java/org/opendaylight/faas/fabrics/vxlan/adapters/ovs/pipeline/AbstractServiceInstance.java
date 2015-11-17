/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineOrchestrator;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.Service;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.Constants;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OvsSouthboundUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.ovsdb.utils.mdsal.openflow.InstructionUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;

public abstract class AbstractServiceInstance {
    public static final String SERVICE_PROPERTY ="serviceProperty";
    public static final String OPENFLOW = "openflow:";
    private DataBroker dataBroker = null;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceInstance.class);

    //private volatile PipelineOrchestrator orchestrator;
    //private OvsSouthboundUtils ovsSouthboundUtils;

    private Service service;

    public AbstractServiceInstance(Service service, DataBroker dataBroker) {
        this.service = service;
        this.dataBroker = dataBroker;
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }



    public NodeBuilder createNodeBuilder(String nodeId) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder;
    }

    public boolean isBridgeInPipeline (Node node){
        String bridgeName = OvsSouthboundUtils.getBridgeName(node);
        return bridgeName != null && Constants.INTEGRATION_BRIDGE.equals(bridgeName);
    }

    public short getTable() {
        return service.getTable();
    }

    protected final InstructionBuilder getMutablePipelineInstructionBuilder() {
        Service nextService = PipelineOrchestrator.getNextServiceInPipeline(service);
        if (nextService != null) {
            return InstructionUtils.createGotoTableInstructions(new InstructionBuilder(), nextService.getTable());
        } else {
            return InstructionUtils.createDropInstructions(new InstructionBuilder());
        }
    }

    private static InstanceIdentifier<Flow> createFlowPath(FlowBuilder flowBuilder, NodeBuilder nodeBuilder) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        nodeBuilder.getKey())
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId()))
                .child(Flow.class, flowBuilder.getKey()).build();
    }

    private static InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node>
    createNodePath(NodeBuilder nodeBuilder) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        nodeBuilder.getKey()).build();
    }

    protected void writeFlow(FlowBuilder flowBuilder, NodeBuilder nodeBuilder) {
        LOG.debug("writeFlow: flowBuilder: {}, nodeBuilder: {}",
                flowBuilder.build(), nodeBuilder.build());
        WriteTransaction modification = dataBroker.newWriteOnlyTransaction();
        modification.put(LogicalDatastoreType.CONFIGURATION, createNodePath(nodeBuilder),
                nodeBuilder.build(), true /*createMissingParents*/);
        modification.put(LogicalDatastoreType.CONFIGURATION, createFlowPath(flowBuilder, nodeBuilder),
                flowBuilder.build(), true /*createMissingParents*/);

        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        try {
            commitFuture.get();
            LOG.debug("Transaction success for write of Flow {}", flowBuilder.getFlowName());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            modification.cancel();
        }
    }

    protected void removeFlow(FlowBuilder flowBuilder, NodeBuilder nodeBuilder) {
        WriteTransaction modification = dataBroker.newWriteOnlyTransaction();
        modification.delete(LogicalDatastoreType.CONFIGURATION, createFlowPath(flowBuilder, nodeBuilder));

        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        try {
            commitFuture.get();
            LOG.debug("Transaction success for deletion of Flow {}", flowBuilder.getFlowName());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            modification.cancel();
        }
    }

    public Flow getFlow(FlowBuilder flowBuilder, NodeBuilder nodeBuilder) {
        ReadOnlyTransaction readTx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<Flow> data =
                    readTx.read(LogicalDatastoreType.CONFIGURATION, createFlowPath(flowBuilder, nodeBuilder)).get();
            if (data.isPresent()) {
                return data.get();
            }
        } catch (InterruptedException|ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }

        LOG.debug("Cannot find data for Flow {}", flowBuilder.getFlowName());
        return null;
    }

    private long getDpid(Node node) {
        long dpid = OvsSouthboundUtils.getDataPathId(node);
        if (dpid == 0) {
            LOG.warn("getDpid: dpid not found: {}", node);
        }
        return dpid;
    }

    /**
     * Program Default Pipeline Flow.
     *
     * @param node on which the default pipeline flow is programmed.
     */
    public void programDefaultPipelineRule(Node node) {
        if (!isBridgeInPipeline(node)) {
            //LOG.trace("Bridge is not in pipeline {} ", node);
            return;
        }
        MatchBuilder matchBuilder = new MatchBuilder();
        FlowBuilder flowBuilder = new FlowBuilder();
        long dpid = getDpid(node);
        if (dpid == 0L) {
            LOG.info("could not find dpid: {}", node.getNodeId());
            return;
        }
        String nodeName = OPENFLOW + getDpid(node);
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);

        // Create the OF Actions and Instructions
        InstructionsBuilder isb = new InstructionsBuilder();

        // Instructions List Stores Individual Instructions
        List<Instruction> instructions = Lists.newArrayList();

        // Call the InstructionBuilder Methods Containing Actions
        InstructionBuilder ib = this.getMutablePipelineInstructionBuilder();
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());

        // Add InstructionBuilder to the Instruction(s)Builder List
        isb.setInstruction(instructions);

        // Add InstructionsBuilder to FlowBuilder
        flowBuilder.setInstructions(isb.build());

        String flowId = "DEFAULT_PIPELINE_FLOW_"+service.getTable();
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setPriority(0);
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(service.getTable());
        flowBuilder.setKey(key);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        writeFlow(flowBuilder, nodeBuilder);
    }

    public void programDefaultPipelineRule(Long dpid) {
        MatchBuilder matchBuilder = new MatchBuilder();
        FlowBuilder flowBuilder = new FlowBuilder();

        if (dpid == 0L) {
            LOG.info("could not find dpid: {}", dpid);
            return;
        }
        String nodeName = OPENFLOW + dpid;
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);

        // Create the OF Actions and Instructions
        InstructionsBuilder isb = new InstructionsBuilder();

        // Instructions List Stores Individual Instructions
        List<Instruction> instructions = Lists.newArrayList();

        // Call the InstructionBuilder Methods Containing Actions
        InstructionBuilder ib = this.getMutablePipelineInstructionBuilder();
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());

        // Add InstructionBuilder to the Instruction(s)Builder List
        isb.setInstruction(instructions);

        // Add InstructionsBuilder to FlowBuilder
        flowBuilder.setInstructions(isb.build());

        String flowId = "DEFAULT_PIPELINE_FLOW_"+service.getTable();
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setPriority(0);
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(service.getTable());
        flowBuilder.setKey(key);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        writeFlow(flowBuilder, nodeBuilder);
    }

}
