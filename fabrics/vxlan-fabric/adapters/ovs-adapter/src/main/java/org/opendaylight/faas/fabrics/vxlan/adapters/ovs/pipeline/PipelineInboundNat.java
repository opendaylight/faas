package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

public class PipelineInboundNat extends AbstractServiceInstance {

    public PipelineInboundNat(DataBroker dataBroker) {
        super(Service.INBOUND_NAT, dataBroker);
    }
}
