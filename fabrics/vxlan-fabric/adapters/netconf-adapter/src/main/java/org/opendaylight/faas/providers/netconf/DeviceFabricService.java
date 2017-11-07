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
import java.util.List;

public interface DeviceFabricService {

    List<String> getAllPorts();
    com.google.common.collect.ImmutableMap<String, ImmutableMap<String, String>> getNNIs();
    String getLLDPInfo(String ifname);
    CESystemInfo getLLDPSysInfo();
    String getVtepIPAddress();

    void createHostRoute();
    void deleteHostRoute();
    String addIPv4FamilyinBGP(String vrfName);
    String deleteIPv4FamilyinBGP(String vrfName);
    String allowDirectRoute(String vrfName);
    String deleteDirectRoute(String vrfName);
    String getRouteImportFromBGP(String vrfName);
    String createBDIF(Long bdIF);
    String createVRF(String vrfname);
    String removeVRF(String vrfname);
    String setVRFVNI(String vrfname, String vni);
    String createPolicyForVRF(String vrfName, boolean isForImport);
    String addRoutePolicy(String vrfName, boolean isExport);
    String deleteRoutePolicy(String vrfName, boolean isExport);
    String addImportRoutePolicyEntry(String vrfName, String prefixName, String nodeIndex);
    String deleteImportRoutePolicyEntry(String vrfName, String prefixName, String nodeIndex);
    String createIPv4Prefix(String ipPrefixName);
    String removeIPv4Prefix(String ipPrefixName);
    String createIPv4PrefixNode(String ipPrefixName, String nodeIndex, String ipaddress, int masklen);
    String removeIPv4PrefixNode(String ipPrefixName, String nodeIndex, String ipaddress, int masklen);
    String setVRFAfandRD(String vrfname, String rd);
    String setVRFRT(String vrfname, String exportValue, String importValue);
    String addVRFImportRT(String vrfname, String importValue);
    String bindIFtoVRF(String vrfname, String ifname, String ip, String mask);
    String assignIPtoIF(String ifname, String mask, String ipAddress);
    String enableArpConfig(String ifname);
    String deleteBDIF(Long bdIF);
    /**
     *
     * @param bdID
     * @param desc
     */
    String createBridgeDomain(String bdID, String desc, String vni);
    public String setBDRT(String bdID, String rt);
    public String removeBDRT(String bdID, String rt);
    String deleteBridgeDomain(String bdID, String vni) ;
    String createSubIf(String port, String type, String number, String vid);
    String removeSubIf(String ifname) ;
    String createBdPort(String bdID, String ifname);
    String deleteBdPort(String bdID, String ifname);
    String getSubIFTags(String ifname);

    void createVtepMembers();
    void deleteVtepMembers();
    void createFabricAcl();
    void deleteFabricAcl();
    void createRoute();
    void deleteRoute();
    void createIpMapping();
    void deleteIpMapping();

}
