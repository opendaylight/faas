package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric;
import org.opendaylight.yangtools.yang.binding.Identifier;


public class VniMembersKey
 implements Identifier<VniMembers> {
    private static final long serialVersionUID = 7499433306930832695L;
    private final java.lang.Long _vni;


    public VniMembersKey(java.lang.Long _vni) {
    
    
        this._vni = _vni;
    }
    
    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public VniMembersKey(VniMembersKey source) {
        this._vni = source._vni;
    }


    public java.lang.Long getVni() {
        return _vni;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_vni == null) ? 0 : _vni.hashCode());
        return result;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VniMembersKey other = (VniMembersKey) obj;
        if (_vni == null) {
            if (other._vni != null) {
                return false;
            }
        } else if(!_vni.equals(other._vni)) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembersKey.class.getSimpleName()).append(" [");
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

