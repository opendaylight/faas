//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.03.21 at 12:14:54 PM EDT 
//


package com.huawei.enterprise.ce.netconf.api.message.getLLDPInfo;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.huawei.enterprise.ce.netconf.api.message.getLLDPInfo package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.huawei.enterprise.ce.netconf.api.message.getLLDPInfo
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RpcReply }
     * 
     */
    public RpcReply createRpcReply() {
        return new RpcReply();
    }

    /**
     * Create an instance of {@link RpcReply.Data }
     * 
     */
    public RpcReply.Data createRpcReplyData() {
        return new RpcReply.Data();
    }

    /**
     * Create an instance of {@link RpcReply.Data.Lldp }
     * 
     */
    public RpcReply.Data.Lldp createRpcReplyDataLldp() {
        return new RpcReply.Data.Lldp();
    }

    /**
     * Create an instance of {@link RpcReply.Data.Lldp.LldpInterfaces }
     * 
     */
    public RpcReply.Data.Lldp.LldpInterfaces createRpcReplyDataLldpLldpInterfaces() {
        return new RpcReply.Data.Lldp.LldpInterfaces();
    }

    /**
     * Create an instance of {@link RpcReply.Data.Lldp.LldpInterfaces.LldpInterface }
     * 
     */
    public RpcReply.Data.Lldp.LldpInterfaces.LldpInterface createRpcReplyDataLldpLldpInterfacesLldpInterface() {
        return new RpcReply.Data.Lldp.LldpInterfaces.LldpInterface();
    }

    /**
     * Create an instance of {@link RpcReply.Data.Lldp.LldpInterfaces.LldpInterface.LldpIfInformation }
     * 
     */
    public RpcReply.Data.Lldp.LldpInterfaces.LldpInterface.LldpIfInformation createRpcReplyDataLldpLldpInterfacesLldpInterfaceLldpIfInformation() {
        return new RpcReply.Data.Lldp.LldpInterfaces.LldpInterface.LldpIfInformation();
    }

}
