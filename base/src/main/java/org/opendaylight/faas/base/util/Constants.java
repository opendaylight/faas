/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.util;

public class Constants {

    /**
     * This class is not instantiable, it includes constant definition all most of
     * constant values.
     */
    private Constants() {}

    public static final String JXB_BINDING_CONTEXT = "com.huawei.sdn.controller.common.xmlbinding";

    public static final String MORE_LINE = "[ ]{2}[-]{4} More [-]{4}\\[42D[ \\t]+\\[42D";
    public static final String MORE_LINE_MARKER = "---- More ----";
    public static final int MAXIMUM_QUEUE_LENGTH = 100;
    public static final String SNMP_V3_USM_PASSWORD = "Huawei-1234.";

    // "Management Information Base module for LLDP configuration,
    // statistics, local system data and remote systems data
    // components.
    // Copyright (C) IEEE (2005). This version of this MIB module
    // is published as subclause 12.1 of IEEE Std 802.1AB.-2005;
    // see the standard itself for full legal notices."
    // REVISION "200505060000Z" -- May 06, 2005
    // DESCRIPTION
    // "Published as part of IEEE Std 802.1AB-2005 initial version."
    // ::= { iso std(0) iso8802(8802) ieee802dot1(1) ieee802dot1mibs(1) 2 }

    // lldpNotifications OBJECT IDENTIFIER ::= { lldpMIB 0 }
    // lldpObjects OBJECT IDENTIFIER ::= { lldpMIB 1 }
    // lldpConformance OBJECT IDENTIFIER ::= { lldpMIB 2 }

    // 1.0.8802.1.1.2 - LLDP MIB
    // 0 - notification

    public static final String oidLldpMib = "1.0.8802.1.1.2";
    public static final String oidLldpRemTablesChange = "1.0.8802.1.1.2.0.0.1";

    // 1 - lldp objects
    public static final String oidLldpMessageTxInterval = "1.0.8802.1.1.2.1.1.1.0";
    public static final String oidLldpMessageTxHoldMultiplier = "1.0.8802.1.1.2.1.1.2.0";
    public static final String oidLldpReinitDelay = "1.0.8802.1.1.2.1.1.3.0";
    public static final String oidLldpTxDelay = "1.0.8802.1.1.2.1.1.4.0";
    public static final String oidLldpNotificationInterval = "1.0.8802.1.1.2.1.1.5.0";
    public static final String oidLldpPortConfigTable = "1.0.8802.1.1.2.1.1.6";
    public static final String oidLldpPortConfigEntry = "1.0.8802.1.1.2.1.1.6.1";
    public static final String oidLldpPortConfigPortNum = "1.0.8802.1.1.2.1.1.6.1.1";
    public static final String colLldpPortConfigPortNum = "1";
    public static final String oidLldpPortConfigAdminStatus = "1.0.8802.1.1.2.1.1.6.1.2";
    public static final String colLldpPortConfigAdminStatus = "2";
    public static final String oidLldpPortConfigNotificationEnable = "1.0.8802.1.1.2.1.1.6.1.3";
    public static final String colLldpPortConfigNotificationEnable = "3";
    public static final String oidLldpPortConfigTLVsTxEnable = "1.0.8802.1.1.2.1.1.6.1.4";
    public static final String colLldpPortConfigTLVsTxEnable = "4";
    public static final String oidLldpConfigManAddrTable = "1.0.8802.1.1.2.1.1.7";
    public static final String oidLldpConfigManAddrEntry = "1.0.8802.1.1.2.1.1.7.1";
    public static final String oidLldpConfigManAddrPortsTxEnable = "1.0.8802.1.1.2.1.1.7.1.1";
    public static final String colLldpConfigManAddrPortsTxEnable = "1";

    // lldp statistics
    public static final String oidLldpStatsRemTablesLastChangeTime = "1.0.8802.1.1.2.1.2.1.0";
    public static final String oidLldpStatsRemTablesInserts = "1.0.8802.1.1.2.1.2.2.0";
    public static final String oidLldpStatsRemTablesDeletes = "1.0.8802.1.1.2.1.2.3.0";
    public static final String oidLldpStatsRemTablesDrops = "1.0.8802.1.1.2.1.2.4.0";
    public static final String oidLldpStatsRemTablesAgeouts = "1.0.8802.1.1.2.1.2.5.0";
    public static final String oidLldpStatsTxPortTable = "1.0.8802.1.1.2.1.2.6";
    public static final String oidLldpStatsTxPortEntry = "1.0.8802.1.1.2.1.2.6.1";
    public static final String oidLldpStatsTxPortNum = "1.0.8802.1.1.2.1.2.6.1.1";
    public static final String colLldpStatsTxPortNum = "1";
    public static final String oidLldpStatsTxPortFramesTotal = "1.0.8802.1.1.2.1.2.6.1.2";
    public static final String colLldpStatsTxPortFramesTotal = "2";
    public static final String oidLldpStatsRxPortTable = "1.0.8802.1.1.2.1.2.7";
    public static final String oidLldpStatsRxPortEntry = "1.0.8802.1.1.2.1.2.7.1";
    public static final String oidLldpStatsRxPortNum = "1.0.8802.1.1.2.1.2.7.1.1";
    public static final String colLldpStatsRxPortNum = "1";
    public static final String oidLldpStatsRxPortFramesDiscardedTotal = "1.0.8802.1.1.2.1.2.7.1.2";
    public static final String colLldpStatsRxPortFramesDiscardedTotal = "2";
    public static final String oidLldpStatsRxPortFramesErrors = "1.0.8802.1.1.2.1.2.7.1.3";
    public static final String colLldpStatsRxPortFramesErrors = "3";
    public static final String oidLldpStatsRxPortFramesTotal = "1.0.8802.1.1.2.1.2.7.1.4";
    public static final String colLldpStatsRxPortFramesTotal = "4";
    public static final String oidLldpStatsRxPortTLVsDiscardedTotal = "1.0.8802.1.1.2.1.2.7.1.5";
    public static final String colLldpStatsRxPortTLVsDiscardedTotal = "5";
    public static final String oidLldpStatsRxPortTLVsUnrecognizedTotal = "1.0.8802.1.1.2.1.2.7.1.6";
    public static final String colLldpStatsRxPortTLVsUnrecognizedTotal = "6";
    public static final String oidLldpStatsRxPortAgeoutsTotal = "1.0.8802.1.1.2.1.2.7.1.7";
    public static final String colLldpStatsRxPortAgeoutsTotal = "7";

    // Local Data
    public static final String oidLldpLocChassisIdSubtype = "1.0.8802.1.1.2.1.3.1.0";
    public static final String oidLldpLocChassisId = "1.0.8802.1.1.2.1.3.2.0";
    public static final String oidLldpLocSysName = "1.0.8802.1.1.2.1.3.3";
    public static final String oidLldpLocSysDesc = "1.0.8802.1.1.2.1.3.4.0";
    public static final String oidLldpLocSysCapSupported = "1.0.8802.1.1.2.1.3.5.0";
    public static final String oidLldpLocSysCapEnabled = "1.0.8802.1.1.2.1.3.6.0";
    public static final String oidLldpLocPortTable = "1.0.8802.1.1.2.1.3.7";
    public static final String oidLldpLocPortEntry = "1.0.8802.1.1.2.1.3.7.1";
    public static final String oidLldpLocPortNum = "1.0.8802.1.1.2.1.3.7.1.1";
    public static final String colLldpLocPortNum = "1";
    public static final String oidLldpLocPortIdSubtype = "1.0.8802.1.1.2.1.3.7.1.2";
    public static final String colLldpLocPortIdSubtype = "2";
    public static final String oidLldpLocPortId = "1.0.8802.1.1.2.1.3.7.1.3";
    public static final String colLldpLocPortId = "3";
    public static final String oidLldpLocPortDesc = "1.0.8802.1.1.2.1.3.7.1.4";
    public static final String colLldpLocPortDesc = "4";
    public static final String oidLldpLocManAddrTable = "1.0.8802.1.1.2.1.3.8";
    public static final String oidLldpLocManAddrEntry = "1.0.8802.1.1.2.1.3.8.1";
    public static final String oidLldpLocManAddrSubtype = "1.0.8802.1.1.2.1.3.8.1.1";
    public static final String colLldpLocManAddrSubtype = "1";
    public static final String oidLldpLocManAddr = "1.0.8802.1.1.2.1.3.8.1.2";
    public static final String colLldpLocManAddr = "2";
    public static final String oidLldpLocManAddrLen = "1.0.8802.1.1.2.1.3.8.1.3";
    public static final String colLldpLocManAddrLen = "3";
    public static final String oidLldpLocManAddrIfSubtype = "1.0.8802.1.1.2.1.3.8.1.4";
    public static final String colLldpLocManAddrIfSubtype = "4";
    public static final String oidLldpLocManAddrIfId = "1.0.8802.1.1.2.1.3.8.1.5";
    public static final String colLldpLocManAddrIfId = "5";
    public static final String oidLldpLocManAddrOID = "1.0.8802.1.1.2.1.3.8.1.6";
    public static final String colLldpLocManAddrOID = "6";

    // Remote data
    public static final String oidLldpRemTable = "1.0.8802.1.1.2.1.4.1";
    public static final String oidLldpRemEntry = "1.0.8802.1.1.2.1.4.1.1";
    public static final String oidLldpRemTimeMark = "1.0.8802.1.1.2.1.4.1.1.1";
    public static final String colLldpRemTimeMark = "1";
    public static final String oidLldpRemLocalPortNum = "1.0.8802.1.1.2.1.4.1.1.2";
    public static final String colLldpRemLocalPortNum = "2";
    public static final String oidLldpRemIndex = "1.0.8802.1.1.2.1.4.1.1.3";
    public static final String colLldpRemIndex = "3";
    public static final String oidLldpRemChassisIdSubtype = "1.0.8802.1.1.2.1.4.1.1.4";
    public static final String colLldpRemChassisIdSubtype = "4";
    public static final String oidLldpRemChassisId = "1.0.8802.1.1.2.1.4.1.1.5";
    public static final String colLldpRemChassisId = "5";
    public static final String oidLldpRemPortIdSubtype = "1.0.8802.1.1.2.1.4.1.1.6";
    public static final String colLldpRemPortIdSubtype = "6";
    public static final String oidLldpRemPortId = "1.0.8802.1.1.2.1.4.1.1.7";
    public static final String colLldpRemPortId = "7";
    public static final String oidLldpRemPortDesc = "1.0.8802.1.1.2.1.4.1.1.8";
    public static final String colLldpRemPortDesc = "8";
    public static final String oidLldpRemSysName = "1.0.8802.1.1.2.1.4.1.1.9";
    public static final String colLldpRemSysName = "9";
    public static final String oidLldpRemSysDesc = "1.0.8802.1.1.2.1.4.1.1.10";
    public static final String colLldpRemSysDesc = "10";
    public static final String oidLldpRemSysCapSupported = "1.0.8802.1.1.2.1.4.1.1.11";
    public static final String colLldpRemSysCapSupported = "11";
    public static final String oidLldpRemSysCapEnabled = "1.0.8802.1.1.2.1.4.1.1.12";
    public static final String colLldpRemSysCapEnabled = "12";
    public static final String oidLldpRemManAddrTable = "1.0.8802.1.1.2.1.4.2";
    public static final String oidLldpRemManAddrEntry = "1.0.8802.1.1.2.1.4.2.1";
    public static final String oidLldpRemManAddrSubtype = "1.0.8802.1.1.2.1.4.2.1.1";
    public static final String colLldpRemManAddrSubtype = "1";
    public static final String oidLldpRemManAddr = "1.0.8802.1.1.2.1.4.2.1.2";
    public static final String colLldpRemManAddr = "2";
    public static final String oidLldpRemManAddrIfSubtype = "1.0.8802.1.1.2.1.4.2.1.3";
    public static final String colLldpRemManAddrIfSubtype = "3";
    public static final String oidLldpRemManAddrIfId = "1.0.8802.1.1.2.1.4.2.1.4";
    public static final String colLldpRemManAddrIfId = "4";
    public static final String oidLldpRemManAddrOID = "1.0.8802.1.1.2.1.4.2.1.5";
    public static final String colLldpRemManAddrOID = "5";
    public static final String oidLldpRemUnknownTLVTable = "1.0.8802.1.1.2.1.4.3";
    public static final String oidLldpRemUnknownTLVEntry = "1.0.8802.1.1.2.1.4.3.1";
    public static final String oidLldpRemUnknownTLVType = "1.0.8802.1.1.2.1.4.3.1.1";
    public static final String colLldpRemUnknownTLVType = "1";
    public static final String oidLldpRemUnknownTLVInfo = "1.0.8802.1.1.2.1.4.3.1.2";
    public static final String colLldpRemUnknownTLVInfo = "2";
    public static final String oidLldpRemOrgDefInfoTable = "1.0.8802.1.1.2.1.4.4";
    public static final String oidLldpRemOrgDefInfoEntry = "1.0.8802.1.1.2.1.4.4.1";
    public static final String oidLldpRemOrgDefInfoOUI = "1.0.8802.1.1.2.1.4.4.1.1";
    public static final String colLldpRemOrgDefInfoOUI = "1";
    public static final String oidLldpRemOrgDefInfoSubtype = "1.0.8802.1.1.2.1.4.4.1.2";
    public static final String colLldpRemOrgDefInfoSubtype = "2";
    public static final String oidLldpRemOrgDefInfoIndex = "1.0.8802.1.1.2.1.4.4.1.3";
    public static final String colLldpRemOrgDefInfoIndex = "3";
    public static final String oidLldpRemOrgDefInfo = "1.0.8802.1.1.2.1.4.4.1.4";
    public static final String colLldpRemOrgDefInfo = "4";

    /*
     * public static final String nLldpPortConfigAdminStatus 0
     * public static final String cLldpPortConfigAdminStatus 2
     * public static final String nLldpPortConfigNotificationEnable 1
     * public static final String cLldpPortConfigNotificationEnable 3
     * public static final String nLldpPortConfigTLVsTxEnable 2
     * public static final String cLldpPortConfigTLVsTxEnable 4
     * public static final String nLldpConfigManAddrPortsTxEnable 0
     * public static final String cLldpConfigManAddrPortsTxEnable 1
     * public static final String nLldpStatsTxPortFramesTotal 0
     * public static final String cLldpStatsTxPortFramesTotal 2
     * public static final String nLldpStatsRxPortFramesDiscardedTotal 0
     * public static final String cLldpStatsRxPortFramesDiscardedTotal 2
     * public static final String nLldpStatsRxPortFramesErrors 1
     * public static final String cLldpStatsRxPortFramesErrors 3
     * public static final String nLldpStatsRxPortFramesTotal 2
     * public static final String cLldpStatsRxPortFramesTotal 4
     * public static final String nLldpStatsRxPortTLVsDiscardedTotal 3
     * public static final String cLldpStatsRxPortTLVsDiscardedTotal 5
     * public static final String nLldpStatsRxPortTLVsUnrecognizedTotal 4
     * public static final String cLldpStatsRxPortTLVsUnrecognizedTotal 6
     * public static final String nLldpStatsRxPortAgeoutsTotal 5
     * public static final String cLldpStatsRxPortAgeoutsTotal 7
     * public static final String nLldpLocPortIdSubtype 0
     * public static final String cLldpLocPortIdSubtype 2
     * public static final String nLldpLocPortId 1
     * public static final String cLldpLocPortId 3
     * public static final String nLldpLocPortDesc 2
     * public static final String cLldpLocPortDesc 4
     * public static final String nLldpLocManAddrLen 0
     * public static final String cLldpLocManAddrLen 3
     * public static final String nLldpLocManAddrIfSubtype 1
     * public static final String cLldpLocManAddrIfSubtype 4
     * public static final String nLldpLocManAddrIfId 2
     * public static final String cLldpLocManAddrIfId 5
     * public static final String nLldpLocManAddrOID 3
     * public static final String cLldpLocManAddrOID 6
     * public static final String nLldpRemChassisIdSubtype 0
     * public static final String cLldpRemChassisIdSubtype 4
     * public static final String nLldpRemChassisId 1
     * public static final String cLldpRemChassisId 5
     * public static final String nLldpRemPortIdSubtype 2
     * public static final String cLldpRemPortIdSubtype 6
     * public static final String nLldpRemPortId 3
     * public static final String cLldpRemPortId 7
     * public static final String nLldpRemPortDesc 4
     * public static final String cLldpRemPortDesc 8
     * public static final String nLldpRemSysName 5
     * public static final String cLldpRemSysName 9
     * public static final String nLldpRemSysDesc 6
     * public static final String cLldpRemSysDesc 10
     * public static final String nLldpRemSysCapSupported 7
     * public static final String cLldpRemSysCapSupported 11
     * public static final String nLldpRemSysCapEnabled 8
     * public static final String cLldpRemSysCapEnabled 12
     * public static final String nLldpRemManAddrIfSubtype 0
     * public static final String cLldpRemManAddrIfSubtype 3
     * public static final String nLldpRemManAddrIfId 1
     * public static final String cLldpRemManAddrIfId 4
     * public static final String nLldpRemManAddrOID 2
     * public static final String cLldpRemManAddrOID 5
     * public static final String nLldpRemUnknownTLVInfo 0
     * public static final String cLldpRemUnknownTLVInfo 2
     * public static final String nLldpRemOrgDefInfo 0
     * public static final String cLldpRemOrgDefInfo 4
     */

    public static final String oiDSNMPMIB2IfTable = "1.3.6.1.2.1.2.2";
    public static final String oiDSNMPMIB2IfName = "1.3.6.1.2.1.31.1.1.1.1";
    public static final String oidIPAddressEntry = "1.3.6.1.2.1.4.20.1.1";
    public static final String oidIfDescIndex = "1.3.6.1.2.1.4.20.1.2";
    public static final String oidIfTable = "1.3.6.1.2.1.2.2.1.2";

    // public static final String oidHWLSWVLAN = "1.3.6.1.4.1.2011.2.23.1.2.2.15";
    // public static final String oidHWH3CVLAN = "1.3.6.1.4.1.2011.2.23.10.2.16.1.5";
    // public static final String oidHWVLAN = "1.3.6.1.4.1.2011.1.3.3.3.1";
    public static final String oidHWVLANList = "1.3.6.1.4.1.2011.5.25.42.3.1.1.1.1.2";
    public static final String oidHWVLANPortList = "1.3.6.1.4.1.2011.5.25.42.3.1.1.1.1.3";
    public static final String oidIFList = "1.3.6.1.2.1.2.2.1.2";
    public static final int telnet_port = 23;
    public static final int sshPort_c = 22;
    public static final String magic_cookie = "z9hG4bK";

    // STP
    public static final String oiddot1dStpPortState = "1.3.6.1.2.1.17.2.15.1.3";
    public static final String oiddot1dStpPortEntry = "1.3.6.1.2.1.17.2.15.1";
    public static final String oiddot1dStpPortTable = "1.3.6.1.2.1.17.2.15";

}
