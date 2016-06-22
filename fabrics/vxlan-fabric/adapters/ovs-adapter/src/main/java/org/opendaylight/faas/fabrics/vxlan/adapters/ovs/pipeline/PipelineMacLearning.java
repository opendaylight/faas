package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

public class PipelineMacLearning extends AbstractServiceInstance {

    public PipelineMacLearning(DataBroker dataBroker) {
        super(Service.MAC_LEARNING, dataBroker);
    }
}
