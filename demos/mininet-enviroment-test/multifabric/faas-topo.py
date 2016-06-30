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

class FabricTopo( Topo ):
    "Simple Fabric Topology example."

    def __init__( self, **opts ):
        "Create fabric topo."

        super(FabricTopo, self).__init__(**opts)

        host1 = self.addHost('h1', mac='62:02:1a:00:b7:11', ip='172.16.1.11/24', defaultRoute='via 172.16.1.1')
        host2 = self.addHost('h2', mac='62:02:1a:00:b7:12', ip='172.16.1.12/24', defaultRoute='via 172.16.1.1')

        switch11 = self.addSwitch('s11')

        self.addLink(host1, switch11)
        self.addLink(host2, switch11)

        host3 = self.addHost('h3', mac='62:02:1a:00:b7:13', ip='172.16.1.13/24', defaultRoute='via 172.16.1.1')
        host4 = self.addHost('h4', mac='62:02:1a:00:b7:22', ip='172.16.2.22/24', defaultRoute='via 172.16.2.1')

        switch12 = self.addSwitch('s12')

        self.addLink(host3, switch12)
        self.addLink(host4, switch12)

        switch21 = self.addSwitch('s21')
        self.addLink(switch12, switch21)

        switch22 = self.addSwitch('s22')
        host5 = self.addHost('h5',  mac='62:02:1a:00:b7:14', ip='172.16.1.14/24', defaultRoute='via 172.16.1.1')
        host6 = self.addHost('h6',  mac='62:02:1a:00:b7:23', ip='172.16.2.23/24', defaultRoute='via 172.16.2.1')
        self.addLink(host5, switch22)
        self.addLink(host6, switch22)

        switch31 = self.addSwitch('s31')
        self.addLink(switch21, switch31)

        hostx = self.addHost('hostx', mac='62:02:1a:00:00:1', ip='192.168.2.1/24') 
        self.addLink(hostx, switch31)

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
    os.system("ifconfig s11 192.168.20.101")
    os.system("ovs-vsctl br-set-external-id s11 vtep-ip 192.168.20.101")
    os.system("ifconfig s12 192.168.20.102")
    os.system("ovs-vsctl br-set-external-id s12 vtep-ip 192.168.20.102")
    os.system("ifconfig s21 192.168.20.103")
    os.system("ovs-vsctl br-set-external-id s21 vtep-ip 192.168.20.103")
    os.system("ifconfig s22 192.168.20.104")
    os.system("ovs-vsctl br-set-external-id s22 vtep-ip 192.168.20.104")

    info( "*** Starting CLI (type 'exit' to exit)\n" )
    CLI( network )

    info( "*** Stopping network\n" )
    network.stop()
