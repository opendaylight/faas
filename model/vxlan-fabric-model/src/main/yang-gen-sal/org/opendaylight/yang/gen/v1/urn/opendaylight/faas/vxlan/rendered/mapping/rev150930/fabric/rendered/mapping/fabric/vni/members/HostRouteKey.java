package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;


public class HostRouteKey
 implements Identifier<HostRoute> {
    private static final long serialVersionUID = -5459444930931185728L;
    private final MacAddress _mac;


    public HostRouteKey(MacAddress _mac) {
    
    
        this._mac = _mac;
    }
    
    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public HostRouteKey(HostRouteKey source) {
        this._mac = source._mac;
    }


    public MacAddress getMac() {
        return _mac;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_mac == null) ? 0 : _mac.hashCode());
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
        HostRouteKey other = (HostRouteKey) obj;
        if (_mac == null) {
            if (other._mac != null) {
                return false;
            }
        } else if(!_mac.equals(other._mac)) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRouteKey.class.getSimpleName()).append(" [");
        boolean first = true;
    
        if (_mac != null) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("_mac=");
            builder.append(_mac);
         }
        return builder.append(']').toString();
    }



}

