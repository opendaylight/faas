#!/usr/bin/python
import argparse
import requests,json
from requests.auth import HTTPBasicAuth
from subprocess import call
from faas_mininet_config import *
import time
import sys
import os

DEFAULT_PORT='8181'

USERNAME='admin'
PASSWORD='admin'

OPER_OVSDB_TOPO='/restconf/operational/network-topology:network-topology/topology/ovsdb:1'

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
            for ovsdb_node in topo_item["node"]:
                if ovsdb_node.has_key("ovsdb:datapath-id"): #it's a bridge node
                    if ovsdb_node.has_key("ovsdb:bridge-external-ids"): #vtep-ip has been setup
                        print "xxxx"
                        for externalId in ovsdb_node["ovsdb:bridge-external-ids"]:
                            print externalId
                            if externalId["bridge-external-id-key"] == "vtep-ip":
                                bridges.append({'id':ovsdb_node["node-id"], "vtep-ip":externalId["bridge-external-id-value"]})

    return bridges

# Main Program

NODE_ID_OVSDB = ''
DEVICE_REF_PATTERN = "/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='%s']"

def rpc_compose_fabric_uri():
    return "/restconf/operations/fabric:compose-fabric"

def rpc_compose_fabric_data(bridges):
    devNodes = list()
    
    for bridge in bridges:
        devNodes.append({"device-ref" : DEVICE_REF_PATTERN % bridge['id'], "vtep-ip":bridge['vtep-ip']})

    return {
      "input" : {
           "name": "first fabric",
           "type":"VXLAN",
           "device-nodes" : devNodes
       }
    }

if __name__ == "__main__":


    print "get all ovs bridge's node-id"
    bridgeIds = get_all_ovs_bridge(odl_controller) 

    print "compose fabric"
    post(odl_controller, DEFAULT_PORT, rpc_compose_fabric_uri(), rpc_compose_fabric_data(bridgeIds), True)
