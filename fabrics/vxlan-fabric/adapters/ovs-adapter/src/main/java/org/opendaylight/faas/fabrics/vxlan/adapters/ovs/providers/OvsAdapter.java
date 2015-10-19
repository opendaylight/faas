package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers;


import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OvsAdapter implements AutoCloseable, DataChangeListener{

    private final FabricNodeManager fabricNodeManager = null;
    //private final HostRouteManager hostRouteManager;
    //private final BdIfManager bfIfManager;
    //private final AclManager aclManager;

    Openflow13Provider of13Provider = null;

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub

    }

}
