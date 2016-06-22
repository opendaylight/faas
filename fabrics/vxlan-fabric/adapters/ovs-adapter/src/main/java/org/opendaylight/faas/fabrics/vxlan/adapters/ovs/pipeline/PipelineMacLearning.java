package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineMacLearning extends AbstractServiceInstance {
    private static final Logger LOG = LoggerFactory.getLogger(PipelineL2Forwarding.class);

    public PipelineMacLearning(DataBroker dataBroker) {
        super(Service.MAC_LEARNING, dataBroker);
    }
}
