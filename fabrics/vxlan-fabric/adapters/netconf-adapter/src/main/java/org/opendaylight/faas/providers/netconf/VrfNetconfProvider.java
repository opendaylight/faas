/**
 * Copyright (c) 2017 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.providers.netconf;

import com.google.common.collect.ImmutableMap;
import com.huawei.enterprise.ce.data.CESystemInfo;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.opendaylight.faas.netconfcli.Commander;
import org.opendaylight.faas.netconfcli.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VrfNetconfProvider implements DeviceFabricService {
    private static final Logger LOG = LoggerFactory.getLogger(VrfNetconfProvider.class);
    private Commander commander;
    private final String username;
    private final String serverIP;
    private final String password;
    private final String VTEP_SOURCEIP_IF = "loopback1";

    public VrfNetconfProvider(String username, String password, String serverIP) {
        this.username = username;
        this.password = password; // TODO , we should not store this.
        this.serverIP = serverIP;
    }

    public String getServerIP() {
        return serverIP;
    }

    // TODO
    public void init() {
        commander = new Commander(username, password, serverIP);
    }

    // TODO
    public void close() {
        if (commander != null) {
            commander.close();
            commander = null;
        }
    }

    @Override
    public void createHostRoute() {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteHostRoute() {
        // TODO Auto-generated method stub

    }

    @Override
    public String createBDIF(Long bdif) {
        return commander.sendRequest(MessageBuilder.msgCreateBDIF(bdif));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.faas.providers.netconf.DeviceFabricService#assignIPtoIF(
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String assignIPtoIF(String ifname, String mask, String ipAddress) {
        return commander.sendRequest(MessageBuilder.msgAssignIPtoIF(ifname, mask, ipAddress));
    }

    @Override
    public String enableArpConfig(String ifname) {
        return commander.sendRequest(MessageBuilder.msgIfArpConfig(ifname, true, true));
    }

    @Override
    public String deleteBDIF(Long bdif) {
        return commander.sendRequest(MessageBuilder.msgDeleteBDIF(bdif));
    }

    @Override
    public String createBridgeDomain(String bdID, String desc, String vni) {
        String ret1 = commander.sendRequest(MessageBuilder.msgCreateBridgeDomain(bdID, desc));
        String ret2 = commander.sendRequest(MessageBuilder.msgBindBDtoVNI(vni, bdID));
        String ret3 = commander.sendRequest(MessageBuilder.msgCreateVtepMembers(vni));
        String ret4 = commander.sendRequest(MessageBuilder.msgCreateEVPN(bdID, true, bdID + ":" + bdID));
        String ret5 = commander.sendRequest(MessageBuilder.msgSetRT(bdID, bdID + ":1"));

        return String.join("\n", ret1, ret2, ret3, ret4, ret5);
    }

    @Override
    public String setBDRT(String bdID, String rt) {
        return commander.sendRequest(MessageBuilder.msgSetRT(bdID, rt));

    }

    @Override
    public String removeBDRT(String bdID, String rt) {
        return commander.sendRequest(MessageBuilder.msgRemoveRT(bdID, rt));

    }

    @Override
    public String deleteBridgeDomain(String bdID, String vni) {
        String ret1 = commander.sendRequest(MessageBuilder.msgDeleteBridgeDomain(bdID));
        String ret2 = commander.sendRequest(MessageBuilder.msgDeleteVtepMembers(vni));

        return String.join("\n", ret1, ret2);
    }

    @Override
    public void createVtepMembers() {
    }

    @Override
    public void deleteVtepMembers() {
        // TODO Auto-generated method stub

    }

    @Override
    public void createFabricAcl() {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteFabricAcl() {
        // TODO Auto-generated method stub

    }

    @Override
    public String createBdPort(String bdID, String ifname) {
        return commander.sendRequest(MessageBuilder.msgAddPortToBD(bdID, ifname));
    }

    @Override
    public String deleteBdPort(String bdID, String ifname) {
        String ret1 = commander.sendRequest(MessageBuilder.msgDeletePortFromBD(bdID, ifname));
        String ret2 = commander.sendRequest(MessageBuilder.msgRemoveSubIf(ifname));

        return String.join("\n", ret1, ret2);

    }

    @Override
    public void createRoute() {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteRoute() {
        // TODO Auto-generated method stub

    }

    @Override
    public void createIpMapping() {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteIpMapping() {
        // TODO Auto-generated method stub

    }

    @Override
    public String createSubIf(String port, String type, String number, String vid) {
        String ret1 = commander.sendRequest(MessageBuilder.msgCreateSubIf(port, type, number));
        String ret2 = commander.sendRequest(MessageBuilder.msgBindTagToSubIf(port + "." + number, vid));

        return String.join("\n", ret1, ret2);
    }

    @Override
    public String removeSubIf(String ifname) {
        return commander.sendRequest(MessageBuilder.msgRemoveSubIf(ifname));
    }

    @Override
    public String getSubIFTags(String ifname) {
        return commander.sendRequest(MessageBuilder.msgGetSubTags(ifname));
    }

    private String preprocess(String data) {
        return data.replaceAll("xmlns=", "xmlns1=")
            .replaceAll("content-version=\"1.0\"", "content-version=\"1\"")
            .replaceAll("format-version=\"1.0\"", "format-version=\"1\"");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.faas.providers.netconf.DeviceFabricService#getAllPorts()
     */
    @Override
    public List<String> getAllPorts() {
        String request = MessageBuilder.msgGetEthernetIfs();
        String ret = preprocess(commander.sendRequest(request));
        try {
            JAXBContext jaxbContext = JAXBContext
                    .newInstance(com.huawei.enterprise.ce.netconf.api.message.getEthernetIF.RpcReply.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(ret);
            com.huawei.enterprise.ce.netconf.api.message.getEthernetIF.RpcReply reply = (com.huawei.enterprise.ce.netconf.api.message.getEthernetIF.RpcReply) jaxbUnmarshaller
                    .unmarshal(reader);

            List<String> portList = new ArrayList<>();
            reply.getData().getEthernet().getEthernetIfs().getEthernetIf()
                    .forEach(ethernetif -> portList.add(ethernetif.getIfName()));
            return portList;

        } catch (JAXBException e) {
            LOG.error("marshal xml reply failed", e);
            return Collections.emptyList();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#
     * getNeighborInfo()
     */
    @Override
    public com.google.common.collect.ImmutableMap<String, ImmutableMap<String, String>> getNNIs() {
        String request = MessageBuilder.msgGetNNIs();
        String ret = preprocess(commander.sendRequest(request));

        com.google.common.collect.ImmutableMap.Builder<String, ImmutableMap<String, String>> linksBuilder = com.google.common.collect.ImmutableMap
                .builder();
        try {
            JAXBContext jaxbContext = JAXBContext
                    .newInstance(com.huawei.enterprise.ce.netconf.api.message.getLLDPNeighbor.RpcReply.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(ret);
            com.huawei.enterprise.ce.netconf.api.message.getLLDPNeighbor.RpcReply reply = (com.huawei.enterprise.ce.netconf.api.message.getLLDPNeighbor.RpcReply) jaxbUnmarshaller
                    .unmarshal(reader);

            reply.getData().getLldp().getLldpInterfaces().getLldpInterface().forEach((einterface) -> {
                ImmutableMap.Builder<String, String> imap = ImmutableMap.builder();
                if (einterface.getLldpNeighbors() != null) {
                    String sysId = einterface.getLldpNeighbors().getLldpNeighbor().getChassisId();
                    String portId = einterface.getLldpNeighbors().getLldpNeighbor().getPortId();
                    if (portId != null) {
                        imap.put(portId, sysId);
                        linksBuilder.put(einterface.getIfName(), imap.build());
                    }
                }
            });
            return linksBuilder.build();
        } catch (JAXBException e) {
            LOG.error("marshal xml reply failed", e);
            return linksBuilder.build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.faas.providers.netconf.DeviceFabricService#getLLDPInfo(
     * java.lang.String)
     */
    @Override
    public String getLLDPInfo(String ifname) {
        return commander.sendRequest(MessageBuilder.msgGetNeighborInfo(ifname));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#
     * getLLDPSysInfo()
     */
    @Override
    public CESystemInfo getLLDPSysInfo() {
        String request = MessageBuilder.msgGetLLDPSysInfo();
        String ret = preprocess(commander.sendRequest(request));
        try {
            JAXBContext jaxbContext = JAXBContext
                    .newInstance(com.huawei.enterprise.ce.netconf.api.message.getLLDPSysInfo.RpcReply.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(ret);
            com.huawei.enterprise.ce.netconf.api.message.getLLDPSysInfo.RpcReply reply = (com.huawei.enterprise.ce.netconf.api.message.getLLDPSysInfo.RpcReply) jaxbUnmarshaller
                    .unmarshal(reader);
            return new CESystemInfo(reply.getData().getLldp().getLldpSys().getLldpSysInformation().getSysName(),
                    reply.getData().getLldp().getLldpSys().getLldpSysInformation().getChassisId(),
                    reply.getData().getLldp().getLldpSys().getLldpSysInformation().getChassisIdSubtype());
        } catch (JAXBException e) {
            LOG.error("marshal xml reply failed", e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#
     * getVtepIPAddress()
     */

    @Override
    public String getVtepIPAddress() {
        String request = MessageBuilder.msgGetIfIPv4Address(VTEP_SOURCEIP_IF);
        String ret = preprocess(commander.sendRequest(request));
        try {
            JAXBContext jaxbContext = JAXBContext
                    .newInstance(com.huawei.enterprise.ce.netconf.api.message.getIfIPAddress.RpcReply.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(ret);
            com.huawei.enterprise.ce.netconf.api.message.getIfIPAddress.RpcReply reply = (com.huawei.enterprise.ce.netconf.api.message.getIfIPAddress.RpcReply) jaxbUnmarshaller
                    .unmarshal(reader);
            return reply.getData().getIfm().getInterfaces().getInterface().getIfmAm4().getAm4CfgAddrs().getAm4CfgAddr()
                    .getIfIpAddr();
        } catch (JAXBException e) {
            LOG.error("marshal xml reply failed", e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.faas.providers.netconf.DeviceFabricService#createVRF(
     * java.lang.String)
     */
    @Override
    public String createVRF(String vrfname) {
        return commander.sendRequest(MessageBuilder.msgCreateVRF(vrfname, vrfname));
    }

    @Override
    public String removeVRF(String vrfname) {
        return commander.sendRequest(MessageBuilder.msgRemoveVRF(vrfname));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.faas.providers.netconf.DeviceFabricService#setVRFVNI(
     * java.lang.String, java.lang.String)
     */
    @Override
    public String setVRFVNI(String vrfname, String vni) {
        return commander.sendRequest(MessageBuilder.msgBindVrf2Vni(vrfname, vni));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.faas.providers.netconf.DeviceFabricService#setVRFAfandRD
     * (java.lang.String, java.lang.String)
     */
    @Override
    public String setVRFAfandRD(String vrfname, String rd) {
        return commander.sendRequest(MessageBuilder.msgSetVrfAfandRD(vrfname, rd));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.faas.providers.netconf.DeviceFabricService#setVRFRT(java
     * .lang.String, java.lang.String)
     */
    @Override
    public String setVRFRT(String vrfname, String importValue, String exportValue) {
        return commander.sendRequest(MessageBuilder.msgSetVRFTarget(vrfname, importValue, exportValue));
    }

    @Override
    public String addVRFImportRT(String vrfname, String importValue) {
        return commander.sendRequest(MessageBuilder.msgSetVRFImportTarget(vrfname, importValue));
    }



    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.faas.providers.netconf.DeviceFabricService#bindIFtoVRF(
     * java.lang.String, java.lang.String)
     */
    @Override
    public String bindIFtoVRF(String ifname, String vrfname, String ip, String mask) {
        String req = MessageBuilder.msgBindIFtoVRF(ifname, vrfname, ip, mask);
        System.out.print(req);
        return commander.sendRequest(req);
    }

    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#createPolicyForVRF(java.lang.String, boolean)
     */
    @Override
    public String createPolicyForVRF(String vrfName, boolean isForImport) {
        return commander.sendRequest(MessageBuilder.msgCreateRoutePolicy(vrfName, isForImport));
    }

    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#createIPv4Prefix(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String createIPv4PrefixNode(String ipPrefixName, String nodeIndex ,String ipaddress, int masklen) {
        return commander.sendRequest(MessageBuilder.msgCreateIPv4PrefixFilterNode(ipPrefixName, nodeIndex, ipaddress, masklen));
    }

    @Override
    public String createIPv4Prefix(String ipPrefixName) {
        return commander.sendRequest(MessageBuilder.msgCreateIPv4PrefixFilter(ipPrefixName));
    }

    @Override
    public String removeIPv4Prefix(String ipPrefixName) {
        return commander.sendRequest(MessageBuilder.msgRemoveIPv4PrefixFilter(ipPrefixName));
    }



    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#addPolicyEntry(java.lang.String, java.lang.String, int)
     */
    @Override
    public String addImportRoutePolicyEntry(String vrfName, String ipPrefixName, String nodeIndex) {
        String req = MessageBuilder.msgCreateImportRoutePolicyEntry(vrfName, ipPrefixName, nodeIndex);
        return commander.sendRequest(req);
    }

    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#allowDirectRoute(java.lang.String)
     */
    @Override
    public String allowDirectRoute(String vrfName) {
        String request = MessageBuilder.msgImportDirectRouteByBGP(vrfName);
        System.out.println(request);
        return commander.sendRequest(request);

    }


    @Override
    public String deleteDirectRoute(String vrfName) {
        String request = MessageBuilder.msgDeleteDirectRouteByBGP(vrfName);
        return commander.sendRequest(request);
    }


    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#getRouteImportFromBGP(java.lang.String)
     */
    @Override
    public String getRouteImportFromBGP(String vrfName) {
        String req = MessageBuilder.msgGetBGPVRFRouteImport(vrfName);

        return commander.sendRequest(req);
    }

    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#addIPv4FamilyinBGP(java.lang.String)
     */
    @Override
    public String addIPv4FamilyinBGP(String vrfName) {
        String req = MessageBuilder.msgAddIPv4FamilyInBGP(vrfName);
         System.out.println(req);
        return commander.sendRequest(req);
    }

    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#deleteIPv4FamilyinBGP(java.lang.String)
     */
    @Override
    public String deleteIPv4FamilyinBGP(String vrfName) {
        String req = MessageBuilder.msgDeleteIPv4FamilyInBGP(vrfName);

        return commander.sendRequest(req);
    }

    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#removeIPv4PrefixNode(java.lang.String, java.lang.String, int)
     */
    @Override
    public String removeIPv4PrefixNode(String ipPrefixName, String nodeIndex, String ipaddress, int masklen) {
        return commander.sendRequest(MessageBuilder.msgRemoveIPv4PrefixFilterNode(ipPrefixName, nodeIndex, ipaddress, masklen));
    }

    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#deletePolicyEntry(java.lang.String, java.lang.String)
     */
    @Override
    public String deleteImportRoutePolicyEntry(String vrfName, String prefixName, String nodeIndex) {
        return commander.sendRequest(MessageBuilder.msgRemoveImportRoutePolicyEntry(vrfName, prefixName, nodeIndex));
    }

    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#addRoutePolicy(java.lang.String)
     */
    @Override
    public String addRoutePolicy(String vrfName, boolean isExport) {
        return commander.sendRequest(MessageBuilder.msgCreateRoutePolicy(vrfName, isExport));
    }

    /* (non-Javadoc)
     * @see org.opendaylight.faas.providers.netconf.DeviceFabricService#deleteRoutePolicy(java.lang.String)
     */
    @Override
    public String deleteRoutePolicy(String vrfName, boolean isExport) {
        // TODO Auto-generated method stub
        return commander.sendRequest(MessageBuilder.msgRemoveRoutePolicy(vrfName, isExport));
    }
}
