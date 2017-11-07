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


OPER_NODES='/restconf/operational/opendaylight-inventory:nodes/'
CONF_TENANT='/restconf/config/policy:tenants'
OPER_CE_TOPO='/restconf/operational/network-topology:network-topology/topology/faas:physical'

UUID_EP3 = 'cae54555-7957-4d26-8515-7f2d1de5da55'
UUID_EP4 = '4bc83eb7-4147-435c-8e0f-a546288fd639'
NODE_ID_CE_TOR1= ''
NODE_ID_CE_TOR2= ''

def get_jsondata(host, port, uri):
    url = 'http://' + host + ":" + port + uri
    #print url
    r = requests.get(url, auth=HTTPBasicAuth(USERNAME, PASSWORD))
    jsondata=json.loads(r.text)
    return jsondata

def get(host, port, uri):
    url='http://'+host+":"+port+uri
    r = requests.get(url, auth=HTTPBasicAuth(USERNAME, PASSWORD))
    return r

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

def rpc_create_logic_switch_uri():
    return "/restconf/operations/fabric-service:create-logical-switch"

def rpc_create_logic_switch_data(lswname, vni):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "name":lswname,
           "vni":vni
         }
    }

def rpc_create_logic_router_uri():
    return "/restconf/operations/fabric-service:create-logical-router"

def rpc_create_logic_router_data(lrname):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "name":lrname
        }
    }

def rpc_create_logic_port_uri():
    return "/restconf/operations/fabric-service:create-logical-port"

def rpc_create_logic_port_data(ldName, portName):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "name":portName,
           "logical-device":ldName
       }
    }

def rpc_register_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:register-endpoint"

def rpc_register_endpoint_data3():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "endpoint-uuid":UUID_EP3,
           "mac-address":"c8:1f:be:37:1b:6a",
           "ip-address":"192.168.5.20",
           "gateway":"192.168.5.1",
            "logical-location": {
                "node-id":"vswitch-2",
                "tp-id":"vswitch-2-p-1"
            }
       }
    }

def rpc_register_endpoint_data4():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "endpoint-uuid":UUID_EP4,
           "mac-address":"ac:61:75:e4:c9:26",
           "ip-address":"192.168.5.21",
           "gateway":"192.168.5.1",
            "logical-location": {
                "node-id":"vswitch-2",
                "tp-id":"vswitch-2-p-2"
            }
       }
    }

def rpc_locate_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:locate-endpoint"

def rpc_locate_endpoint_data3():
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "endpoint-id":UUID_EP3,
            "location": {
                "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='faas:physical']/network-topology:node[network-topology:node-id='" + NODE_ID_CE_TOR1 + "']",
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='faas:physical']/network-topology:node[network-topology:node-id='" + NODE_ID_CE_TOR1 + "']/network-topology:termination-point[network-topology:tp-id='10GE1/0/1']",
                "access-type":"vlan",
                "access-segment":"100"
            }
       }
    }

def rpc_locate_endpoint_data4():
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "endpoint-id":UUID_EP4,
            "location": {
                "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='faas:physical']/network-topology:node[network-topology:node-id='" + NODE_ID_CE_TOR2 + "']",
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='faas:physical']/network-topology:node[network-topology:node-id='" + NODE_ID_CE_TOR2 + "']/network-topology:termination-point[network-topology:tp-id='10GE1/0/1']",
                "access-type":"vlan",
                "access-segment":"100"
            }
       }
    }

def pause():
    print "press Enter key to continue..."
    raw_input()

if __name__ == "__main__":
    # Launch main menu

    # Some sensible defaults
    controller=os.environ.get('ODL')
    if controller == None:
        sys.exit("No controller set.")

    print "get CE node-id"
    ce_topo = get_jsondata(controller, DEFAULT_PORT,OPER_CE_TOPO)["topology"]
    for topo_item in ce_topo:
        if topo_item["node"] is not None:
            for ce_node in topo_item["node"]:
                if ce_node.has_key("fabric-vxlan-device-adapter:credential"):
                    cre = ce_node["fabric-vxlan-device-adapter:credential"]
                    if cre["sysname"] == "ce6801":
                        NODE_ID_CE_TOR1 = ce_node["node-id"]
                        print "ce6801 node-id= "+ NODE_ID_CE_TOR1
                    if cre["sysname"] == "ce6802":
                        NODE_ID_CE_TOR2 = ce_node["node-id"]
                        print "ce6802 node-id= "+NODE_ID_CE_TOR2


    print "create_logic_switch_2"
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("vswitch-2", 200), True)

    print "create_logic_port"
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-2", "vswitch-2-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-2", "vswitch-2-p-2"), True)

    print "registering endpoints"
    pause()
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data3(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data4(), True)

    print "locate endpoints"
    pause()
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data3(), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data4(), True)
