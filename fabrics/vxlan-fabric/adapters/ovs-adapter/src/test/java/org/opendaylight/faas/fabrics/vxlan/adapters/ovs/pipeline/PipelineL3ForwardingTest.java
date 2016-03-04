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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;

import com.google.common.util.concurrent.CheckedFuture;

@RunWith(MockitoJUnitRunner.class)
public class PipelineL3ForwardingTest {
    @Mock private DataBroker dataBroker;

    @InjectMocks private PipelineL3Forwarding pipelineL3Forwarding= new PipelineL3Forwarding(dataBroker);

    @Mock private WriteTransaction writeTransaction;
    @Mock CheckedFuture<Void, TransactionCommitFailedException> commitFuture;

    @Before
    public void initialTest() {
        when(writeTransaction.submit()).thenReturn(commitFuture);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);

    }

    @Test
    public void testProgramForwardingTableEntry() throws Exception {
        pipelineL3Forwarding.programForwardingTableEntry(Long.valueOf(1), Long.valueOf(1), new IpAddress(new Ipv4Address("192.168.2.3")), "00:00:00:00:00:01", true);
        pipelineL3Forwarding.programForwardingTableEntry(Long.valueOf(1), Long.valueOf(1), new IpAddress(new Ipv4Address("192.168.2.3")), "00:00:00:00:00:01", false);

    }

}
