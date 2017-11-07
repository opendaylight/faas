/**
 * Copyright (c) 2017 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.netconfcli;

/**
 * MessageBuilder - build NetConf API payload.
 * @author xingjun
 *
 */
public class MessageBuilder {
    private static final String EDIT_CONFIG_HEADER = String.join("\n"
             , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
             , "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"801\">"
             , "<edit-config>"
             , "<target>"
             , "<running/>"
             ,     "</target>"
             ,     "<default-operation>merge</default-operation>"
             ,     "<error-option>rollback-on-error</error-option>"
             ,     "<config>"
             );

    private static final String EDIT_VRF_CONFIG_HEADER = String.join("\n"
            , EDIT_CONFIG_HEADER
              ,             "<l3vpn xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
              ,                 "<l3vpncomm>"
              ,                   "<l3vpnInstances>"
              ,                    " <l3vpnInstance>"
            );
    private static final String EDIT_VRF_CONFIG_CREATE_HEADER = String.join("\n"
            , EDIT_CONFIG_HEADER
              ,             "<l3vpn xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
              ,                 "<l3vpncomm>"
              ,                   "<l3vpnInstances>"
              ,                     "<l3vpnInstance operation=\"create\">"
            );

    private static final String EDIT_VRF_CONFIG_REMOVE_HEADER = String.join("\n"
            , EDIT_CONFIG_HEADER
              ,             "<l3vpn xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
              ,                 "<l3vpncomm>"
              ,                   "<l3vpnInstances>"
              ,                     "<l3vpnInstance operation=\"delete\">"
            );



    private static final String EDIT_CONFIG_TAIL = String.join("\n"
             ,    "</config>"
             ,   "</edit-config>"
             , "</rpc>"
             );

    private static final String EDIT_VRF_CONFIG_TAIL = String.join("\n"
              ,                   " </l3vpnInstance>"
              ,                  "</l3vpnInstances>"
              ,                 "</l3vpncomm>"
              ,               "</l3vpn>"
              ,   EDIT_CONFIG_TAIL
              );



    private static final String GET_CONFIG_HEADER =  String.join("\n"
            , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          ,   "<rpc message-id=\"1024\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
          ,    "<get>"
          ,    "<filter type=\"subtree\">"
          );


    private MessageBuilder(){}

    public static String msgCreateEVPN(String bdID, boolean isRDAuto, String rd)
    {
        String evpnStr = "<evpnName>" + bdID + "</evpnName>";
        String bdStr = "<bdId>" +bdID + "</bdId>";

        String rdStr ;
        if (isRDAuto) {
            rdStr = " <evpnAutoRD>true</evpnAutoRD>";
        } else
        {
           rdStr = "<evpnRD>" + rd  + "</evpnRD>";
        }

        return String.join("\n"
                 ,       EDIT_CONFIG_HEADER
        ,               "<evpn xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
        ,                 "<evpnInstances>"
        ,                   "<evpnInstance operation=\"create\">"
        ,                     evpnStr
        ,                     bdStr
        ,                     rdStr
        ,                   "</evpnInstance>"
        ,                 "</evpnInstances>"
        ,              "</evpn>"
        ,    EDIT_CONFIG_TAIL
                  );
    }

    public static String msgSetRT(String bdID, String rt) {
        String evpnStr = "<evpnName>" + bdID + "</evpnName>";
        String bdStr = "<bdId>" +bdID + "</bdId>";
        String stringRT = "<vrfRTValue>" + rt + "</vrfRTValue>";

        return String.join("\n"
                     ,       EDIT_CONFIG_HEADER
                     ,       "<evpn xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                     ,         "<evpnInstances>"
                     ,           "<evpnInstance>"
                     ,             evpnStr
                     ,             bdStr
                     ,             "<evpnRTs>"
                     ,               "<evpnRT operation=\"create\">"
                     ,                 "<vrfRTType>export_extcommunity</vrfRTType>"
                     ,                 stringRT
                     ,               "</evpnRT>"
                     ,               "<evpnRT operation=\"create\">"
                     ,                 "<vrfRTType>import_extcommunity</vrfRTType>"
                     ,                 stringRT
                     ,               "</evpnRT>"
                     ,             "</evpnRTs>"
                     ,          " </evpnInstance>"
                     ,         "</evpnInstances>"
                     ,       "</evpn>"
                     ,    EDIT_CONFIG_TAIL
                      );
            }

    public static String msgRemoveRT(String bdID, String rt) {
        String evpnStr = "<evpnName>" + bdID + "</evpnName>";
        String bdStr = "<bdId>" + bdID + "</bdId>";
        String stringRT = "<vrfRTValue>" + rt + "</vrfRTValue>";

        return String.join("\n"
                     ,       EDIT_CONFIG_HEADER
                     ,       "<evpn xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                     ,         "<evpnInstances>"
                     ,           "<evpnInstance>"
                     ,             evpnStr
                     ,             bdStr
                     ,             "<evpnRTs>"
                     ,               "<evpnRT operation=\"delete\">"
                     ,                 "<vrfRTType>export_extcommunity</vrfRTType>"
                     ,                 stringRT
                     ,               "</evpnRT>"
                     ,               "<evpnRT operation=\"delete\">"
                     ,                 "<vrfRTType>import_extcommunity</vrfRTType>"
                     ,                 stringRT
                     ,               "</evpnRT>"
                     ,             "</evpnRTs>"
                     ,          " </evpnInstance>"
                     ,         "</evpnInstances>"
                     ,       "</evpn>"
                     ,    EDIT_CONFIG_TAIL
                      );
            }


        public static String msgHello()
        {
            return  String.join("\n"
                    ,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    , "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                    , "<capabilities>"
                    , "<capability>"
                    ,  "urn:ietf:params:netconf:base:1.0"
                    , "</capability>"
                    , "<capability>urn:ietf:params:netconf:capability:writable-running:1.0</capability>"
                    , "<capability>urn:ietf:params:netconf:capability:candidate:1.0</capability>"
                    , "<capability>urn:ietf:params:netconf:capability:confirmed-commit:1.0</capability>"
                    , "<capability>urn:ietf:params:netconf:capability:rollback-on-error:1.0</capability>"
                    , "<capability>http://www.huawei.com/netconf/capability/execute-cli/1.0</capability>"
                    , "<capability>urn:ietf:params:netconf:capability:notification:1.0</capability>"
                    , "<capability>urn:ietf:params:netconf:capability:interleave:1.0</capability>"
                    , "</capabilities>"
                    , "</hello>"
                    );


        }

        public static String msgDispCurrentConfig()
        {
            return String.join("\n"
                    , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    , "<rpc message-id=\"11\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                    , "<execute-cli xmlns=\"http://www.huawei.com/netconf/capability/base/1.0\">"
                    , "<cmd>"
                    , "<id>1</id>"
                    , "<cmdline>display cu</cmdline>"
                    , "</cmd>"
                    , "</execute-cli>"
                    , "</rpc>"
                    );

        }

        public static String msgBindBDtoVNI (String vni , String bdID ) {
            String bdprop = "<bdId>" + bdID + "</bdId>";
            String vniProp = " <vniId>" + vni + "</vniId>";

            return String.join("\n"
                     ,       EDIT_CONFIG_HEADER
                     ,       "<nvo3 xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                     ,        "<nvo3Vni2Bds>"
                     ,           "<nvo3Vni2Bd operation=\"create\">"
                     ,            vniProp
                     ,            bdprop
                     ,          "</nvo3Vni2Bd>"
                     ,         "</nvo3Vni2Bds>"
                     ,       "</nvo3>"
             ,  EDIT_CONFIG_TAIL
             );

        }


        public static String msgRemoveVNIFROMBD(Long vni, Long bdID) {
            String bdprop = "<bdId>" + bdID + "</bdId>";
            String vniProp = " <vniId>" + vni + "</vniId>";
            return String.join("\n"
                     ,        EDIT_CONFIG_HEADER
                     ,       "<nvo3 xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                     ,        "<nvo3Vni2Bds>"
                     ,           "<nvo3Vni2Bd operation=\"delete\">"
                     ,            vniProp
                     ,            bdprop
                     ,          "</nvo3Vni2Bd>"
                     ,         "</nvo3Vni2Bds>"
                     ,       "</nvo3>"
                     ,    EDIT_CONFIG_TAIL
                  );
        }



            public static String configNVE (String sourceIP) {
                String sourceIPProp = "<srcAddr>" + sourceIP+ "</srcAddr>";
                return String.join("\n"
                         ,       EDIT_CONFIG_HEADER
                         ,       "<nvo3 xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                         ,                 "<nvo3Nves>"
                         ,                   "<nvo3Nve operation=\"merge\">"
             ,                     "<ifName>Nve1</ifName>"
             ,                     "<nveType>mode-l2</nveType>"
             ,                     sourceIPProp
             ,                   "</nvo3Nve>"
             ,                 "</nvo3Nves>"
             ,       "</nvo3>"
             ,    EDIT_CONFIG_TAIL
          );
            }


        public static String msgQueryIF(String ifname) {
            String ifstr = "<ifName>" + ifname + "</ifName>";
            return String.join("\n"
                     , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                     ,        "<rpc message-id=\"1024\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                     ,          " <get>"
                     ,            " <filter type=\"subtree\">"
                     ,              " <ifm xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                     ,               " <interfaces>"
                     ,               "  <interface>"
                     ,               ifstr
                     ,               "      <ifPhyType></ifPhyType>"
                     ,                "     <ifParentIfName></ifParentIfName>"
                     ,               "      <ifNumber></ifNumber>"
                     ,                "   </interface>"
                     ,                " </interfaces>"
                     ,              " </ifm>"
                     ,             "</filter>"
                     ,          " </get>"
                     ,         "</rpc>"
                     );


        }

                public static String msgCreateBDIF(Long bdID) {
                    String vbdif = "<ifName>vbdif" + bdID + "</ifName>";
                    return String.join("\n"
                            ,   EDIT_CONFIG_HEADER
                     ,               "<ifm xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                     ,                " <interfaces>"
                     ,                   "<interface operation=\"create\">"
                     ,                    vbdif
                     ,                    " <ifPhyType>Vbdif</ifPhyType>"
                     ,                   "</interface>"
                     ,                 "</interfaces>"
                     ,               "</ifm>"
                     ,             EDIT_CONFIG_TAIL
                     );

                }

                public static String msgAssignIPtoIF(String ifname, String mask, String ipAddress)
                {
                    String ifnameElement = "<ifName>" + ifname + "</ifName>";
                    String subnetMaskElement = "<subnetMask>" + mask + "</subnetMask>";
                    String addrTypeElement = "<addrType>main</addrType>" ;
                    String ipAddressElement = "<ifIpAddr>" + ipAddress + "</ifIpAddr>";

                    return String.join("\n"
                            ,   EDIT_CONFIG_HEADER
                            ,               "<ifm xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                            ,                " <interfaces>"
                            ,        "<interface>"
                            ,        ifnameElement
                            ,        "<ifmAm4>"
                            ,        "<am4CfgAddrs>"
                            ,         "<am4CfgAddr operation=\"create\">"
                            ,         subnetMaskElement
                            ,         addrTypeElement
                            ,         ipAddressElement
                            ,         "</am4CfgAddr>"
                            ,         "</am4CfgAddrs>"
                            ,         "</ifmAm4>"
                            ,         "</interface>"
                            ,         "</interfaces>"
                            ,         "</ifm>"
                ,             EDIT_CONFIG_TAIL
                );
           }




                public static String msgDeleteBDIF(Long bdID) {
                    String vbdif = "<ifName>vbdif" + bdID + "</ifName>";
                    return String.join("\n"
                            , EDIT_CONFIG_HEADER
                     ,               "<ifm xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                     ,                " <interfaces>"
                     ,                   "<interface operation=\"delete\">"
                     ,                    vbdif
                     ,                    " <ifPhyType>Vbdif</ifPhyType>"
                     ,                   "</interface>"
                     ,                 "</interfaces>"
                     ,               "</ifm>"
                     ,             EDIT_CONFIG_TAIL
                     );

                }

                public static String msgCreateBridgeDomain(String bdID, String desc) {
                    String BDStr = "<bdId>" + bdID + "</bdId>";
                    String BDDesc = "<bdDesc>" +desc + "</bdDesc>";

                    return String.join("\n"
                            ,  EDIT_CONFIG_HEADER
                            ,               "<evc xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                            ,                 "<bds>"
                            ,                   "<bd operation=\"create\">"
                            ,                     BDStr
                            ,                     BDDesc
                            ,                     "<macLearn>enable</macLearn>"
                            ,                     "<statistic>disable</statistic>"
                            ,                   "</bd>"
                            ,                 "</bds>"
                            ,               "</evc>"
                            ,  EDIT_CONFIG_TAIL
                            );

                }

                public static String  msgDeleteBridgeDomain(String bdID) {
                    String BDStr = "<bdId>" + bdID + "</bdId>";
                    return String.join("\n"
                            , EDIT_CONFIG_HEADER
                            ,               "<evc xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                            ,                 "<bds>"
                            ,                   "<bd operation=\"delete\">"
                            ,                     BDStr
                            ,                   "</bd>"
                            ,                 "</bds>"
                            ,               "</evc>"
                            ,          EDIT_CONFIG_TAIL
                            );


                }

                public static String  msgCreateVtepMembers(String vni) {
                    String vniStr = "<vniId>" + vni + "</vniId>";
                    return String.join("\n"
                            , EDIT_CONFIG_HEADER
                             ,       "<nvo3 xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                             ,                 "<nvo3Nves>"
                             ,                   "<nvo3Nve>"
                     ,                     "<ifName>Nve1</ifName>"
                     ,                     "<vniMembers>"
                     ,                      "<vniMember operation=\"create\">"
                     ,                        vniStr
                     ,                         "<protocol>bgp</protocol>"
                     ,                       "</vniMember>"
                     ,                     "</vniMembers>"
                     ,                   "</nvo3Nve>"
                     ,                 "</nvo3Nves>"
                     ,       "</nvo3>"
                     ,    EDIT_CONFIG_TAIL
                  );
                }

                public static String msgDeleteVtepMembers(String vni) {
                    String vniStr = "<vniId>" + vni + "</vniId>";

                    return String.join("\n"
                            , EDIT_CONFIG_HEADER
                             ,       "<nvo3 xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                             ,                 "<nvo3Nves>"
                             ,                   "<nvo3Nve>"
                     ,                     "<ifName>Nve1</ifName>"
                     ,                     "<vniMembers>"
                     ,                      "<vniMember operation=\"delete\">"
                     ,                        vniStr
                     ,                       "</vniMember>"
                     ,                     "</vniMembers>"
                     ,                   "</nvo3Nve>"
                     ,                 "</nvo3Nves>"
                     ,       "</nvo3>"
                     ,    EDIT_CONFIG_TAIL
                  );

                }

                public static String buildHexString(Integer bitIndex)
                {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0 ; i < 4096; i ++) {
                        if (i == bitIndex) {
                            sb.append("1");
                        } else {
                            sb.append("0");
                        }
                    }

                    StringBuilder sb1 = new StringBuilder();
                    String value = sb.toString();
                    for (int i = 0 ; i < 1024; i++) {
                        String a = value.substring(i*4, i*4 +4);
                        int decimal = Integer.parseInt(a, 2);
                        String hexStr = Integer.toString(decimal, 16);
                        sb1.append(hexStr);
                    }

                    return sb1.toString() + ":" + sb1.toString();
                }

                public static String msgBindTagToSubIf(String ifname, String vlanID) {

                    String vlanStr = "<dot1qVids>" + buildHexString(Integer.valueOf(vlanID)) + "</dot1qVids>";
                    String ifstr = "<ifName>" + ifname + "</ifName>";
                    return String.join("\n"
                      , EDIT_CONFIG_HEADER
                     ,            "<ethernet xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                     ,                 "<servicePoints>"
                     ,                   "<servicePoint>"
                     ,                   ifstr
                     ,                  "   <flowDot1qs operation=\"create\">"
                     ,                 vlanStr
                     ,                "</flowDot1qs>"
                     ,                "   </servicePoint>"
                     ,               "  </servicePoints>"
                     ,              " </ethernet>"

                     ,   EDIT_CONFIG_TAIL
                  );
                }


                public static String msgAddPortToBD (String bdID, String ifname)
                {
                    String bdStr = "<bdId>" + bdID + "</bdId>";
                    String ifnameStr = "<ifName>" + ifname + "</ifName>";

                    return String.join("\n"
                              , EDIT_CONFIG_HEADER
                 ,              "<evc xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                 ,                 "<bds>"
                 ,                   "<bd>"
                 ,                     bdStr
                 ,                     "<servicePoints>"
                 ,                       "<servicePoint operation=\"create\">"
                 ,                         ifnameStr
                 ,                       "</servicePoint>"
                 ,                     "</servicePoints>"
                 ,                   "</bd>"
                 ,                 "</bds>"
                 ,               "</evc>"
                 ,   EDIT_CONFIG_TAIL
              );
                }

                public static String msgDeletePortFromBD(String bdID, String ifname)
                {
                    String bdStr = "<bdId>" + bdID + "</bdId>";
                    String ifnameStr = "<ifName>" + ifname +  "</ifName>";

                    return String.join("\n"
                              , EDIT_CONFIG_HEADER
                              ,              "<evc xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                             ,                 "<bds>"
                             ,                   "<bd>"
                             ,                     bdStr
                             ,                     "<servicePoints>"
                             ,                       "<servicePoint operation=\"delete\">"
                             ,                         ifnameStr
                             ,                       "</servicePoint>"
                             ,                     "</servicePoints>"
                             ,                   "</bd>"
                             ,                 "</bds>"
                             ,               "</evc>"
                 ,   EDIT_CONFIG_TAIL
              );
                }

                public static String msgCreateSubIf(String port, String type, String number)
                {

                    String ifnameStr = "<ifName>" + port + "." + number + "</ifName>";
                    String ifParentStr = "<ifParentIfName>" + port + "</ifParentIfName>";
                    String ifNumberStr = "<ifNumber>" + number + "</ifNumber>";
                    String ifPhyTypeStr = "<ifPhyType>" + type + "</ifPhyType>";
                    String l2Str = "<l2SubIfFlag>true</l2SubIfFlag>";

                    return String.join("\n"
                              , EDIT_CONFIG_HEADER
                 ,               "<ifm xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                 ,                 "<interfaces>"
                 ,                   "<interface operation=\"create\">"
                 ,                     ifnameStr
                 ,                     ifPhyTypeStr
                 ,                     ifParentStr
                 ,                     ifNumberStr
                 ,                     l2Str
                 ,                     "<ifClass>subInterface</ifClass>"
                 ,                   "</interface>"
                 ,                 "</interfaces>"
                 ,               "</ifm>"
                 ,   EDIT_CONFIG_TAIL
              );
                }

                public static String msgRemoveSubIf(String ifname)
                {

                    String ifnameStr = "<ifName>" + ifname + "</ifName>";

                    return String.join("\n"
                              , EDIT_CONFIG_HEADER
                 ,               "<ifm xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                 ,                 "<interfaces>"
                 ,                   "<interface operation=\"delete\">"
                 ,                     ifnameStr
                 ,                   "</interface>"
                 ,                 "</interfaces>"
                 ,               "</ifm>"
                 ,   EDIT_CONFIG_TAIL
              );
                }


                public static String msgUndoPortSwitch(String port)
                {
                   String portStr = "<ifName>" + port + "</ifName>";
                return String.join("\n"
                          , EDIT_CONFIG_HEADER
                   ,              "<ethernet xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                   ,                 "<ethernetIfs>"
                   ,                   "<ethernetIf operation=\"merge\">"
                   ,                     portStr
                   ,                   "<l2Enable>disable</l2Enable>"
                   ,                   "</ethernetIf>"
                   ,                 "</ethernetIfs>"
                   ,               "</ethernet>"
                     ,   EDIT_CONFIG_TAIL
                          );
                }

                public static String msgGetSubTags(String ifname)
                {
                       String ifnameStr = "<ifName>" + ifname + "</ifName>";
                        return String.join("\n"
                                  ,GET_CONFIG_HEADER
                ,               "<ethernet xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                ,                 "<servicePoints>"
                ,                   "<servicePoint>"
                ,                     ifnameStr
                ,                     "<flowDot1qs>"
                ,                      "<dot1qVids></dot1qVids>"
                ,                   "</flowDot1qs>"
                ,                  "</servicePoint>"
                ,                "</servicePoints>"
                ,               "</ethernet>"
                ,             "</filter>"
                ,           "</get>"
                ,         "</rpc>"
                );
                }



                public static String msgGetEthernetIfs()
                {
                    return String.join("\n"
                            ,GET_CONFIG_HEADER
                            ,               "<ethernet xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                ,                 "<ethernetIfs>"
                ,                   "<ethernetIf>"
                ,                     "<ifName></ifName>"
                ,                  "</ethernetIf>"
                ,                 "</ethernetIfs>"
                ,               "</ethernet>"
                ,            " </filter>"
                ,           "</get>"
                ,         "</rpc>"
                );
                }

                public static String msgGetNNIs()
                {

                return String.join("\n"
                        ,GET_CONFIG_HEADER
                ,               "<lldp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                ,                 "<lldpInterfaces>"
                ,                  "<lldpInterface>"
                ,                     "<ifName></ifName>"
                ,                     "<lldpAdminStatus></lldpAdminStatus>"
                ,                     "<lldpNeighbors>"
                ,                       "<lldpNeighbor>"
                ,                         "<nbIndex></nbIndex>"
                ,                         "<chassisIdSubtype></chassisIdSubtype>"
                ,                         "<chassisId></chassisId>"
                ,                         "<systemName></systemName>"
                ,                         "<portId></portId>"
                ,                       "</lldpNeighbor>"
                ,                    "</lldpNeighbors>"

                ,                   "</lldpInterface>"
                ,                 "</lldpInterfaces>"
                ,               "</lldp>"
                ,            " </filter>"
                ,           "</get>"
                ,         "</rpc>"
                );
                }

                public static String msgGetNeighborInfo(String ifname)
                {

                    String ifnameElement =  "<ifName>" + ifname + "</ifName>";
                return String.join("\n"
                        ,GET_CONFIG_HEADER
                ,               "<lldp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                ,                 "<lldpInterfaces>"
                ,                  "<lldpInterface>"
                ,                     ifnameElement
                ,                     "<lldpNeighbors>"
                ,                       "<lldpNeighbor>"
                ,                         "<nbIndex></nbIndex>"
                ,                         "<chassisIdSubtype></chassisIdSubtype>"
                ,                         "<chassisId></chassisId>"
                ,                         "<systemName></systemName>"
                ,                         "<portId></portId>"
                ,                       "</lldpNeighbor>"
                ,                    "</lldpNeighbors>"
                ,                   "</lldpInterface>"
                ,                 "</lldpInterfaces>"
                ,               "</lldp>"
                ,            " </filter>"
                ,           "</get>"
                ,         "</rpc>"
                );
                }

      public static String msgGetLLDPSysInfo()
      {
          return String.join("\n"
                  ,GET_CONFIG_HEADER
          ,              "<lldp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,               "  <lldpSys>"
          ,                "   <lldpSysInformation>"
          ,                 "    <chassisIdSubtype></chassisIdSubtype>"
          ,                  "   <chassisId></chassisId>"
          ,                   "  <sysName></sysName>"
          ,                  " </lldpSysInformation>"
          ,                " </lldpSys>"
          ,              " </lldp>"
          ,            " </filter>"
          ,          " </get>"
          ,         "</rpc>"
          );

      }


      public static String msgGetIfIPv4Address(String ifname)
      {
          String ifnamestr = "<ifName>" + ifname + "</ifName>";
          return String.join("\n"
                  ,GET_CONFIG_HEADER
          ,               "<ifm xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<interfaces>"
          ,                   "<interface>"
          ,                    ifnamestr
          ,                     "<ifmAm4>"
          ,                      " <am4CfgAddrs>"
          ,                       "  <am4CfgAddr>"
          ,                        "   <subnetMask></subnetMask>"
          ,                         "  <addrType></addrType>"
          ,                          " <ifIpAddr></ifIpAddr>"
          ,                        " </am4CfgAddr>"
          ,                      " </am4CfgAddrs>"
          ,                   "  </ifmAm4>"
          ,                  " </interface>"
          ,                " </interfaces>"
          ,               "</ifm>"
          ,            " </filter>"
          ,          " </get>"
          ,         "</rpc>"
                  );
      }

      public static String msgCreateRoutePolicy(String vrfName, boolean isExportPolicy)
      {
          String policyName = vrfName + "_import";
          if(isExportPolicy) {
              policyName =  vrfName + "_export";
          }

          return String.join("\n"
                  , EDIT_CONFIG_HEADER
          ,               "<rtp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<routePolicys>"
          ,                   "<routePolicy operation=\"create\">"
          ,                     "<name>" + policyName + "</name>"
          ,                  "</routePolicy>"
          ,                 "</routePolicys>"
          ,               "</rtp>"
          ,   EDIT_CONFIG_TAIL
                   );
      }


      public static String msgCreateRoutePolicyPermitNode(String vrfName, boolean isExportPolicy, String nodeIndex)
      {
          String policyName = vrfName + "_import";
          if(isExportPolicy) {
              policyName =  vrfName + "_export";
          }

          return String.join("\n"
                  , EDIT_CONFIG_HEADER
          ,               "<rtp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<routePolicys>"
          ,                   "<routePolicy>"
          ,                     "<name>" + policyName + "</name>"
          ,                     "<routePolicyNodes>"
          ,                       "<routePolicyNode>"
          ,                         "<nodeSequence>" + nodeIndex+ "</nodeSequence>"
          ,                        "<matchMode>permit</matchMode>"
          ,                         "<description>" + policyName + "</description>"
          ,                      "</routePolicyNode>"
          ,                     "</routePolicyNodes>"
          ,                  "</routePolicy>"
          ,                 "</routePolicys>"
          ,               "</rtp>"
          ,   EDIT_CONFIG_TAIL
                   );
      }

      public static String msgRemoveRoutePolicyPermitNode(String vrfName, boolean isExportPolicy, String nodeIndex)
      {
          String policyName = vrfName + "_import";
          if(isExportPolicy) {
              policyName =  vrfName + "_export";
          }

          return String.join("\n"
                  , EDIT_CONFIG_HEADER
          ,               "<rtp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<routePolicys>"
          ,                   "<routePolicy>"
          ,                     "<name>" + policyName + "</name>"
          ,                     "<routePolicyNodes>"
          ,                       "<routePolicyNode operation=\"delete\">"
          ,                         "<nodeSequence>" + nodeIndex+ "</nodeSequence>"
          ,                        "<matchMode>permit</matchMode>"
          ,                         "<description>" + policyName + "</description>"
          ,                      "</routePolicyNode>"
          ,                     "</routePolicyNodes>"
          ,                  "</routePolicy>"
          ,                 "</routePolicys>"
          ,               "</rtp>"
          ,   EDIT_CONFIG_TAIL
                   );
      }


      public static String msgRemoveRoutePolicy(String vrfName, boolean isExportPolicy)
      {
          String policyName = vrfName + "_import";
          if(isExportPolicy) {
              policyName =  vrfName + "_export";
          }

          return String.join("\n"
                  , EDIT_CONFIG_HEADER
          ,               "<rtp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<routePolicys>"
          ,                   "<routePolicy operation=\"delete\">"
          ,                     "<name>" + policyName + "</name>"
          ,                  "</routePolicy>"
          ,                 "</routePolicys>"
          ,               "</rtp>"
          ,   EDIT_CONFIG_TAIL
                   );
      }


      public static String msgCreateIPv4PrefixFilter(String prefixName)
      {
          String prefixLine = "<name>" + prefixName + "</name>";
          return String.join("\n"
          ,  EDIT_CONFIG_HEADER
          ,               "<rtp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<prefixFilters>"
          ,                  "<prefixFilter operation=\"create\">"
          ,                     prefixLine
          ,                  "</prefixFilter>"
          ,                 "</prefixFilters>"
          ,               "</rtp>"
          ,   EDIT_CONFIG_TAIL
                  );
      }

      public static String msgRemoveIPv4PrefixFilter(String prefixName)
      {
          String prefixLine = "<name>" + prefixName + "</name>";
          return String.join("\n"
          ,  EDIT_CONFIG_HEADER
          ,               "<rtp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<prefixFilters>"
          ,                  "<prefixFilter operation=\"delete\">"
          ,                     prefixLine
          ,                  "</prefixFilter>"
          ,                 "</prefixFilters>"
          ,               "</rtp>"
          ,   EDIT_CONFIG_TAIL
                  );
      }


      public static String msgCreateIPv4PrefixFilterNode(String prefixName, String nodeIndex, String ipaddress, int mask)
      {
          String ipLine = "<address>" + ipaddress + "</address>";
          String maskLine = "<maskLength>" + mask  + "</maskLength>";
          String prefixLine = "<name>" + prefixName + "</name>";
          return String.join("\n"
          ,  EDIT_CONFIG_HEADER
          ,               "<rtp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<prefixFilters>"
          ,                  "<prefixFilter>"
          ,                     prefixLine
          ,                     "<prefixFilterNodes>"
          ,                      "<prefixFilterNode operation=\"merge\">"
          ,                       "<nodeSequence>" + nodeIndex + "</nodeSequence>"
          ,                         "<matchMode>permit</matchMode>"
          ,                         ipLine
          ,                         maskLine
          ,                       "</prefixFilterNode>"
          ,                     "</prefixFilterNodes>"
          ,                  "</prefixFilter>"
          ,                 "</prefixFilters>"
          ,               "</rtp>"
          ,   EDIT_CONFIG_TAIL
                  );
      }

      public static String msgRemoveIPv4PrefixFilterNode(String prefixName, String nodeIndex, String ipaddress, int mask)
      {
          String ipLine = "<address>" + ipaddress + "</address>";
          String maskLine = "<maskLength>" + mask  + "</maskLength>";
          String prefixLine = "<name>" + prefixName + "</name>";
          return String.join("\n"
          ,  EDIT_CONFIG_HEADER
          ,               "<rtp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<prefixFilters>"
          ,                  "<prefixFilter>"
          ,                     prefixLine
          ,                     "<prefixFilterNodes>"
          ,                      "<prefixFilterNode operation=\"delete\">"
          ,                       "<nodeSequence>" + nodeIndex + "</nodeSequence>"
          ,                         "<matchMode>permit</matchMode>"
          ,                         ipLine
          ,                         maskLine
          ,                       "</prefixFilterNode>"
          ,                     "</prefixFilterNodes>"
          ,                  "</prefixFilter>"
          ,                 "</prefixFilters>"
          ,               "</rtp>"
          ,   EDIT_CONFIG_TAIL
                  );
      }



      public static String msgCreateImportRoutePolicyEntry(String vrfName, String prefixName, String nodeIndex) {
          String policyNameLine = "<name>" + vrfName + "_import</name>";
          String prefixNameLine = "<prefixName>" + prefixName + "</prefixName>";

          return String.join("\n"
                  , EDIT_CONFIG_HEADER
          ,               "<rtp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<routePolicys>"
          ,                   "<routePolicy>"
          ,                     policyNameLine
          ,                     "<routePolicyNodes>"
          ,                      "<routePolicyNode>"
          ,                         "<nodeSequence>"+ nodeIndex + "</nodeSequence>"
          ,                        "<matchCondition>"
          ,                          "<matchDestPrefixFilters>"
          ,                             "<matchDestPrefixFilter operation=\"create\">"
          ,                               prefixNameLine
          ,                             "</matchDestPrefixFilter>"
          ,                           "</matchDestPrefixFilters>"
          ,                        "</matchCondition>"
          ,                       "</routePolicyNode>"
          ,                    "</routePolicyNodes>"
          ,                   "</routePolicy>"
          ,                 "</routePolicys>"
          ,               "</rtp>"
          ,   EDIT_CONFIG_TAIL
                   );
      }

      public static String msgRemoveImportRoutePolicyEntry(String vrfName, String prefixName, String nodeIndex) {
          String policyNameLine = "<name>" + vrfName + "_import</name>";
          String prefixNameLine = "<prefixName>" + prefixName + "</prefixName>";

          return String.join("\n"
                  , EDIT_CONFIG_HEADER
          ,               "<rtp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
          ,                 "<routePolicys>"
          ,                   "<routePolicy>"
          ,                     policyNameLine
          ,                     "<routePolicyNodes>"
          ,                      "<routePolicyNode operation=\"delete\">"
          ,                        "<nodeSequence>"+ nodeIndex + "</nodeSequence>"
          ,                        "<matchMode>permit</matchMode>"
          ,                       "</routePolicyNode>"
          ,                    "</routePolicyNodes>"
          ,                   "</routePolicy>"
          ,                 "</routePolicys>"
          ,               "</rtp>"
          ,   EDIT_CONFIG_TAIL
                   );
      }



      public static String msgSetVrfAfandRD(String vrfname,  String vrfRD) {

          String vrfline = "<vrfName>" + vrfname  + "</vrfName>";
          String rdline =  "<vrfRD>" + vrfRD + "</vrfRD>";
          if (vrfRD.isEmpty()) {
            rdline = "";
        }
          String impolicyLine =  "<imPolicyName>" + vrfname + "_import" + "</imPolicyName>";
          String expolicyLine =  "<exPolicyName>" + vrfname + "_export" + "</exPolicyName>";

          return String.join("\n"
                  , EDIT_VRF_CONFIG_HEADER
      ,                       vrfline
      ,                       "<vpnInstAFs>"
      ,                         "<vpnInstAF operation=\"create\">"
      ,                        "   <afType>ipv4uni</afType>"
      ,                           rdline
      ,                      impolicyLine
      ,                      expolicyLine
      ,                     "    </vpnInstAF>"
      ,                    "   </vpnInstAFs>"
      ,   EDIT_VRF_CONFIG_TAIL
   );
      }

      public static String msgSetVRFTarget(String vrfname, String importValue, String exportValue) {

          String vrfline = "<vrfName>" + vrfname  + "</vrfName>";
          String rtImportline = " <vrfRTValue>" + importValue + "</vrfRTValue>";
          String rtExportline = " <vrfRTValue>" + exportValue + "</vrfRTValue>";

          return String.join("\n"
                  , EDIT_VRF_CONFIG_HEADER
      ,                      vrfline
      ,                       "<vpnInstAFs>"
      ,                        " <vpnInstAF>"
      ,                           "<afType>ipv4uni</afType>"
      ,                          " <exVpnTargets>"
      ,                             "<exVpnTarget operation=\"create\">"
      ,                              rtImportline
      ,                             "  <vrfRTType>export_extcommunity</vrfRTType>"
      ,                              " <extAddrFamily>evpn</extAddrFamily>"
      ,                            " </exVpnTarget>"
      ,                             "<exVpnTarget operation=\"create\">"
      ,                             rtExportline
      ,                             "  <vrfRTType>import_extcommunity</vrfRTType>"
      ,                              " <extAddrFamily>evpn</extAddrFamily>"
      ,                            " </exVpnTarget>"
      ,                           "</exVpnTargets>"
      ,                         "</vpnInstAF>"
      ,                       "</vpnInstAFs>"
      ,   EDIT_VRF_CONFIG_TAIL
        );

      }

      public static String msgSetVRFImportTarget(String vrfname, String importValue) {

          String vrfline = "<vrfName>" + vrfname  + "</vrfName>";
          String rtImportline = " <vrfRTValue>" + importValue + "</vrfRTValue>";

          return String.join("\n"
                  , EDIT_VRF_CONFIG_HEADER
      ,                      vrfline
      ,                       "<vpnInstAFs>"
      ,                        " <vpnInstAF>"
      ,                           "<afType>ipv4uni</afType>"
      ,                          " <exVpnTargets>"
      ,                             "<exVpnTarget operation=\"create\">"
      ,                              rtImportline
      ,                             "  <vrfRTType>export_extcommunity</vrfRTType>"
      ,                              " <extAddrFamily>evpn</extAddrFamily>"
      ,                            " </exVpnTarget>"
      ,                           "</exVpnTargets>"
      ,                         "</vpnInstAF>"
      ,                       "</vpnInstAFs>"
      ,   EDIT_VRF_CONFIG_TAIL
        );

      }


      public static String msgBindVrf2Vni(String vrfname, String vni)
      {
          String vrfline = "<vrfName>" + vrfname  + "</vrfName>";
          String vniline = " <vniId>" + vni + "</vniId>";
          return String.join("\n"
                  , EDIT_CONFIG_HEADER
      ,               "<nvo3 xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
      ,                 "<nvo3Vni2Vrfs>"
      ,                   "<nvo3Vni2Vrf operation=\"create\">"
      ,                    vniline
      ,                    vrfline
      ,                  " </nvo3Vni2Vrf>"
      ,                 "</nvo3Vni2Vrfs>"
      ,              "</nvo3>"
      ,   EDIT_CONFIG_TAIL
   );
      }


      public static String msgCreateVRF(String vrfname, String desc) {

          String vrfline = "<vrfName>" + vrfname  + "</vrfName>";
          String descline = " <vrfDescription>" + desc + "</vrfDescription>";
          return String.join("\n"
                  , EDIT_VRF_CONFIG_CREATE_HEADER
      ,                      vrfline
      ,                      descline
      ,         EDIT_VRF_CONFIG_TAIL
      );
      }

      public static String msgRemoveVRF(String vrfname) {

          String vrfline = "<vrfName>" + vrfname  + "</vrfName>";
          return String.join("\n"
                  , EDIT_VRF_CONFIG_REMOVE_HEADER
      ,                      vrfline
      ,         EDIT_VRF_CONFIG_TAIL
      );
      }



      public static String msgBindIFtoVRF(String ifname, String vrfname, String ipaddress, String mask)
      {
          String vrfline = "<vrfName>" + vrfname  + "</vrfName>";
          String ifnameline = "<ifName>" + ifname + "</ifName>";
          String ipline = "<ipv4Addr>" + ipaddress + "</ipv4Addr>";
          String maskline = "<subnetMask>" + mask + "</subnetMask>";
          return String.join("\n"
                  , EDIT_VRF_CONFIG_HEADER
      ,                       vrfline
      ,                       "<l3vpnIfs>"
      ,                         "<l3vpnIf operation=\"create\">"
      ,                           ifnameline
      ,                           ipline
      ,                           maskline
      ,                         "</l3vpnIf>"
      ,                       "</l3vpnIfs>"
      ,         EDIT_VRF_CONFIG_TAIL
      );
      }

      public static String msgIfArpConfig(String ifname, boolean isCollectHostEnable, boolean isDistriGWEnable)
      {
          String ifnameLine = "<ifName>" + ifname + "</ifName>";
          String ch_value = "true";
          if (!isCollectHostEnable) {
            ch_value = "false";
          }

          String dg_value = "true";
          if (!isDistriGWEnable) {
              dg_value = "false";
            }

          String hostCollectEnableLine = "<hostCollectEnable>"+ ch_value + "</hostCollectEnable>";
          String distriGWEnableLine = "<distriEnable>"+ dg_value + "</distriEnable>";

          return String.join("\n"
                  , EDIT_CONFIG_HEADER
                  , "<arp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
                  ,  "<arpInterfaces>"
                   ,   "<arpInterface operation=\"merge\">"
                    ,    ifnameLine
                     ,   hostCollectEnableLine
                     ,   distriGWEnableLine
                     , "</arpInterface>"
                    ,"</arpInterfaces>"
                 , "</arp>"
                 , EDIT_CONFIG_TAIL
                  );
      }


      //Leaking routes between different vpn-instances.
      public static String msgImportDirectRouteByBGP(String vrfname)
      {
          String vpn_instanceLine =   "<vrfName>" + vrfname + "</vrfName>";
          return String.join("\n"
                  , EDIT_CONFIG_HEADER
      ,             "<bgp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
      ,                 "<bgpcomm>"
      ,                  " <bgpVrfs>"
      ,                   "  <bgpVrf>"
      ,                    vpn_instanceLine
      ,                     "  <bgpVrfAFs>"
      ,                      "   <bgpVrfAF>"
      ,                       "    <afType>ipv4uni</afType>"
      ,                        "   <importRoutes>"
      ,                         "    <importRoute operation=\"create\">"
      ,                          "     <importProtocol>direct</importProtocol>"
      ,                           "    <importProcessId>0</importProcessId>"
      ,                            " </importRoute>"
      ,                          " </importRoutes>"
      ,                         "</bgpVrfAF>"
      ,                       "</bgpVrfAFs>"
      ,                     "</bgpVrf>"
      ,                  " </bgpVrfs>"
      ,                 "</bgpcomm>"
      ,              " </bgp>"
      ,             EDIT_CONFIG_TAIL

                  );
      }

      public static String msgAddIPv4FamilyInBGP(String vrfname)
      {
          String vpn_instanceLine =   "<vrfName>" + vrfname + "</vrfName>";
          return String.join("\n"
                  , EDIT_CONFIG_HEADER
      ,             "<bgp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
      ,                 "<bgpcomm>"
      ,                  " <bgpVrfs>"
      ,                   "  <bgpVrf>"
      ,                    vpn_instanceLine
      ,                     "  <bgpVrfAFs>"
      ,                      "   <bgpVrfAF>"
      ,                       "    <afType>ipv4uni</afType>"
      ,                         "</bgpVrfAF>"
      ,                       "</bgpVrfAFs>"
      ,                     "</bgpVrf>"
      ,                  " </bgpVrfs>"
      ,                 "</bgpcomm>"
      ,              " </bgp>"
      ,             EDIT_CONFIG_TAIL

                  );
      }

      public static String msgDeleteIPv4FamilyInBGP(String vrfname)
      {
          String vpn_instanceLine =   "<vrfName>" + vrfname + "</vrfName>";
          return String.join("\n"
                  , EDIT_CONFIG_HEADER
      ,             "<bgp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
      ,                 "<bgpcomm>"
      ,                  " <bgpVrfs>"
      ,                   "  <bgpVrf>"
      ,                    vpn_instanceLine
      ,                     "  <bgpVrfAFs>"
      ,                      "   <bgpVrfAF operation=\"delete\">"
      ,                       "    <afType>ipv4uni</afType>"
      ,                         "</bgpVrfAF>"
      ,                       "</bgpVrfAFs>"
      ,                     "</bgpVrf>"
      ,                  " </bgpVrfs>"
      ,                 "</bgpcomm>"
      ,              " </bgp>"
      ,             EDIT_CONFIG_TAIL

                  );
      }



      public static String msgDeleteDirectRouteByBGP(String vrfname)
      {
          String vpn_instanceLine =   "<vrfName>" + vrfname + "</vrfName>";
          return String.join("\n"
                  , EDIT_CONFIG_HEADER
      ,             "<bgp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
      ,                 "<bgpcomm>"
      ,                  " <bgpVrfs>"
      ,                   "  <bgpVrf>"
      ,                    vpn_instanceLine
      ,                     "  <bgpVrfAFs>"
      ,                      "   <bgpVrfAF>"
      ,                       "    <afType>ipv4uni</afType>"
      ,                        "   <importRoutes>"
      ,                         "    <importRoute operation=\"delete\">"
      ,                          "     <importProtocol>direct</importProtocol>"
      ,                           "    <importProcessId>0</importProcessId>"
      ,                            " </importRoute>"
      ,                          " </importRoutes>"
      ,                         "</bgpVrfAF>"
      ,                       "</bgpVrfAFs>"
      ,                     "</bgpVrf>"
      ,                  " </bgpVrfs>"
      ,                 "</bgpcomm>"
      ,              " </bgp>"
      ,             EDIT_CONFIG_TAIL

                  );
      }



      public static String msgGetBGPVRFRouteImport(String vrfName)
      {
          String vrfLine = "<vrfName>" + vrfName + "</vrfName>";
          return String.join("\n"
                  ,GET_CONFIG_HEADER
      ,              "<bgp xmlns=\"http://www.huawei.com/netconf/vrp\" content-version=\"1.0\" format-version=\"1.0\">"
      ,                 "<bgpcomm>"
      ,                   "<bgpVrfs>"
      ,                     "<bgpVrf>"
      ,                       vrfLine
      ,                       "<bgpVrfAFs>"
      ,                         "<bgpVrfAF>"
      ,                      "<afType></afType>"
     // ,                           "<importRoutes>"
     // ,                             "<importRoute>"
     // ,                               "<importProtocol></importProtocol>"
     // ,                               "<importProcessId></importProcessId>"
     // ,                             "</importRoute>"
     // ,                           "</importRoutes>"
      ,                         "</bgpVrfAF>"
      ,                       "</bgpVrfAFs>"
      ,                     "</bgpVrf>"
      ,                 "</bgpVrfs>"
      ,                 "</bgpcomm>"
      ,               "</bgp>"
      ,             "</filter>"
      ,           "</get>"
      ,         "</rpc>"
      );
          }
}


