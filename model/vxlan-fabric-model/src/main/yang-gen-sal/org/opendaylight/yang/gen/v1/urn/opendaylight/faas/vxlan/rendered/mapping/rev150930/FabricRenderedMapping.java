package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.common.QName;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;fabric-vxlan-rendered-mapping&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF\yang\fabric-vxlan-rendered-mapping.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * container fabric-rendered-mapping {
 *     list fabric {
 *         key "id"
 *         leaf id {
 *             type fabric-id;
 *         }
 *         list vni-members {
 *             key "vni"
 *             leaf vni {
 *                 type uint32;
 *             }
 *             leaf-list vteps {
 *                 type ip-address;
 *             }
 *             list host-route {
 *                 key "mac"
 *                 leaf mac {
 *                     type mac-address;
 *                 }
 *                 leaf vrf-ctx {
 *                     type uint32;
 *                 }
 *                 leaf ip {
 *                     type ip-address;
 *                 }
 *                 leaf dest-vtep {
 *                     type ip-address;
 *                 }
 *                 leaf dest-bridge-port {
 *                     type uuid;
 *                 }
 *             }
 *         }
 *     }
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;fabric-vxlan-rendered-mapping/fabric-rendered-mapping&lt;/i&gt;
 * 
 * &lt;p&gt;To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMappingBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMappingBuilder
 *
 */
public interface FabricRenderedMapping
    extends
    ChildOf<FabricVxlanRenderedMappingData>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.cachedReference(org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:faas:vxlan:rendered:mapping","2015-09-30","fabric-rendered-mapping"));

    List<Fabric> getFabric();

}

