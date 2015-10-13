package org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930;
import org.opendaylight.yangtools.yang.binding.DataRoot;


/**
 * This module contains a collection of YANG definitions for Fabric.
 * 
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;fabric-vxlan-rendered-mapping&lt;/b&gt;
 * &lt;br&gt;Source path: &lt;i&gt;META-INF\yang\fabric-vxlan-rendered-mapping.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * module fabric-vxlan-rendered-mapping {
 *     yang-version 1;
 *     namespace "urn:opendaylight:faas:vxlan:rendered:mapping";
 *     prefix "mapping";
 * 
 *     import ietf-inet-types { prefix "inet"; }
 *     
 *     import fabric { prefix "fabric"; }
 *     
 *     import ietf-yang-types { prefix "yang"; }
 *     revision 2015-09-30 {
 *         description "This module contains a collection of YANG definitions for Fabric.
 *         ";
 *     }
 * 
 *     container fabric-rendered-mapping {
 *         list fabric {
 *             key "id"
 *             leaf id {
 *                 type fabric-id;
 *             }
 *             list vni-members {
 *                 key "vni"
 *                 leaf vni {
 *                     type uint32;
 *                 }
 *                 leaf-list vteps {
 *                     type ip-address;
 *                 }
 *                 list host-route {
 *                     key "mac"
 *                     leaf mac {
 *                         type mac-address;
 *                     }
 *                     leaf vrf-ctx {
 *                         type uint32;
 *                     }
 *                     leaf ip {
 *                         type ip-address;
 *                     }
 *                     leaf dest-vtep {
 *                         type ip-address;
 *                     }
 *                     leaf dest-bridge-port {
 *                         type uuid;
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * &lt;/pre&gt;
 *
 */
public interface FabricVxlanRenderedMappingData
    extends
    DataRoot
{




    FabricRenderedMapping getFabricRenderedMapping();

}

