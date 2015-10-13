package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.concepts.Builder;
import java.math.BigInteger;
import java.util.List;
import com.google.common.collect.Range;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1
 *
 */
public class BridgeDomain1Builder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1> {

    private java.lang.Long _vni;


    public BridgeDomain1Builder() {
    }

    public BridgeDomain1Builder(BridgeDomain1 base) {
        this._vni = base.getVni();
    }


    public java.lang.Long getVni() {
        return _vni;
    }

    private static void checkVniRange(final long value) {
        if (value >= 0L && value <= 4294967295L) {
            return;
        }
        throw new IllegalArgumentException(String.format("Invalid range: %s, expected: createRangeString(constraints).", value));
    }
    
    public BridgeDomain1Builder setVni(java.lang.Long value) {
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

    public BridgeDomain1 build() {
        return new BridgeDomain1Impl(this);
    }

    private static final class BridgeDomain1Impl implements BridgeDomain1 {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1.class;
        }

        private final java.lang.Long _vni;


        private BridgeDomain1Impl(BridgeDomain1Builder base) {
            this._vni = base.getVni();
        }

        @Override
        public java.lang.Long getVni() {
            return _vni;
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
            result = prime * result + ((_vni == null) ? 0 : _vni.hashCode());
        
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1 other = (org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1)obj;
            if (_vni == null) {
                if (other.getVni() != null) {
                    return false;
                }
            } else if(!_vni.equals(other.getVni())) {
                return false;
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("BridgeDomain1 [");
            boolean first = true;
        
            if (_vni != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_vni=");
                builder.append(_vni);
             }
            return builder.append(']').toString();
        }
    }

}
