#!/usr/bin/python
import argparse
import requests,json
from requests.auth import HTTPBasicAuth
from subprocess import call
import time
import sys
import os
from faas_topo import FLOATING_IP_1, FLOATING_IP_GW_MAC


DEFAULT_PORT='8181'

USERNAME='admin'
PASSWORD='admin'

OPER_OVSDB_TOPO='/restconf/operational/network-topology:network-topology/topology/ovsdb:1'
OPER_FABRIC2_TOPO='/restconf/operational/network-topology:network-topology/topology/fabric:2'

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

UUID_EXT_GW = '18221321-04b6-47e1-97c1-2c1e604058ab'

NODE_ID_OVSDB = ''
NODE_ID_F2_LR = ''

def rpc_create_logic_switch_uri():
    return "/restconf/operations/fabric-service:create-logical-switch"

def rpc_create_logic_switch_data(fabricId, name, vni):
    return {
        "input" : {
           "fabric-id": fabricId,
           "name":name,
           "vni":vni
         }
    }

def rpc_create_external_switch_data(fabricId, name):
    return {
        "input" : {
           "fabric-id" : fabricId,
           "name" : name,
           "external" : True
         }
    }

def rpc_create_logic_router_uri():
    return "/restconf/operations/fabric-service:create-logical-router"

def rpc_create_logic_router_data(fabricId, name):
    return {
        "input" : {
           "fabric-id": fabricId,
           "name":name
        }
    }

def rpc_create_logic_port_uri():
    return "/restconf/operations/fabric-service:create-logical-port"

def rpc_create_logic_port_data(fabricId, deviceName, portName):
    return {
        "input" : {
           "fabric-id": fabricId,
           "name":portName,
           "logical-device":deviceName
       }
    }

def rpc_register_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:register-endpoint"

def rpc_reg_external_gw_ep_data(fabricid, epId, ipaddr, pDevice, pPort):
    return {
        "input" : {
           "fabric-id": fabricid,
           "endpoint-uuid":epId,
           "mac-address":FLOATING_IP_GW_MAC,
           "ip-address":ipaddr,
            "logical-location": {
                "node-id":"lsw-external",
                "tp-id":"lsw-external-p-1"
            },
           "location" : {
                "node-ref": OVS_BR_P % (NODE_ID_OVSDB + "/bridge/" + pDevice),
                "tp-ref": OVS_TP_P % (NODE_ID_OVSDB + "/bridge/" + pDevice, pPort),
                "access-type":"exclusive"
           }
       }
    }


def rpc_locate_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:locate-endpoint"

def rpc_create_gateway_uri():
    return "/restconf/operations/fabric-service:create-gateway"


def rpc_create_gateway_data(fabricId, ipaddr, network, switchName):
    return {
      "input" : {
           "fabric-id": fabricId,
           "ip-address":ipaddr,
           "network":network,
           "logical-router":NODE_ID_F2_LR,
           "logical-switch":switchName
       }
    }

def rpc_add_route_uri():
    return "/restconf/operations/fabric-service:add-static-route"

def rpc_add_route(fabricId, destIp, nexthop, interface):
    return {
      "input" : {
           "fabric-id" : fabricId,
           "node-id" : NODE_ID_F2_LR,
           "route" : [
               { "destination-prefix" : destIp,
                 "next-hop" : nexthop,
                 "outgoing-interface" : interface
               }
           ]
        }
    }

def rpc_add_function_uri():
    return "/restconf/operations/fabric-service:add-port-function"

def rpc_add_function_data(fabricId, extIp, interIp):
    return {
      "input" : {
           "fabric-id" : fabricId,
           "logical-device" : NODE_ID_F2_LR,
           "logical-port" : "192.168.1.254",
           "port-function" : {
               "ip-mapping-entry" :[
                     { "external-ip" : extIp,
                       "internal-ip" : interIp
                     }
               ]
            }
        }
    }

def get_acl_uri():
    return "/restconf/config/ietf-access-control-list:access-lists/acl/ietf-access-control-list:ipv4-acl/acl-icmp-allow"

def get_acl_data():
    return {
        "ietf-access-control-list:acl": [
            {
            "acl-name":"acl-icmp-allow",
            "acl-type":"ipv4-acl",
            "access-list-entries":{
                "ace" :[
                    {
                    "rule-name":"rule-icmp-allow",
                    "matches":{
                        "source-port-range":{
                            "lower-port":1,
                            },
                        "destination-port-range":{
                            "lower-port":1
                            },
                        "protocol":1
                        },
                        "actions": {
                            "permit" : "true"
                        }
                    }
                ]}
            }
            ]
    }

def rpc_add_acl_uri():
    return "/restconf/operations/fabric-service:add-acl"

def rpc_add_switch_acl_data(fabicId, switchName, aclName):
    return {
      "input" : {
           "fabric-id": fabicId,
           "logical-device":switchName,
           "acl-name":aclName
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
                if ovsdb_node.has_key("ovsdb:ovs-version"):
                    #uuid_ovsdb = ovsdb_node["node-id"][13:]
                    NODE_ID_OVSDB = ovsdb_node["node-id"]
                    print NODE_ID_OVSDB

    print "get logical router node-id on fabric:2"
    ovsdb_topo = get(controller, DEFAULT_PORT,OPER_FABRIC2_TOPO)["topology"]
    for topo_item in ovsdb_topo:
        if topo_item["node"] is not None:
            for ovsdb_node in topo_item["node"]:
                if ovsdb_node.has_key("fabric-service:lr-attribute"):
                    #uuid_ovsdb = ovsdb_node["node-id"][13:]
                    NODE_ID_F2_LR = ovsdb_node["node-id"]
                    print NODE_ID_F2_LR


    #----------------------------------- NAT -------------------------------------
    print "enable NAT Function..."
    pause()
    
    # create external logical switch
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_external_switch_data("fabric:2", "lsw-external"), True)
    # create logical port
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("fabric:2", "lsw-external", "lsw-external-p-1"), True)
    # register external gateway endpoint
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_reg_external_gw_ep_data("fabric:2", UUID_EXT_GW, "192.168.1.1", "sw2", "sw2-eth2"), True)
    # create outgoing logical port
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("fabric:2", "192.168.1.254", "192.168.1.0/24", "lsw-external"), True)
    # add default routing
    post(controller, DEFAULT_PORT, rpc_add_route_uri(), rpc_add_route("fabric:2", "192.168.2.0/24", "192.168.1.1", "192.168.1.254"), True)
    # add NAT function
    post(controller, DEFAULT_PORT, rpc_add_function_uri(), rpc_add_function_data("fabric:2", FLOATING_IP_1, "10.0.36.8"), True)
    # add acl to enable all traffic to external network
    put(controller, DEFAULT_PORT, get_acl_uri(), get_acl_data(), True)
    post(controller, DEFAULT_PORT, rpc_add_acl_uri(), rpc_add_switch_acl_data("fabric:2", "lsw-external","acl-icmp-allow"), True)


