/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PipelineOrchestrator {
    private static List<Service> staticPipeline = Lists.newArrayList(Service.TRAFFIC_CLASSIFIER, Service.ARP_HANDlER,
            Service.ACL_HANDlER, Service.L3_ROUTING, Service.L3_FORWARDING, Service.L2_FORWARDING);
    static Map<Service, AbstractServiceInstance> serviceRegistry = Maps.newConcurrentMap();
    //private OvsSouthboundUtils ovsSouthboundUtils;

    public static Service getNextServiceInPipeline(Service service) {
        int index = staticPipeline.indexOf(service);
        if (index >= staticPipeline.size() - 1) {
            return null;
        }
        return staticPipeline.get(index + 1);
    }

//    public void initialPipeline(Node node) {
//        try {
//            for (Service service : staticPipeline) {
//                AbstractServiceInstance serviceInstance = getServiceInstance(service);
//                if (serviceInstance != null && OvsSouthboundUtils.getBridge(node) != null) {
//                    serviceInstance.programDefaultPipelineRule(node);
//                }
//            }
//        } catch (Exception e) {
//            LOG.warn("Processing interrupted, terminating ", e);
//        }
//    }
//
//    public void initialPipeline(Node node, AbstractServiceInstance serviceInstance) {
//        if (serviceInstance != null && OvsSouthboundUtils.getBridge(node) != null) {
//            serviceInstance.programDefaultPipelineRule(node);
//        }
//    }

//    public AbstractServiceInstance getServiceInstance(Service service) {
//        if (service == null) {
//            return null;
//        }
//        return serviceRegistry.get(service);
//    }
//
//    public static void setServiceRegistry(Service pipelineService, AbstractServiceInstance serviceInstance) {
//        serviceRegistry.put(pipelineService, serviceInstance);
//    }

}
