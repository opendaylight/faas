#!/usr/bin/python
import argparse
import requests,json
from requests.auth import HTTPBasicAuth
from subprocess import call
import time
import sys
import os
from infrastructure_config import *


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
    

def get_default_gw(ipaddr):
    arr = ipaddr.split(".")
    return "%s.%s.%s.%s" % (arr[0], arr[1], arr[2], "1") 

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
SUBNET_2_LSW = {"10.0.35.1":"vswitch-1", "10.0.36.1":"vswitch-2", "10.0.37.1":"vswitch-3"}
PORTIDX_OF_LSW = {"vswitch-1":1, "vswitch-2":1, "vswitch-3":1}

def rpc_create_logic_switch_uri():
    return "/restconf/operations/fabric-service:create-logic-switch"

def rpc_create_logic_switch_data(name):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "name":name
         }
    }


def rpc_create_logic_router_uri():
    return "/restconf/operations/fabric-service:create-logic-router"

def rpc_create_logic_router_data(name):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "name":name
        }
    }


def rpc_create_logic_port_uri():
    return "/restconf/operations/fabric-service:create-logic-port"

def rpc_create_logic_port_data(deviceName, portName):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "name":portName,
           "logic-device":deviceName
       }
    }

def rpc_register_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:register-endpoint"


BRIDGE_REF_P="/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='%s']"
TP_REF_P="/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='%s']/network-topology:termination-point[network-topology:tp-id='%s']"


def get_nodeid_of_switch(name):
    for switch in switches:
       if switch["name"] == name:
          return switch["nodeid"] 

def rpc_register_endpoint_data(host):
    mac = host["mac"]
    ip = host["ip"].split("/")[0]
    gw = get_default_gw(ip) 
    lsw = SUBNET_2_LSW[gw]
    lport = lsw + "-p-" + str(PORTIDX_OF_LSW[lsw])
    PORTIDX_OF_LSW[lsw] += 1
    bridge = host["switch"]
    port = "vethl-" + str(host["name"])

    nodeid = get_nodeid_of_switch(bridge)

    return {
        "input" : {
            "fabric-id":"fabric:1",
            "mac-address":mac,
            "ip-address":ip,
            "gateway":gw,
            "logic-location" : {
                "node-id": lsw,
                "tp-id": lport
            },
            "location" : {
                "node-ref": BRIDGE_REF_P % (nodeid),
                "tp-ref": TP_REF_P % (nodeid, port),
                "access-type":"exclusive"
            }
        }
    }


def rpc_create_gateway_uri():
    return "/restconf/operations/fabric-service:create-gateway"

def rpc_create_gateway_data(ipaddr, network, switchName):
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "ip-address":ipaddr,
           "network":network,
           "logic-router":"vrouter-1",
           "logic-switch":switchName
       }
    }


def pause():
    print "press Enter key to continue..."
    raw_input()

if __name__ == "__main__":
    # Launch main menu


    # Some sensible defaults
    controller = os.environ.get('ODL')
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

    print "create_logic_switch ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("vswitch-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("vswitch-2"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("vswitch-3"), True)

    print "create_logic_port ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-2"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-3"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-4"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-2", "vswitch-2-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-2", "vswitch-2-p-2"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-2", "vswitch-2-p-3"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-2", "vswitch-2-p-4"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-3", "vswitch-3-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-3", "vswitch-3-p-2"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-3", "vswitch-3-p-3"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-3", "vswitch-3-p-4"), True)

    print "registering endpoints ..."
    pause()
    for host in hosts:
        post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data(host), True)

    print "create_logic_router ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_router_uri(), rpc_create_logic_router_data("vrouter-1"), True)

    print "create gateway ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("10.0.35.1", "10.0.35.1/24", "vswitch-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("10.0.36.1", "10.0.36.1/24", "vswitch-2"), True)
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("10.0.37.1", "10.0.37.1/24", "vswitch-3"), True)

