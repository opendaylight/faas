package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import com.google.common.collect.Range;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute;
import java.math.BigInteger;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers
 *
 */
public class VniMembersBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers> {

    private List<HostRoute> _hostRoute;
    private VniMembersKey _key;
    private java.lang.Long _vni;
    private List<IpAddress> _vteps;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>> augmentation = Collections.emptyMap();

    public VniMembersBuilder() {
    }

    public VniMembersBuilder(VniMembers base) {
        if (base.getKey() == null) {
            this._key = new VniMembersKey(
                base.getVni()
            );
            this._vni = base.getVni();
        } else {
            this._key = base.getKey();
            this._vni = _key.getVni();
        }
        this._hostRoute = base.getHostRoute();
        this._vteps = base.getVteps();
        if (base instanceof VniMembersImpl) {
            VniMembersImpl impl = (VniMembersImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public List<HostRoute> getHostRoute() {
        return _hostRoute;
    }
    
    public VniMembersKey getKey() {
        return _key;
    }
    
    public java.lang.Long getVni() {
        return _vni;
    }
    
    public List<IpAddress> getVteps() {
        return _vteps;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public VniMembersBuilder setHostRoute(List<HostRoute> value) {
        this._hostRoute = value;
        return this;
    }
    
    public VniMembersBuilder setKey(VniMembersKey value) {
        this._key = value;
        return this;
    }
    
    private static void checkVniRange(final long value) {
        if (value >= 0L && value <= 4294967295L) {
            return;
        }
        throw new IllegalArgumentException(String.format("Invalid range: %s, expected: createRangeString(constraints).", value));
    }
    
    public VniMembersBuilder setVni(java.lang.Long value) {
        if (value != null) {
            checkVniRange(value);
        }
        this._vni = value;
        return this;
    }
    /**
     * @deprecated This method is slated for removal in a future release. See BUG-1485 for details.
     */
    @Deprecated
    public static List<Range<BigInteger>> _vni_range() {
        final List<Range<BigInteger>> ret = new java.util.ArrayList<>(1);
        ret.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
        return ret;
    }
    
    public VniMembersBuilder setVteps(List<IpAddress> value) {
        this._vteps = value;
        return this;
    }
    
    public VniMembersBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public VniMembersBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public VniMembers build() {
        return new VniMembersImpl(this);
    }

    private static final class VniMembersImpl implements VniMembers {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers.class;
        }

        private final List<HostRoute> _hostRoute;
        private final VniMembersKey _key;
        private final java.lang.Long _vni;
        private final List<IpAddress> _vteps;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>> augmentation = Collections.emptyMap();

        private VniMembersImpl(VniMembersBuilder base) {
            if (base.getKey() == null) {
                this._key = new VniMembersKey(
                    base.getVni()
                );
                this._vni = base.getVni();
            } else {
                this._key = base.getKey();
                this._vni = _key.getVni();
            }
            this._hostRoute = base.getHostRoute();
            this._vteps = base.getVteps();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public List<HostRoute> getHostRoute() {
            return _hostRoute;
        }
        
        @Override
        public VniMembersKey getKey() {
            return _key;
        }
        
        @Override
        public java.lang.Long getVni() {
            return _vni;
        }
        
        @Override
        public List<IpAddress> getVteps() {
            return _vteps;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + ((_hostRoute == null) ? 0 : _hostRoute.hashCode());
            result = prime * result + ((_key == null) ? 0 : _key.hashCode());
            result = prime * result + ((_vni == null) ? 0 : _vni.hashCode());
            result = prime * result + ((_vteps == null) ? 0 : _vteps.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers other = (org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers)obj;
            if (_hostRoute == null) {
                if (other.getHostRoute() != null) {
                    return false;
                }
            } else if(!_hostRoute.equals(other.getHostRoute())) {
                return false;
            }
            if (_key == null) {
                if (other.getKey() != null) {
                    return false;
                }
            } else if(!_key.equals(other.getKey())) {
                return false;
            }
            if (_vni == null) {
                if (other.getVni() != null) {
                    return false;
                }
            } else if(!_vni.equals(other.getVni())) {
                return false;
            }
            if (_vteps == null) {
                if (other.getVteps() != null) {
                    return false;
                }
            } else if(!_vteps.equals(other.getVteps())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                VniMembersImpl otherImpl = (VniMembersImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("VniMembers [");
            boolean first = true;
        
            if (_hostRoute != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_hostRoute=");
                builder.append(_hostRoute);
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
            if (_vni != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_vni=");
                builder.append(_vni);
             }
            if (_vteps != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_vteps=");
                builder.append(_vteps);
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
