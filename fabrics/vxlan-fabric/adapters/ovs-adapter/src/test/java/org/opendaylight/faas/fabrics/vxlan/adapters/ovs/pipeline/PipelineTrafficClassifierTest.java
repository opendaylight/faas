package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;

import com.google.common.util.concurrent.CheckedFuture;


@RunWith(MockitoJUnitRunner.class)
public class PipelineTrafficClassifierTest {

    @Mock private DataBroker dataBroker;

    @InjectMocks private PipelineTrafficClassifier trafficClassifier = new PipelineTrafficClassifier(dataBroker);

    @Mock private Node node;
    @Mock private WriteTransaction writeTransaction;
    @Mock CheckedFuture<Void, TransactionCommitFailedException> commitFuture;

    @Before
    public void initialTest() {
        when(writeTransaction.submit()).thenReturn(commitFuture);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
    }

    @Test
    public void testProgramDefaultPipelineRule() throws Exception {
        trafficClassifier.programDefaultPipelineRule(Long.valueOf(10));
        trafficClassifier.programDefaultPipelineRule(node);
    }

    @Test
    public void testProgramDropSrcIface() throws Exception {
        trafficClassifier.programDropSrcIface(Long.valueOf(10), Long.valueOf(3), true);
        trafficClassifier.programDropSrcIface(Long.valueOf(10), Long.valueOf(3), false);
    }

    @Test
    public void testProgramLocalInPort() throws Exception {
        trafficClassifier.programLocalInPort(Long.valueOf(10), Long.valueOf(2), Long.valueOf(3), "62:02:1a:00:b7:11", true);
        trafficClassifier.programLocalInPort(Long.valueOf(10), Long.valueOf(2), Long.valueOf(3), "62:02:1a:00:b7:11", false);
    }

    @Test
    public void testProgramTunnelIn() throws Exception {
        trafficClassifier.programTunnelIn(Long.valueOf(10), Long.valueOf(2), Long.valueOf(4), true);
        trafficClassifier.programTunnelIn(Long.valueOf(10), Long.valueOf(2), Long.valueOf(4), false);
    }

}