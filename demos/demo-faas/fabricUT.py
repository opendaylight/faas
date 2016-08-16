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
OPER_OVSDB_TOPO='/restconf/operational/network-topology:network-topology/topology/ovsdb:1'

UUID_EP1 = '75a4451e-eed0-4645-9194-64454bda2902'
UUID_EP2 = 'ad08c19c-32cc-4cee-b902-3f4919f51bbc'
UUID_EP3 = 'cae54555-7957-4d26-8515-7f2d1de5da55'
UUID_EP4 = '4bc83eb7-4147-435c-8e0f-a546288fd639'
NODE_ID_OVSDB_SW1 = ''
NODE_ID_OVSDB_SW6 = ''

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

def rpc_create_logic_switch_data(name, vni):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "name":name,
           "vni":vni
         }
    }

def rpc_create_logic_router_uri():
    return "/restconf/operations/fabric-service:create-logical-router"

def rpc_create_logic_router_data(name):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "name":name
        }
    }

def rpc_create_logic_port_uri():
    return "/restconf/operations/fabric-service:create-logical-port"

def rpc_create_logic_port_data(deviceName, portName):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "name":portName,
           "logical-device":deviceName
       }
    }

def rpc_register_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:register-endpoint"

def rpc_register_endpoint_data1():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "endpoint-uuid":UUID_EP1,
           "mac-address":"00:00:00:00:35:02",
           "ip-address":"10.0.35.2",
           "gateway":"10.0.35.1",
            "logical-location": {
                "node-id":"vswitch-1",
                "tp-id":"vswitch-1-p-1"
            }
       }
    }

def rpc_register_endpoint_data2():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "endpoint-uuid":UUID_EP2,
           "mac-address":"00:00:00:00:35:03",
           "ip-address":"10.0.35.3",
           "gateway":"10.0.35.1",
            "logical-location": {
                "node-id":"vswitch-1",
                "tp-id":"vswitch-1-p-2"
            }
       }
    }

def rpc_register_endpoint_data3():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "endpoint-uuid":UUID_EP3,
           "mac-address":"00:00:00:00:35:04",
           "ip-address":"10.0.35.4",
           "gateway":"10.0.35.1",
            "logical-location": {
                "node-id":"vswitch-1",
                "tp-id":"vswitch-1-p-3"
            }
       }
    }

def rpc_register_endpoint_data4():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "endpoint-uuid":UUID_EP4,
           "mac-address":"00:00:00:00:36:04",
           "ip-address":"10.0.36.4",
           "gateway":"10.0.36.1",
            "logical-location": {
                "node-id":"vswitch-2",
                "tp-id":"vswitch-2-p-1"
            }
       }
    }

def rpc_locate_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:locate-endpoint"

def rpc_locate_endpoint_data1():
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "endpoint-id":UUID_EP1,
            "location": {
                "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW1 + "']",
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW1 + "']/network-topology:termination-point[network-topology:tp-id='vethl-h35-2']",
                "access-type":"exclusive"
            }
       }
    }

def rpc_locate_endpoint_data2():
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "endpoint-id":UUID_EP2,
            "location": {
                "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW1 + "']",
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW1 + "']/network-topology:termination-point[network-topology:tp-id='vethl-h35-3']",
                "access-type":"exclusive"
            }
       }
    }

def rpc_locate_endpoint_data3():
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "endpoint-id":UUID_EP3,
            "location": {
                "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW6 + "']",
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW6 + "']/network-topology:termination-point[network-topology:tp-id='vethl-h35-4']",
                "access-type":"exclusive"
            }
       }
    }

def rpc_locate_endpoint_data4():
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "endpoint-id":UUID_EP4,
            "location": {
                "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW6 + "']",
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW6 + "']/network-topology:termination-point[network-topology:tp-id='vethl-h36-4']",
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
           "logical-router":"vrouter-1",
           "logical-switch":switchName
       }
    }

def get_create_service_path_uri():
    return "/restconf/operations/rendered-service-path:create-rendered-path"

def get_create_service_path_data():
    return {
      "input" : {
           "parent-service-function-path": "SFCGBP-Path"
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

    print "get ovsdb node-id"
    ovsdb_topo = get_jsondata(controller, DEFAULT_PORT,OPER_OVSDB_TOPO)["topology"]
    for topo_item in ovsdb_topo:
        if topo_item["node"] is not None:
            for ovsdb_node in topo_item["node"]:
                if ovsdb_node.has_key("ovsdb:bridge-name"):
                    #uuid_ovsdb = ovsdb_node["node-id"][13:]
                    if ovsdb_node["ovsdb:bridge-name"] == "sw1":
                        NODE_ID_OVSDB_SW1 = ovsdb_node["node-id"]
                        print "sw1 node-id= "+NODE_ID_OVSDB_SW1
                    if ovsdb_node["ovsdb:bridge-name"] == "sw6":
                        NODE_ID_OVSDB_SW6 = ovsdb_node["node-id"]
                        print "sw6 node-id= "+NODE_ID_OVSDB_SW6


    print "create_logic_switch"
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("vswitch-1", 1), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("vswitch-2", 2), True)

    print "create_logic_router"
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_router_uri(), rpc_create_logic_router_data("vrouter-1"), True)

    print "create_logic_port"
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-2"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-3"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-2", "vswitch-2-p-1"), True)

    print "registering endpoints"
    pause()
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data1(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data2(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data3(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data4(), True)

    print "locate endpoints"
    pause()
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data1(), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data2(), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data3(), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data4(), True)

    print "create gateway"
    pause()
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("10.0.35.1", "10.0.35.0/24", "vswitch-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("10.0.36.1", "10.0.36.0/24", "vswitch-2"), True)


