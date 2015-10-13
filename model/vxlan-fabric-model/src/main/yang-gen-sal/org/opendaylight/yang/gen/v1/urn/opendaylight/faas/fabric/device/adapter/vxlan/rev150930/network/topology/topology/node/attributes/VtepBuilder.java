package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep
 *
 */
public class VtepBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep> {

    private IpAddress _ip;
    private TpId _tpId;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>> augmentation = Collections.emptyMap();

    public VtepBuilder() {
    }

    public VtepBuilder(Vtep base) {
        this._ip = base.getIp();
        this._tpId = base.getTpId();
        if (base instanceof VtepImpl) {
            VtepImpl impl = (VtepImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public IpAddress getIp() {
        return _ip;
    }
    
    public TpId getTpId() {
        return _tpId;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public VtepBuilder setIp(IpAddress value) {
        if (value != null) {
        }
        this._ip = value;
        return this;
    }
    
    public VtepBuilder setTpId(TpId value) {
        if (value != null) {
        }
        this._tpId = value;
        return this;
    }
    
    public VtepBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public VtepBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public Vtep build() {
        return new VtepImpl(this);
    }

    private static final class VtepImpl implements Vtep {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep.class;
        }

        private final IpAddress _ip;
        private final TpId _tpId;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>> augmentation = Collections.emptyMap();

        private VtepImpl(VtepBuilder base) {
            this._ip = base.getIp();
            this._tpId = base.getTpId();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public IpAddress getIp() {
            return _ip;
        }
        
        @Override
        public TpId getTpId() {
            return _tpId;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
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
            result = prime * result + ((_ip == null) ? 0 : _ip.hashCode());
            result = prime * result + ((_tpId == null) ? 0 : _tpId.hashCode());
            result = prime * result + ((augmentation == null) ? 0 : augmentation.hashCode());
        
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep other = (org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep)obj;
            if (_ip == null) {
                if (other.getIp() != null) {
                    return false;
                }
            } else if(!_ip.equals(other.getIp())) {
                return false;
            }
            if (_tpId == null) {
                if (other.getTpId() != null) {
                    return false;
                }
            } else if(!_tpId.equals(other.getTpId())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                VtepImpl otherImpl = (VtepImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("Vtep [");
            boolean first = true;
        
            if (_ip != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ip=");
                builder.append(_ip);
             }
            if (_tpId != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_tpId=");
                builder.append(_tpId);
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
