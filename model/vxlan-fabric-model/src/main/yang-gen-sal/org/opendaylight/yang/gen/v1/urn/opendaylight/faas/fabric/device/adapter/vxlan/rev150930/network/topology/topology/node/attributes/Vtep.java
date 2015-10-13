package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.Attributes1;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;fabric-vxlan-device-adapter&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF\yang\fabric-vxlan-device-adapter.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * container vtep {
 *     leaf tp-id {
 *         type tp-ref;
 *     }
 *     leaf ip {
 *         type ip-address;
 *     }
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;fabric-vxlan-device-adapter/network-topology/topology/node/(urn:opendaylight:faas:fabric:capable:device?revision=2015-09-30)attributes/(urn:opendaylight:faas:fabric:device:adapter:vxlan?revision=2015-09-30)vtep&lt;/i&gt;
 * 
 * &lt;p&gt;To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.VtepBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.VtepBuilder
 *
 */
public interface Vtep
    extends
    ChildOf<Attributes1>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.cachedReference(org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:faas:fabric:device:adapter:vxlan","2015-09-30","vtep"));

    TpId getTpId();
    
    IpAddress getIp();

}

