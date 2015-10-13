package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.concepts.Builder;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.Attributes1} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.Attributes1
 *
 */
public class Attributes1Builder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.Attributes1> {

    private Vtep _vtep;


    public Attributes1Builder() {
    }

    public Attributes1Builder(Attributes1 base) {
        this._vtep = base.getVtep();
    }


    public Vtep getVtep() {
        return _vtep;
    }

    public Attributes1Builder setVtep(Vtep value) {
        this._vtep = value;
        return this;
    }

    public Attributes1 build() {
        return new Attributes1Impl(this);
    }

    private static final class Attributes1Impl implements Attributes1 {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.Attributes1> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.Attributes1.class;
        }

        private final Vtep _vtep;


        private Attributes1Impl(Attributes1Builder base) {
            this._vtep = base.getVtep();
        }

        @Override
        public Vtep getVtep() {
            return _vtep;
        }

        private int hash = 0;
        private volatile boolean hashValid = false;
        
        @Override
        public int hashCode() {
            if (hashValid) {
                return hash;
            }
        
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_vtep == null) ? 0 : _vtep.hashCode());
        
            hash = result;
            hashValid = true;
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.Attributes1.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.Attributes1 other = (org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.Attributes1)obj;
            if (_vtep == null) {
                if (other.getVtep() != null) {
                    return false;
                }
            } else if(!_vtep.equals(other.getVtep())) {
                return false;
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("Attributes1 [");
            boolean first = true;
        
            if (_vtep != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_vtep=");
                builder.append(_vtep);
             }
            return builder.append(']').toString();
        }
    }

}
