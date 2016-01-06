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

UUID_EP1 = '75a4451e-eed0-4645-9194-64454bda2902'
UUID_EP2 = 'ad08c19c-32cc-4cee-b902-3f4919f51bbc'
UUID_EP3 = 'cae54555-7957-4d26-8515-7f2d1de5da55'
UUID_EP4 = '4bc83eb7-4147-435c-8e0f-a546288fd639'
NODE_ID_OVSDB = ''

def rpc_compose_fabric_uri():
    return "/restconf/operations/fabric:compose-fabric"

def rpc_compose_fabric_data():
    return {
      "input" : {
           "name": "first fabric",
           "type":"VXLAN",
           "device-nodes" : [
             {
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s1']","vtep-ip":"192.168.20.101"
              },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s2']","vtep-ip":"192.168.20.102"
             },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s3']","vtep-ip":"192.168.20.103"
             }
           ]
       }
    }

def rpc_add_node_to_fabric_uri():
    return "/restconf/operations/fabric:add-node-to-fabric"

def rpc_add_node_to_fabric_data(name, vtep):
    return {
      "input" : {
           "fabric-id": "fabric:1",
            "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/" + name + "']",
            "vtep-ip":vtep
           }
       }

def rpc_rm_node_from_fabric_uri():
    return "/restconf/operations/fabric:rm-node-from-fabric"

def rpc_rm_node_from_fabric_data(name):
    return {
      "input" : {
           "fabric-id": "fabric:1",
            "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/" + name + "']"
           }
       }

def rpc_create_logic_switch_uri():
    return "/restconf/operations/fabric-service:create-logic-switch"

def rpc_create_logic_switch_data(name, vni):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "name":name,
           "vni":vni
         }
    }

def rpc_rm_logic_switch_uri():
    return "/restconf/operations/fabric-service:rm-logic-switch"

def rpc_rm_logic_switch_data(name, vni):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "node-id":name
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

def rpc_rm_logic_router_uri():
    return "/restconf/operations/fabric-service:rm-logic-router"

def rpc_rm_logic_router_data(name):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "node-id":name
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

def rpc_register_endpoint_data1():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "endpoint-uuid":UUID_EP1,
           "mac-address":"62:02:1a:00:b7:11",
           "ip-address":"172.16.1.11",
           "gateway":"172.16.1.1",
            "logic-location": {
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
           "mac-address":"62:02:1a:00:b7:12",
           "ip-address":"172.16.1.12",
           "gateway":"172.16.1.1",
            "logic-location": {
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
           "mac-address":"62:02:1a:00:b7:13",
           "ip-address":"172.16.1.13",
           "gateway":"172.16.1.1",
            "logic-location": {
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
           "mac-address":"62:02:1a:00:b7:22",
           "ip-address":"172.16.2.22",
           "gateway":"172.16.2.1",
            "logic-location": {
                "node-id":"vswitch-2",
                "tp-id":"vswitch-2-p-1"
            }
       }
    }

def rpc_unregister_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:unregister-endpoint"

def rpc_unregister_endpoint_data():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "ids":(UUID_EP1,UUID_EP2,UUID_EP3,UUID_EP4)
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
                "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s1']",
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s1']/network-topology:termination-point[network-topology:tp-id='s1-eth1']",
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
                "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s1']",
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s1']/network-topology:termination-point[network-topology:tp-id='s1-eth2']",
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
                "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s2']",
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s2']/network-topology:termination-point[network-topology:tp-id='s2-eth1']",
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
                "node-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s2']",
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s2']/network-topology:termination-point[network-topology:tp-id='s2-eth2']",
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

def rpc_rm_gateway_uri():
    return "/restconf/operations/fabric-service:rm-gateway"

def rpc_rm_gateway_data(ipaddr):
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "ip-address":ipaddr,
           "logic-router":"vrouter-1"
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

    print "compose fabric"
    post(controller, DEFAULT_PORT, rpc_compose_fabric_uri(), rpc_compose_fabric_data(), True)

    print "create_logic_switch ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("vswitch-1", 1), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("vswitch-2", 2), True)

    print "create_logic_router ..."
    pause() 
    post(controller, DEFAULT_PORT, rpc_create_logic_router_uri(), rpc_create_logic_router_data("vrouter-1"), True)

    print "create_logic_port ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-2"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-3"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-2", "vswitch-2-p-1"), True)

    print "registering endpoints ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data1(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data2(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data3(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data4(), True)

    print "locate endpoints ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data1(), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data2(), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data3(), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data4(), True)

    print "create gateway ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("172.16.1.1", "172.16.1.0/24", "vswitch-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("172.16.2.1", "172.16.2.0/24", "vswitch-2"), True)

    print "append a device to fabric ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_add_node_to_fabric_uri(), rpc_add_node_to_fabric_data("s4", "192.168.20.104"), True)

    print "remove a device from fabric ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_rm_node_from_fabric_uri(), rpc_rm_node_from_fabric_data("s3"), True)

    print "unregister endpoints ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_unregister_endpoint_uri(), rpc_unregister_endpoint_data(), True)


    print "remove gateway ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_rm_gateway_uri(), rpc_rm_gateway_data("172.16.1.1"), True)
    post(controller, DEFAULT_PORT, rpc_rm_gateway_uri(), rpc_rm_gateway_data("172.16.2.1"), True)

    print "remove logic switch ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_rm_logic_switch_uri(), rpc_rm_logic_switch_data("vswitch-1", 1), True)
    post(controller, DEFAULT_PORT, rpc_rm_logic_switch_uri(), rpc_rm_logic_switch_data("vswitch-2", 2), True)

    print "remove logic router ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_rm_logic_router_uri(), rpc_rm_logic_router_data("vrouter-1"), True)
