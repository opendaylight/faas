/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
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
