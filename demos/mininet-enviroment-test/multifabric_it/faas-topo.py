#!/usr/bin/python

import re
import os
from mininet.cli import CLI
from mininet.log import lg, info
from mininet.log import setLogLevel, info,error
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.link import Intf
from mininet.node import RemoteController, OVSKernelSwitch,Controller

# learning mac from arp request for spine device
SPINE_ARP_LEARNING = "ovs-ofctl add-flow -OOpenFlow13 %s 'table=10, priority=1024, reg0=0x3, arp, arp_op=1 actions=learn(table=110,priority=1024,NXM_NX_TUN_ID[],NXM_OF_ETH_DST[]=NXM_OF_ETH_SRC[],load:NXM_OF_VLAN_TCI[]->NXM_OF_VLAN_TCI[],output:NXM_OF_IN_PORT[]),goto_table:20'"
# learning mac from normal packets for spine device
SPINE_NORMAL_LEARNING = "ovs-ofctl add-flow -OOpenFlow13 %s 'table=10, priority=1023, reg0=0x3,vlan_tci=0x1000/0x1000 actions=learn(table=110,priority=1024,NXM_NX_TUN_ID[],NXM_OF_ETH_DST[]=NXM_OF_ETH_SRC[],load:NXM_OF_VLAN_TCI[]->NXM_OF_VLAN_TCI[],output:NXM_OF_IN_PORT[]),strip_vlan,goto_table:20'"
# learning mac for leaf device
LEAF_LEARNING = "ovs-ofctl add-flow -OOpenFlow13 %s 'table=10, priority=1024, reg0=0x2, actions=learn(table=110,priority=1024,NXM_NX_TUN_ID[],NXM_OF_ETH_DST[]=NXM_OF_ETH_SRC[],load:NXM_NX_TUN_IPV4_SRC[]->NXM_NX_TUN_IPV4_DST[],output:NXM_OF_IN_PORT[]),goto_table:20'"

HOSTX_MAC = '62:02:1a:00:00:02'
HOSTX_IP = '192.168.2.2'
FLOATING_IP_1 = '192.168.1.2'
HOSTX_GW_MAC = '62:02:1a:00:00:01'
HOSTX_GW_MAC_HEX = '0x62021a000001'
HOSTX_GW = '192.168.2.1'
HOSTX_GW_HEX = '0xc0a80201'
FLOATING_IP_GW_MAC = '80:38:bc:a1:33:c8'
FIXED_IP_GW_MAC = '80:38:bc:a1:33:c7'

S3_INBOUND_FLOW = "ovs-ofctl add-flow -OOpenFlow13 sw3 'table=0,in_port=1,ip, nw_src=%s, nw_dst=%s actions=set_field:%s->eth_dst, set_field:%s->eth_src, output:2'"
S3_OUTBOUND_FLOW = "ovs-ofctl add-flow -OOpenFlow13 sw3 'table=0,in_port=2,ip, nw_src=%s, nw_dst=%s actions=set_field:%s->eth_dst, set_field:%s->eth_src, output:1'"
S3_ARP_RESPONDER = "ovs-ofctl add-flow -OOpenFlow13 sw3 'table=0, arp,arp_tpa=%s,arp_op=1 actions=move:NXM_OF_ETH_SRC[]->NXM_OF_ETH_DST[],set_field:%s->eth_src,load:0x2->NXM_OF_ARP_OP[],move:NXM_NX_ARP_SHA[]->NXM_NX_ARP_THA[],move:NXM_OF_ARP_SPA[]->NXM_OF_ARP_TPA[],load:%s->NXM_NX_ARP_SHA[],load:%s->NXM_OF_ARP_SPA[],IN_PORT'"

class FabricTopo( Topo ):
    "Simple Fabric Topology example."

    def __init__( self, **opts ):
        "Create fabric topo."

        super(FabricTopo, self).__init__(**opts)

        host35_2 = self.addHost('h35_2', mac='00:00:00:00:35:02', ip='10.0.35.2/24', defaultRoute='via 10.0.35.1')
        host35_3 = self.addHost('h35_3', mac='00:00:00:00:35:03', ip='10.0.35.3/24', defaultRoute='via 10.0.35.1')
        host36_2 = self.addHost('h36_2', mac='00:00:00:00:36:02', ip='10.0.36.2/24', defaultRoute='via 10.0.36.1')
        host36_3 = self.addHost('h36_3', mac='00:00:00:00:36:03', ip='10.0.36.3/24', defaultRoute='via 10.0.36.1')

        switch11 = self.addSwitch('sw11')

        self.addLink(switch11, host35_2, 352)
        self.addLink(switch11, host35_3, 353)
        self.addLink(switch11, host36_2, 362)
        self.addLink(switch11, host36_3, 363)

        host35_4 = self.addHost('h35_4', mac='00:00:00:00:35:04', ip='10.0.35.4/24', defaultRoute='via 10.0.35.1')
        host35_5 = self.addHost('h35_5', mac='00:00:00:00:35:05', ip='10.0.35.5/24', defaultRoute='via 10.0.35.1')
        host36_4 = self.addHost('h36_4', mac='00:00:00:00:36:04', ip='10.0.36.4/24', defaultRoute='via 10.0.36.1')
        host36_5 = self.addHost('h36_5', mac='00:00:00:00:36:05', ip='10.0.36.5/24', defaultRoute='via 10.0.36.1')

        switch12 = self.addSwitch('sw12')

        self.addLink(switch12, host35_4, 354)
        self.addLink(switch12, host35_5, 355)
        self.addLink(switch12, host36_4, 364)
        self.addLink(switch12, host36_5, 365)

        host37_2 = self.addHost('h37_2', mac='00:00:00:00:37:02', ip='10.0.37.2/24', defaultRoute='via 10.0.37.1')
        host37_3 = self.addHost('h37_3', mac='00:00:00:00:37:03', ip='10.0.37.3/24', defaultRoute='via 10.0.37.1')

        switch13 = self.addSwitch('sw13')
        self.addLink(switch13, host37_2, 372)
        self.addLink(switch13, host37_3, 373)

        switch1 = self.addSwitch('sw1')

        switch2 = self.addSwitch('sw2')
        self.addLink(switch1, switch2)

        switch21 = self.addSwitch('sw21')
        host35_8 = self.addHost('h35_8',  mac='00:00:00:00:35:08', ip='10.0.35.8/24', defaultRoute='via 10.0.35.1')
        host36_8 = self.addHost('h36_8',  mac='00:00:00:00:36:08', ip='10.0.36.8/24', defaultRoute='via 10.0.36.1')
        host37_8 = self.addHost('h37_8',  mac='00:00:00:00:37:08', ip='10.0.37.8/24', defaultRoute='via 10.0.37.1')

        self.addLink(switch21, host35_8, 358)
        self.addLink(switch21, host36_8, 368)
        self.addLink(switch21, host37_8, 378)

        switch3 = self.addSwitch('sw3')
        self.addLink(switch2, switch3)

        hostx = self.addHost('hostx', mac=HOSTX_MAC, ip='192.168.2.2/24', defaultRoute='via 192.168.2.1') 
        self.addLink(hostx, switch3)

topos = { 'fabric' : FabricTopo}

if __name__ == '__main__':
    lg.setLogLevel( 'info' )

    os.system("ovs-vsctl set-manager tcp:127.0.0.1:6640")

    info( "*** Initializing Mininet and kernel modules\n" )
    OVSKernelSwitch.setup()

    info( "*** Creating network\n" )
    network = Mininet( FabricTopo( ), switch=OVSKernelSwitch ,controller=None)

    network.addController(name='c0', controller=RemoteController, ip='127.0.0.1')

    info( "*** Starting network\n" )
    network.start()

    # setup vtep
    os.system("ifconfig sw1 192.168.20.101")
    os.system("ovs-vsctl br-set-external-id sw1 vtep-ip 192.168.20.101")
    os.system("ifconfig sw11 192.168.20.111")
    os.system("ovs-vsctl br-set-external-id sw11 vtep-ip 192.168.20.111")
    os.system("ifconfig sw12 192.168.20.112")
    os.system("ovs-vsctl br-set-external-id sw12 vtep-ip 192.168.20.112")
    os.system("ifconfig sw13 192.168.20.113")
    os.system("ovs-vsctl br-set-external-id sw13 vtep-ip 192.168.20.113")
    os.system("ifconfig sw2 192.168.20.102")
    os.system("ovs-vsctl br-set-external-id sw2 vtep-ip 192.168.20.102")
    os.system("ifconfig sw21 192.168.20.121")
    os.system("ovs-vsctl br-set-external-id sw21 vtep-ip 192.168.20.121")
    os.system("ifconfig sw3 192.168.20.103")
    os.system("ovs-vsctl br-set-external-id sw3 vtep-ip 192.168.20.103")

    #----------------------------------- Learning flow, because ODL openflowplugin did not support "learn Action -------------------------------------
    os.system(SPINE_ARP_LEARNING % 'sw1')
    os.system(SPINE_NORMAL_LEARNING % 'sw1')
    os.system(SPINE_ARP_LEARNING % 'sw2')
    os.system(SPINE_NORMAL_LEARNING % 'sw2')

    os.system(LEAF_LEARNING % 'sw11')
    os.system(LEAF_LEARNING % 'sw12')
    os.system(LEAF_LEARNING % 'sw13')
    os.system(LEAF_LEARNING % 'sw21')

    #----------------------------------- OVS s3 flow, used for test NAT function -------------------------------------
    os.system(S3_INBOUND_FLOW % (FLOATING_IP_1, HOSTX_IP, HOSTX_MAC, HOSTX_GW_MAC))
    os.system(S3_OUTBOUND_FLOW % (HOSTX_IP, FLOATING_IP_1, FIXED_IP_GW_MAC, FLOATING_IP_GW_MAC))
    os.system(S3_ARP_RESPONDER % (HOSTX_GW, HOSTX_GW_MAC, HOSTX_GW_MAC_HEX, HOSTX_GW_HEX))

    info( "*** Starting CLI (type 'exit' to exit)\n" )
    CLI( network )

    info( "*** Stopping network\n" )
    network.stop()
