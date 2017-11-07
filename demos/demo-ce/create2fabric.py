#!/usr/bin/python
import argparse
import requests,json
from requests.auth import HTTPBasicAuth
from subprocess import call
from infrastructure_config import *
import time
import sys
import os

DEFAULT_PORT='8181'

USERNAME='admin'
PASSWORD='admin'

# OPER_OVSDB_TOPO='/restconf/operational/network-topology:network-topology/topology/ovsdb:1'
OPER_CE_TOPO='/restconf/operational/network-topology:network-topology/topology/faas:physical'

def get(host, port, uri):
    url = 'http://' + host + ":" + port + uri
    #print url
    r = requests.get(url, auth=HTTPBasicAuth(USERNAME, PASSWORD))
    jsondata=json.loads(r.text)
    return jsondata

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
 

def get_all_ovs_bridge(controller):
    bridges = list()
    ovsdb_topo = get(controller, DEFAULT_PORT, OPER_OVSDB_TOPO)["topology"]

    for topo_item in ovsdb_topo:
        if topo_item["node"] is not None:
            for ce_node in topo_item["node"]:
                if ce_node.has_key("fabric-vxlan-device-adapter:credential"): #it's a ce node
                     credential = ce_node["fabric-vxlan-device-adapter:credential"]
                     bridges.append({'id':ce_node["node-id"], "vtep-ip":credential["vtepip"]})
                     print credential["vtepip"]
    return bridges

# Main Program

NODE_ID_OVSDB = ''
DEVICE_REF_PATTERN = "/network-topology:network-topology/network-topology:topology[network-topology:topology-id='faas:physical']/network-topology:node[network-topology:node-id='%s']"

def rpc_compose_fabric_uri():
    return "/restconf/operations/fabric:compose-fabric"

def rpc_compose_fabric_data(behavior):
    devNodes = list()
    
    for switch in switches:
        if switch["type"] == "ce":
            devNodes.append({"device-ref" : DEVICE_REF_PATTERN % switch['nodeid'], "vtep-ip":switch['vtep']})

    return {
      "input" : {
           "name": "first fabric",
           "type":"VXLAN",
           "options":{
               "traffic-behavior":behavior,
               "capability-supported" : ['ip-mapping', 'acl-redirect']
           },
           "device-nodes" : devNodes
       }
    }


if __name__ == "__main__":


    behavior = "policy-driven"
    if len(sys.argv) > 1:
        if sys.argv[1] == "normal":
            behavior = "normal"
       

    controller=os.environ.get('ODL')
    if controller == None:
        sys.exit("No controller set.")

    print "get ce node-id"
    ovsdb_topo = get(controller, DEFAULT_PORT,OPER_CE_TOPO)["topology"]
    for topo_item in ovsdb_topo:
        if topo_item["node"] is not None:
            for ovsdb_node in topo_item["node"]:
                if ovsdb_node.has_key("fabric-vxlan-device-adapter:credential"):
                    cre = ovsdb_node["fabric-vxlan-device-adapter:credential"]
                    switchname = cre["sysname"]
                    vtepip = cre["vtepip"]
                    for switch in switches:
                        if switchname == switch["name"]:
                            if switch["type"] == "ce":
                                switch["nodeid"] = ovsdb_node["node-id"]
                                switch["vtep"] = vtepip 

    print "compose fabric"
    post(controller, DEFAULT_PORT, rpc_compose_fabric_uri(), rpc_compose_fabric_data(behavior), True)
