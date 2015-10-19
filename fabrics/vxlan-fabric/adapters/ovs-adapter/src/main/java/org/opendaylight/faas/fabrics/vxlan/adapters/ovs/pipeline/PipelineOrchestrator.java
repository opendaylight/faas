package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.Service;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.AbstractServiceInstance;
import org.osgi.framework.ServiceReference;

public interface PipelineOrchestrator {
    Service getNextServiceInPipeline(Service service);
    AbstractServiceInstance getServiceInstance(Service service);
    void enqueue(Node node);
    void registerService(final ServiceReference ref, AbstractServiceInstance serviceInstance);
    void unregisterService(final ServiceReference ref);
}
