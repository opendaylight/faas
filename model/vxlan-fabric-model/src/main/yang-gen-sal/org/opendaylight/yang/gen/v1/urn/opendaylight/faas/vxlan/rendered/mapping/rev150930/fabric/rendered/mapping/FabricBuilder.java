package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric
 *
 */
public class FabricBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric> {

    private FabricId _id;
    private FabricKey _key;
    private List<VniMembers> _vniMembers;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>> augmentation = Collections.emptyMap();

    public FabricBuilder() {
    }

    public FabricBuilder(Fabric base) {
        if (base.getKey() == null) {
            this._key = new FabricKey(
                base.getId()
            );
            this._id = base.getId();
        } else {
            this._key = base.getKey();
            this._id = _key.getId();
        }
        this._vniMembers = base.getVniMembers();
        if (base instanceof FabricImpl) {
            FabricImpl impl = (FabricImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public FabricId getId() {
        return _id;
    }
    
    public FabricKey getKey() {
        return _key;
    }
    
    public List<VniMembers> getVniMembers() {
        return _vniMembers;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public FabricBuilder setId(FabricId value) {
        if (value != null) {
        }
        this._id = value;
        return this;
    }
    
    public FabricBuilder setKey(FabricKey value) {
        this._key = value;
        return this;
    }
    
    public FabricBuilder setVniMembers(List<VniMembers> value) {
        this._vniMembers = value;
        return this;
    }
    
    public FabricBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public FabricBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public Fabric build() {
        return new FabricImpl(this);
    }

    private static final class FabricImpl implements Fabric {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric.class;
        }

        private final FabricId _id;
        private final FabricKey _key;
        private final List<VniMembers> _vniMembers;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>> augmentation = Collections.emptyMap();

        private FabricImpl(FabricBuilder base) {
            if (base.getKey() == null) {
                this._key = new FabricKey(
                    base.getId()
                );
                this._id = base.getId();
            } else {
                this._key = base.getKey();
                this._id = _key.getId();
            }
            this._vniMembers = base.getVniMembers();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public FabricId getId() {
            return _id;
        }
        
        @Override
        public FabricKey getKey() {
            return _key;
        }
        
        @Override
        public List<VniMembers> getVniMembers() {
            return _vniMembers;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + ((_id == null) ? 0 : _id.hashCode());
            result = prime * result + ((_key == null) ? 0 : _key.hashCode());
            result = prime * result + ((_vniMembers == null) ? 0 : _vniMembers.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric other = (org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric)obj;
            if (_id == null) {
                if (other.getId() != null) {
                    return false;
                }
            } else if(!_id.equals(other.getId())) {
                return false;
            }
            if (_key == null) {
                if (other.getKey() != null) {
                    return false;
                }
            } else if(!_key.equals(other.getKey())) {
                return false;
            }
            if (_vniMembers == null) {
                if (other.getVniMembers() != null) {
                    return false;
                }
            } else if(!_vniMembers.equals(other.getVniMembers())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                FabricImpl otherImpl = (FabricImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("Fabric [");
            boolean first = true;
        
            if (_id != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_id=");
                builder.append(_id);
             }
            if (_key != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_key=");
                builder.append(_key);
             }
            if (_vniMembers != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_vniMembers=");
                builder.append(_vniMembers);
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
