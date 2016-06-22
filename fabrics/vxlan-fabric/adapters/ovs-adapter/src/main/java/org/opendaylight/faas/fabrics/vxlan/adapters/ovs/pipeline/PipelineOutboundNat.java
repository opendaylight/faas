package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

public class PipelineOutboundNat extends AbstractServiceInstance{

    public PipelineOutboundNat(DataBroker dataBroker){
        super(Service.OUTBOUND_NAT, dataBroker);
    }
}
