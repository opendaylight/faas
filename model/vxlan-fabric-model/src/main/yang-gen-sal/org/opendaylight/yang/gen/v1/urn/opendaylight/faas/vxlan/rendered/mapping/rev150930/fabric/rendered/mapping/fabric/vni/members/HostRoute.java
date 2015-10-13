package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Identifiable;


/**
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;fabric-vxlan-rendered-mapping&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF\yang\fabric-vxlan-rendered-mapping.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * list host-route {
 *     key "mac"
 *     leaf mac {
 *         type mac-address;
 *     }
 *     leaf vrf-ctx {
 *         type uint32;
 *     }
 *     leaf ip {
 *         type ip-address;
 *     }
 *     leaf dest-vtep {
 *         type ip-address;
 *     }
 *     leaf dest-bridge-port {
 *         type uuid;
 *     }
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;fabric-vxlan-rendered-mapping/fabric-rendered-mapping/fabric/vni-members/host-route&lt;/i&gt;
 * 
 * &lt;p&gt;To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRouteBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRouteBuilder
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRouteKey
 *
 */
public interface HostRoute
    extends
    ChildOf<VniMembers>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.HostRoute>,
    Identifiable<HostRouteKey>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.cachedReference(org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:faas:vxlan:rendered:mapping","2015-09-30","host-route"));

    MacAddress getMac();
    
    java.lang.Long getVrfCtx();
    
    IpAddress getIp();
    
    IpAddress getDestVtep();
    
    Uuid getDestBridgePort();
    
    /**
     * Returns Primary Key of Yang List Type
     *
     */
    HostRouteKey getKey();

}

