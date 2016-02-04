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

def rpc_compose_fabric_data(behavior):
    devNodes = list()
    
    for switch in switches:
        if switch["type"] == "gbp":
            devNodes.append({"device-ref" : DEVICE_REF_PATTERN % switch['nodeid'], "vtep-ip":switch['vtep']})

    return {
      "input" : {
           "name": "first fabric",
           "type":"VXLAN",
           "options":{
               "traffic-behavior":behavior
           },
           "device-nodes" : devNodes
       }
    }


if __name__ == "__main__":


    behavior = "need-acl"
    if len(sys.argv) > 1:
        if sys.argv[1] == "normal":
            behavior = "normal"
       

    controller=os.environ.get('ODL')
    if controller == None:
        sys.exit("No controller set.")

    print "get ovsdb node-id"
    ovsdb_topo = get(controller, DEFAULT_PORT,OPER_OVSDB_TOPO)["topology"]
    for topo_item in ovsdb_topo:
        if topo_item["node"] is not None:
            for ovsdb_node in topo_item["node"]:
                if ovsdb_node.has_key("ovsdb:bridge-name"):
                    #uuid_ovsdb = ovsdb_node["node-id"][13:]
                    switchname = ovsdb_node["ovsdb:bridge-name"]
                    for switch in switches:
                        if switchname == switch["name"]:
                            if switch["type"] == "gbp":
                                switch["nodeid"] = ovsdb_node["node-id"]

    SUBNET = os.environ.get("SUBNET")
    for sw_index in range(0, len(switches)-1):
        switches[sw_index]["vtep"] = SUBNET + str(70 + sw_index)

    print "compose fabric"
    post(controller, DEFAULT_PORT, rpc_compose_fabric_uri(), rpc_compose_fabric_data(behavior), True)
