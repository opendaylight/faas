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

        switch1 = self.addSwitch('s1')

        self.addLink(host1, switch1)
        self.addLink(host2, switch1)

        host3 = self.addHost('h3', mac='62:02:1a:00:b7:13', ip='172.16.1.13/24', defaultRoute='via 172.16.1.1')
        host4 = self.addHost('h4', mac='62:02:1a:00:b7:22', ip='172.16.2.22/24', defaultRoute='via 172.16.2.1')

        switch2 = self.addSwitch('s2')

        self.addLink(host3, switch2)
        self.addLink(host4, switch2)

        switch3 = self.addSwitch('s3')
        switch4 = self.addSwitch('s4')

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
    os.system("ifconfig s1 192.168.20.101")
    os.system("ovs-vsctl br-set-external-id s1 vtep-ip 192.168.20.101")
    os.system("ifconfig s2 192.168.20.102")
    os.system("ovs-vsctl br-set-external-id s2 vtep-ip 192.168.20.102")
    os.system("ifconfig s3 192.168.20.103")
    os.system("ovs-vsctl br-set-external-id s3 vtep-ip 192.168.20.103")
    os.system("ifconfig s4 192.168.20.104")
    os.system("ovs-vsctl br-set-external-id s4 vtep-ip 192.168.20.104")

    info( "*** Starting CLI (type 'exit' to exit)\n" )
    CLI( network )

    info( "*** Stopping network\n" )
    network.stop()
