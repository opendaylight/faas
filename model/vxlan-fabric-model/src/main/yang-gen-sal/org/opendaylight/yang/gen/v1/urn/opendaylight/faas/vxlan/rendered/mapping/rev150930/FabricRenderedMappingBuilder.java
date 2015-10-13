package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.List;
import java.util.Collections;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping
 *
 */
public class FabricRenderedMappingBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping> {

    private List<Fabric> _fabric;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>> augmentation = Collections.emptyMap();

    public FabricRenderedMappingBuilder() {
    }

    public FabricRenderedMappingBuilder(FabricRenderedMapping base) {
        this._fabric = base.getFabric();
        if (base instanceof FabricRenderedMappingImpl) {
            FabricRenderedMappingImpl impl = (FabricRenderedMappingImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public List<Fabric> getFabric() {
        return _fabric;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public FabricRenderedMappingBuilder setFabric(List<Fabric> value) {
        this._fabric = value;
        return this;
    }
    
    public FabricRenderedMappingBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public FabricRenderedMappingBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public FabricRenderedMapping build() {
        return new FabricRenderedMappingImpl(this);
    }

    private static final class FabricRenderedMappingImpl implements FabricRenderedMapping {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping.class;
        }

        private final List<Fabric> _fabric;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>> augmentation = Collections.emptyMap();

        private FabricRenderedMappingImpl(FabricRenderedMappingBuilder base) {
            this._fabric = base.getFabric();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public List<Fabric> getFabric() {
            return _fabric;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + ((_fabric == null) ? 0 : _fabric.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping other = (org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping)obj;
            if (_fabric == null) {
                if (other.getFabric() != null) {
                    return false;
                }
            } else if(!_fabric.equals(other.getFabric())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                FabricRenderedMappingImpl otherImpl = (FabricRenderedMappingImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("FabricRenderedMapping [");
            boolean first = true;
        
            if (_fabric != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_fabric=");
                builder.append(_fabric);
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
