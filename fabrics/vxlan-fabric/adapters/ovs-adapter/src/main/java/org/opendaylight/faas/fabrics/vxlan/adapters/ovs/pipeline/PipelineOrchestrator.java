/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import java.util.List;
import com.google.common.collect.Lists;

public class PipelineOrchestrator {
    private static List<Service> staticPipeline = Lists.newArrayList(Service.TRAFFIC_CLASSIFIER, Service.ARP_HANDlER,
            Service.L3_ROUTING, Service.L3_FORWARDING, Service.ACL_HANDlER, Service.L2_FORWARDING);

    public static Service getNextServiceInPipeline(Service service) {
        int index = staticPipeline.indexOf(service);
        if (index >= staticPipeline.size() - 1) {
            return null;
        }
        return staticPipeline.get(index + 1);
    }

}
