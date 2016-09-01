#!/usr/bin/python
import argparse
import requests,json
from requests.auth import HTTPBasicAuth
from subprocess import call
import time
import sys
import os


DEFAULT_PORT='8181'

USERNAME='admin'
PASSWORD='admin'

OPER_OVSDB_TOPO='/restconf/operational/network-topology:network-topology/topology/ovsdb:1'

OVS_BR_P = "/network-topology:network-topology/\
network-topology:topology[network-topology:topology-id='ovsdb:1']/\
network-topology:node[network-topology:node-id='%s']"

OVS_TP_P = "/network-topology:network-topology/\
network-topology:topology[network-topology:topology-id='ovsdb:1']/\
network-topology:node[network-topology:node-id='%s']/\
network-topology:termination-point[network-topology:tp-id='%s']"

def get(host, port, uri):
    url = 'http://' + host + ":" + port + uri
    #print url
    r = requests.get(url, auth=HTTPBasicAuth(USERNAME, PASSWORD))
    jsondata=json.loads(r.text)
    return jsondata

def put(host, port, uri, data, debug=False):
    '''Perform a PUT rest operation, using the URL and data provided'''

    url='http://'+host+":"+port+uri

    headers = {'Content-type': 'application/yang.data+json',
               'Accept': 'application/yang.data+json'}
    if debug == True:
        print "PUT %s" % url
        print json.dumps(data, indent=4, sort_keys=True)
    r = requests.put(url, data=json.dumps(data), headers=headers, auth=HTTPBasicAuth(USERNAME, PASSWORD))
    if debug == True:
        print r.text
    r.raise_for_status()

def post(host, port, uri, data, debug=False):
    '''Perform a POST rest operation, using the URL and data provided'''

    url='http://'+host+":"+port+uri
    headers = {'Content-type': 'application/yang.data+json',
               'Accept': 'application/yang.data+json'}
    if debug == True:
        print "POST %s" % url
        print json.dumps(data, indent=4, sort_keys=True)
    r = requests.post(url, data=json.dumps(data), headers=headers, auth=HTTPBasicAuth(USERNAME, PASSWORD))
    if debug == True:
        print r.text
    r.raise_for_status()
    

# Main definition - constants
 
# =======================
#     MENUS FUNCTIONS
# =======================
 
# Main menu

# =======================
#      MAIN PROGRAM
# =======================
 
# Main Program

NODE_ID_OVSDB = ''

URI_ADD_FABRIC_LINK = "/restconf/operations/fabric-resources:add-fabric-link"

def rpc_compose_fabric_uri():
    return "/restconf/operations/fabric:compose-fabric"

def rpc_compose_fabric_data1(name, behavior):
    return {
      "input" : {
           "name": name,
           "type":"VXLAN",
           "options":{
               "traffic-behavior":behavior,
               "capability-supported" : ['ip-mapping']
           },
           "device-nodes" : [
             {
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/sw11']", "vtep-ip" : "192.168.20.111"
              },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/sw12']", "vtep-ip" : "192.168.20.112"
              },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/sw13']", "vtep-ip" : "192.168.20.113"
             },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/sw1']", "vtep-ip" : "192.168.20.101"
             }
           ]
       }
    }

def rpc_compose_fabric_data2(name, behavior):
    return {
      "input" : {
           "name": name,
           "type":"VXLAN",
           "options":{
               "traffic-behavior":behavior,
               "capability-supported" : ['ip-mapping']
           },
           "device-nodes" : [
             {
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/sw21']", "vtep-ip" : "192.168.20.121"
              },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/sw2']", "vtep-ip" : "192.168.20.102"
             }
           ]
       }
    }

def rpc_create_Fabric_Link1():

    return {
      "input" : {
           "source-fabric": 'fabric:1',
           "source-fabric-port": NODE_ID_OVSDB + "/bridge/sw1" + ' - ' + "sw1-eth1",
           "dest-fabric":'fabric:2',
           "dest-fabric-port": NODE_ID_OVSDB + "/bridge/sw2" + ' - ' + "sw2-eth1",
       }
    }

def rpc_create_Fabric_Link2():

    return {
      "input" : {
           "source-fabric": 'fabric:2',
           "source-fabric-port": NODE_ID_OVSDB + "/bridge/sw2" + ' - ' + "sw2-eth1",
           "dest-fabric":'fabric:1',
           "dest-fabric-port": NODE_ID_OVSDB + "/bridge/sw1" + ' - ' + "sw1-eth1",
       }
    }

def pause():
    print "press Enter key to continue..."
    raw_input()

if __name__ == "__main__":
    # Launch main menu

    behavior = "policy-driven"
    #behavior = "normal"
    if len(sys.argv) > 1:
        if sys.argv[1] == "normal":
            behavior = "normal"

    # Some sensible defaults
    controller = os.environ.get('ODL')
    if controller == None:
        sys.exit("No controller set.")

    print "get ovsdb node-id"
    ovsdb_topo = get(controller, DEFAULT_PORT,OPER_OVSDB_TOPO)["topology"]
    for topo_item in ovsdb_topo:
        if topo_item["node"] is not None:
            for ovsdb_node in topo_item["node"]:
                if ovsdb_node.has_key("ovsdb:ovs-version"):
                    #uuid_ovsdb = ovsdb_node["node-id"][13:]
                    NODE_ID_OVSDB = ovsdb_node["node-id"]
                    print NODE_ID_OVSDB

    print "compose fabric1"
    post(controller, DEFAULT_PORT, rpc_compose_fabric_uri(), rpc_compose_fabric_data1("fabric:1", behavior), True)

    print "compose fabric2"
    post(controller, DEFAULT_PORT, rpc_compose_fabric_uri(), rpc_compose_fabric_data2("fabric:2", behavior), True)

    print "creat link for fabrics"
    post(controller, DEFAULT_PORT, URI_ADD_FABRIC_LINK, rpc_create_Fabric_Link1(), True)
    post(controller, DEFAULT_PORT, URI_ADD_FABRIC_LINK, rpc_create_Fabric_Link2(), True)

