package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import com.google.common.collect.Range;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.math.BigInteger;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import java.util.List;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute
 *
 */
public class HostRouteBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute> {

    private Uuid _destBridgePort;
    private IpAddress _destVtep;
    private IpAddress _ip;
    private HostRouteKey _key;
    private MacAddress _mac;
    private java.lang.Long _vrfCtx;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>> augmentation = Collections.emptyMap();

    public HostRouteBuilder() {
    }

    public HostRouteBuilder(HostRoute base) {
        if (base.getKey() == null) {
            this._key = new HostRouteKey(
                base.getMac()
            );
            this._mac = base.getMac();
        } else {
            this._key = base.getKey();
            this._mac = _key.getMac();
        }
        this._destBridgePort = base.getDestBridgePort();
        this._destVtep = base.getDestVtep();
        this._ip = base.getIp();
        this._vrfCtx = base.getVrfCtx();
        if (base instanceof HostRouteImpl) {
            HostRouteImpl impl = (HostRouteImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public Uuid getDestBridgePort() {
        return _destBridgePort;
    }
    
    public IpAddress getDestVtep() {
        return _destVtep;
    }
    
    public IpAddress getIp() {
        return _ip;
    }
    
    public HostRouteKey getKey() {
        return _key;
    }
    
    public MacAddress getMac() {
        return _mac;
    }
    
    public java.lang.Long getVrfCtx() {
        return _vrfCtx;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public HostRouteBuilder setDestBridgePort(Uuid value) {
        if (value != null) {
        }
        this._destBridgePort = value;
        return this;
    }
    
    public HostRouteBuilder setDestVtep(IpAddress value) {
        if (value != null) {
        }
        this._destVtep = value;
        return this;
    }
    
    public HostRouteBuilder setIp(IpAddress value) {
        if (value != null) {
        }
        this._ip = value;
        return this;
    }
    
    public HostRouteBuilder setKey(HostRouteKey value) {
        this._key = value;
        return this;
    }
    
    public HostRouteBuilder setMac(MacAddress value) {
        if (value != null) {
        }
        this._mac = value;
        return this;
    }
    
    private static void checkVrfCtxRange(final long value) {
        if (value >= 0L && value <= 4294967295L) {
            return;
        }
        throw new IllegalArgumentException(String.format("Invalid range: %s, expected: createRangeString(constraints).", value));
    }
    
    public HostRouteBuilder setVrfCtx(java.lang.Long value) {
        if (value != null) {
            checkVrfCtxRange(value);
        }
        this._vrfCtx = value;
        return this;
    }
    /**
     * @deprecated This method is slated for removal in a future release. See BUG-1485 for details.
     */
    @Deprecated
    public static List<Range<BigInteger>> _vrfCtx_range() {
        final List<Range<BigInteger>> ret = new java.util.ArrayList<>(1);
        ret.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
        return ret;
    }
    
    public HostRouteBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public HostRouteBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public HostRoute build() {
        return new HostRouteImpl(this);
    }

    private static final class HostRouteImpl implements HostRoute {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute.class;
        }

        private final Uuid _destBridgePort;
        private final IpAddress _destVtep;
        private final IpAddress _ip;
        private final HostRouteKey _key;
        private final MacAddress _mac;
        private final java.lang.Long _vrfCtx;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>> augmentation = Collections.emptyMap();

        private HostRouteImpl(HostRouteBuilder base) {
            if (base.getKey() == null) {
                this._key = new HostRouteKey(
                    base.getMac()
                );
                this._mac = base.getMac();
            } else {
                this._key = base.getKey();
                this._mac = _key.getMac();
            }
            this._destBridgePort = base.getDestBridgePort();
            this._destVtep = base.getDestVtep();
            this._ip = base.getIp();
            this._vrfCtx = base.getVrfCtx();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public Uuid getDestBridgePort() {
            return _destBridgePort;
        }
        
        @Override
        public IpAddress getDestVtep() {
            return _destVtep;
        }
        
        @Override
        public IpAddress getIp() {
            return _ip;
        }
        
        @Override
        public HostRouteKey getKey() {
            return _key;
        }
        
        @Override
        public MacAddress getMac() {
            return _mac;
        }
        
        @Override
        public java.lang.Long getVrfCtx() {
            return _vrfCtx;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + ((_destBridgePort == null) ? 0 : _destBridgePort.hashCode());
            result = prime * result + ((_destVtep == null) ? 0 : _destVtep.hashCode());
            result = prime * result + ((_ip == null) ? 0 : _ip.hashCode());
            result = prime * result + ((_key == null) ? 0 : _key.hashCode());
            result = prime * result + ((_mac == null) ? 0 : _mac.hashCode());
            result = prime * result + ((_vrfCtx == null) ? 0 : _vrfCtx.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute other = (org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute)obj;
            if (_destBridgePort == null) {
                if (other.getDestBridgePort() != null) {
                    return false;
                }
            } else if(!_destBridgePort.equals(other.getDestBridgePort())) {
                return false;
            }
            if (_destVtep == null) {
                if (other.getDestVtep() != null) {
                    return false;
                }
            } else if(!_destVtep.equals(other.getDestVtep())) {
                return false;
            }
            if (_ip == null) {
                if (other.getIp() != null) {
                    return false;
                }
            } else if(!_ip.equals(other.getIp())) {
                return false;
            }
            if (_key == null) {
                if (other.getKey() != null) {
                    return false;
                }
            } else if(!_key.equals(other.getKey())) {
                return false;
            }
            if (_mac == null) {
                if (other.getMac() != null) {
                    return false;
                }
            } else if(!_mac.equals(other.getMac())) {
                return false;
            }
            if (_vrfCtx == null) {
                if (other.getVrfCtx() != null) {
                    return false;
                }
            } else if(!_vrfCtx.equals(other.getVrfCtx())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                HostRouteImpl otherImpl = (HostRouteImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("HostRoute [");
            boolean first = true;
        
            if (_destBridgePort != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_destBridgePort=");
                builder.append(_destBridgePort);
             }
            if (_destVtep != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_destVtep=");
                builder.append(_destVtep);
             }
            if (_ip != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ip=");
                builder.append(_ip);
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
            if (_mac != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mac=");
                builder.append(_mac);
             }
            if (_vrfCtx != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_vrfCtx=");
                builder.append(_vrfCtx);
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
