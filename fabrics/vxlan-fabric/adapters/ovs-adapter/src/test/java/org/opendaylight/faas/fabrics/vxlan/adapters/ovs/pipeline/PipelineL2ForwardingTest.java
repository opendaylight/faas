package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
//import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class PipelineL2ForwardingTest  /*extends AbstractDataBrokerTest*/ {
    @Mock private DataBroker dataBroker;

    @InjectMocks private PipelineL2Forwarding pipelineL2Forwarding = new PipelineL2Forwarding(dataBroker);

    @Mock private WriteTransaction writeTransaction;
    @Mock private ReadOnlyTransaction readOnlyTransaction;
    @Mock private ReadWriteTransaction readWriteTransaction;
    @Mock CheckedFuture<Void, TransactionCommitFailedException> commitFuture;
    @Mock CheckedFuture<Optional<Flow>, ?> future;

    @Before
    public void initialTest() throws InterruptedException, ExecutionException {
        when(writeTransaction.submit()).thenReturn(commitFuture);
        when(readWriteTransaction.submit()).thenReturn(commitFuture);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        when(dataBroker.newReadWriteTransaction()).thenReturn(readWriteTransaction);

        NodeBuilder nodeBuilder = mock(NodeBuilder.class);
        when(nodeBuilder.getKey()).thenReturn(mock(NodeKey.class));

        FlowBuilder flowBuilder = new FlowBuilder();


        when(future.get()).thenReturn(Optional.of(flowBuilder.build()));

        when(readOnlyTransaction.read(eq(LogicalDatastoreType.CONFIGURATION), any(InstanceIdentifier.class))).thenReturn(future);

    }


    @Test
    public void testProgramLocalBcastToLocalPort() throws Exception {
        pipelineL2Forwarding.programLocalBcastToLocalPort(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3), true);
        pipelineL2Forwarding.programLocalBcastToLocalPort(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3), false);
    }

    @Test
    public void testProgramLocalBcastToTunnelPort() {
        pipelineL2Forwarding.programLocalBcastToTunnelPort(Long.valueOf(1), Long.valueOf(2), Long.valueOf(5), new IpAddress(new Ipv4Address("192.168.2.3")), true);
        pipelineL2Forwarding.programLocalBcastToTunnelPort(Long.valueOf(1), Long.valueOf(2), Long.valueOf(5), new IpAddress(new Ipv4Address("192.168.2.3")), false);
    }

    @Test
    public void testProgramLocalTableMiss() {
        pipelineL2Forwarding.programLocalTableMiss(Long.valueOf(1), Long.valueOf(1), true);
        pipelineL2Forwarding.programLocalTableMiss(Long.valueOf(1), Long.valueOf(1), false);
    }

    @Test
    public void testProgramLocalUcastOut() {
        pipelineL2Forwarding.programLocalUcastOut(Long.valueOf(1), Long.valueOf(2), Long.valueOf(4), "00:00:00:00:00:01", true);
        pipelineL2Forwarding.programLocalUcastOut(Long.valueOf(1), Long.valueOf(2), Long.valueOf(4), "00:00:00:00:00:01", false);
    }

    @Test
    public void testProgramRemoteBcastOutToLocalPort() {
        pipelineL2Forwarding.programRemoteBcastOutToLocalPort(Long.valueOf(1), Long.valueOf(3), Long.valueOf(2), true);
        pipelineL2Forwarding.programRemoteBcastOutToLocalPort(Long.valueOf(1), Long.valueOf(3), Long.valueOf(2), false);
    }

    @Test
    public void testProgramSfcTunnelOut() {
        pipelineL2Forwarding.programSfcTunnelOut(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3), "00:00:00:00:00:01", new IpAddress(new Ipv4Address("192.168.2.3")), true);
        pipelineL2Forwarding.programSfcTunnelOut(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3), "00:00:00:00:00:01", new IpAddress(new Ipv4Address("192.168.2.3")), false);
    }

    @Test
    public void testProgramTunnelOut() {
        pipelineL2Forwarding.programTunnelOut(Long.valueOf(1), Long.valueOf(1), Long.valueOf(1), "00:00:00:00:00:01", new IpAddress(new Ipv4Address("192.168.2.3")), true);
        pipelineL2Forwarding.programTunnelOut(Long.valueOf(1), Long.valueOf(1), Long.valueOf(1), "00:00:00:00:00:01", new IpAddress(new Ipv4Address("192.168.2.3")), false);
    }
}
