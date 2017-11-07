/**
 *
 */
package org.opendaylight.faas.fabric.vxlan.adapter.ce;

/**
 * @author xingjun
 *
 */
public class VniOutOfScopeException extends Exception {
     public VniOutOfScopeException() {

     }

     public VniOutOfScopeException(String message) {
         super(message);
     }

}
