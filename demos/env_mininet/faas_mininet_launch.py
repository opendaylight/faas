#!/usr/bin/python

import re
import os
from mininet.cli import CLI
from mininet.log import lg, info
from mininet.log import setLogLevel, info,error
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.link import Intf
from mininet.node import RemoteController, OVSKernelSwitch,Controller,OVSSwitch
from subprocess import call
from faas_mininet_config import *

class ExtendOVSSwitch(OVSSwitch):

    def __init__(self, name, vtep, **params):
        self.vtep = vtep
        OVSSwitch.__init__(self, name, **params)

    def attach(self, intf):
        "Connect a data port"
        #self.cmd( 'ovs-vsctl add-port', self, intf)
        self.cmd( 'ovs-vsctl add-port', self, intf, '-- set interface', intf, 'ofport_request=%d' % self.ports[intf])
        self.cmd( 'ifconfig', intf, 'up')
        OVSSwitch.TCReapply( intf)

    def start( self, controllers):
        "Start up a new OVS OpenFlow switch using ovs-vsctl"
        if self.inNamespace:
            raise Exception(
                'OVS kernel switch does not work in a namespace')
        # We should probably call config instead, but this
        # requires some rethinking...
        self.cmd( 'ifconfig lo up' )
        # Annoyingly, --if-exists option seems not to work
        self.cmd( 'ovs-vsctl del-br', self )
        self.cmd( 'ovs-vsctl add-br', self )
        if self.datapath == 'user':
            self.cmd( 'ovs-vsctl set bridge', self,'datapath_type=netdev' )
        int( self.dpid, 16 ) # DPID must be a hex string
        self.cmd( 'ovs-vsctl -- set Bridge', self,
                  'other_config:datapath-id=' + self.dpid )
        self.cmd( 'ovs-vsctl set-fail-mode', self, self.failMode )
        for intf in self.intfList():
            if not intf.IP():
                self.attach( intf )

        # Add vtep setting
        self.cmd( 'ovs-vsctl br-set-external-id', self, 'vtep-ip' , self.vtep)        
        self.cmd( 'ifconfig', self, self.vtep)

        # Add controllers
        clist = ' '.join( [ 'tcp:%s:%d' % ( c.IP(), c.port )
                            for c in controllers ] )
        if self.listenPort:
            clist += ' ptcp:%s' % self.listenPort
        self.cmd( 'ovs-vsctl set-controller', self, clist )
        # Reconnect quickly to controllers (1s vs. 15s max_backoff)
        for uuid in self.controllerUUIDs():
            if uuid.count( '-' ) != 4:
                # Doesn't look like a UUID
                continue
            uuid = uuid.strip()
            self.cmd( 'ovs-vsctl set Controller', uuid,
                      'max_backoff=1000' )


class FabricTopo( Topo ):
    "Simple Fabric Topology example."

    def __init__( self, **opts ):
        "Create fabric topo."

        super(FabricTopo, self).__init__(**opts)

        for sw in switches:
            switchx = self.addSwitch(sw['name'], dpid=self.regulate_dpid(sw['dpid']), vtep=sw["vtep"])

        for host in hosts:
            hostx = self.addHost(host["name"], mac=host["mac"], ip=host["ip"], defaultRoute = "via %s" % host["gw"])
            self.addLink(host["switch"], hostx, port1=host["ofport"])

    def regulate_dpid(self, dpid):
        if len(dpid) < 16:
            filler = '0000000000000000'
            return filler[:len(filler) - len(dpid)] + dpid
        elif len(dpid) > 16:
            print 'DPID: %s is too long' % dpid
            sys.exit(3)


topos = { 'fabric' : FabricTopo}

if __name__ == '__main__':
    lg.setLogLevel( 'info' )

    call(['ovs-vsctl', 'set-manager', 'tcp:%s:6640' % odl_controller])

    info( "*** Initializing Mininet and kernel modules\n" )
    OVSKernelSwitch.setup()

    info( "*** Creating network\n" )
    network = Mininet( FabricTopo(), switch=ExtendOVSSwitch ,controller=None)

    network.addController(name='c0', controller=RemoteController, ip=odl_controller)

    info( "*** Starting network\n" )
    network.start()

    info( "*** Starting CLI (type 'exit' to exit)\n" )
    CLI( network )

    info( "*** Stopping network\n" )
    network.stop()
