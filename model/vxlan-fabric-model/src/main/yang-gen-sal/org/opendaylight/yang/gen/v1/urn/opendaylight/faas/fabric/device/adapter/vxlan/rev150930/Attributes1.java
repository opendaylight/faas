package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Attributes;


public interface Attributes1
    extends
    DataObject,
    Augmentation<Attributes>
{




    Vtep getVtep();

}

