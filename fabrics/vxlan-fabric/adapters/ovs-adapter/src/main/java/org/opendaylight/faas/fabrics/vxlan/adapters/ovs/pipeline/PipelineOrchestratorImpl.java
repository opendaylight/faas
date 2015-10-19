package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.Service;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.Action;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.NodeCacheListener;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OvsSouthbound;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.AbstractServiceInstance;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PipelineOrchestratorImpl implements NodeCacheListener, PipelineOrchestrator {
    private static final Logger LOG = LoggerFactory.getLogger(PipelineOrchestratorImpl.class);
    private List<Service> staticPipeline = Lists.newArrayList(
            Service.TRAFFIC_CLASSIFIER,
            Service.ARP_HANDlER,
            Service.ACL_HANDlER,
            Service.L3_ROUTING,
            Service.L3_FORWARDING,
            Service.L2_FORWARDING
    );
    Map<Service, AbstractServiceInstance> serviceRegistry = Maps.newConcurrentMap();
    private volatile BlockingQueue<Node> queue;
    private OvsSouthbound southbound;

    @Override
    public Service getNextServiceInPipeline(Service service) {
        int index = staticPipeline.indexOf(service);
        if (index >= staticPipeline.size() - 1) {
            return null;
        }
        return staticPipeline.get(index + 1);
    }

    @Override
    public void registerService(ServiceReference ref, AbstractServiceInstance serviceInstance) {
        Service service = (Service)ref.getProperty(AbstractServiceInstance.SERVICE_PROPERTY);
        LOG.info("registerService {} - {}", serviceInstance, service);
        serviceRegistry.put(service, serviceInstance);
    }

    @Override
    public void unregisterService(ServiceReference ref) {
        serviceRegistry.remove(ref.getProperty(AbstractServiceInstance.SERVICE_PROPERTY));
    }

//    public final void start() {
//        eventHandler.submit(new Runnable()  {
//            @Override
//            public void run() {
//                try {
//                    while (true) {
//                        Node node = queue.take();
//                        LOG.info(">>>>> dequeue: {}", node);
//                        for (Service service : staticPipeline) {
//                            AbstractServiceInstance serviceInstance = getServiceInstance(service);
//                            if (serviceInstance != null && southbound.getBridge(node) != null) {
//                                serviceInstance.programDefaultPipelineRule(node);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    LOG.warn("Processing interrupted, terminating ", e);
//                }
//
//                while (!queue.isEmpty()) {
//                    queue.poll();
//                }
//                queue = null;
//            }
//        });
//    }
//
//    public void stop() {
//        queue.clear();
//        eventHandler.shutdownNow();
//    }

    public void initialPipeline() {
        try {
            while (true) {
                Node node = queue.take();
                LOG.info(">>>>> dequeue: {}", node);
                for (Service service : staticPipeline) {
                    AbstractServiceInstance serviceInstance = getServiceInstance(service);
                    if (serviceInstance != null && southbound.getBridge(node) != null) {
                        serviceInstance.programDefaultPipelineRule(node);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Processing interrupted, terminating ", e);
        }

        while (!queue.isEmpty()) {
            queue.poll();
        }
        queue = null;
    }

    @Override
    public void enqueue(Node node) {
        LOG.info(">>>>> enqueue: {}", node);
        try {
            queue.put(node);
        } catch (InterruptedException e) {
            LOG.warn("Failed to enqueue operation {}", node, e);
        }
    }

    @Override
    public void notifyNode(Node node, Action action) {
        if (action == Action.ADD) {
            enqueue(node);
        } else {
            LOG.info("update ignored: {}", node);
        }
    }

    @Override
    public AbstractServiceInstance getServiceInstance(Service service) {
        if (service == null) {
            return null;
        }
        return serviceRegistry.get(service);
    }

//    @Override
//    public void setDependencies(BundleContext bundleContext, ServiceReference serviceReference) {
//        NodeCacheManager nodeCacheManager =
//                (NodeCacheManager) ServiceHelper.getGlobalInstance(NodeCacheManager.class, this);
//        nodeCacheManager.cacheListenerAdded(
//                bundleContext.getServiceReference(PipelineOrchestrator.class.getName()), this);
//        southbound =
//                (OvsSouthbound) ServiceHelper.getGlobalInstance(OvsSouthbound.class, this);
//    }
//
//    @Override
//    public void setDependencies(Object impl) {}
//
//    @Override
//    public void setDependencies(ServiceReference serviceReference) {
//        // TODO Auto-generated method stub
//
//    }

}
