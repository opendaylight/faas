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

def wait_for_sff_in_datastore(url):
    for i in xrange(30):
        resp=get(controller, DEFAULT_PORT, url)
        if ('192.168.50.71' in resp.text) and ('192.168.50.73' in resp.text):
            break
        time.sleep(3)
    if ('192.168.50.71' not in resp.text):
        print "ERROR: SFF1 has not been initialized!"
        sys.exit(1)
    if ('192.168.50.73' not in resp.text):
        print "ERROR: SFF2 has not been initialized!"
        sys.exit(1)




def get_service_functions_uri():
    return "/restconf/config/service-function:service-functions"

def get_service_functions_data():
    return {
    "service-functions": {
        "service-function": [
            {
                "name": "firewall-72",
                "ip-mgmt-address": "192.168.50.72",
                "type": "service-function-type:firewall",
                "nsh-aware": "true",
                "sf-data-plane-locator": [
                    {
                        "name": "2",
                        "port": 6633,
                        "ip": "192.168.50.72",
                        "transport": "service-locator:vxlan-gpe",
                        "service-function-forwarder": "SFF1"
                    }
                ]
            },
            {
                "name": "dpi-74",
                "ip-mgmt-address": "192.168.50.74",
                "type": "service-function-type:dpi",
                "nsh-aware": "true",
                "sf-data-plane-locator": [
                    {
                        "name": "3",
                        "port": 6633,
                        "ip": "192.168.50.74",
                        "transport": "service-locator:vxlan-gpe",
                        "service-function-forwarder": "SFF2"
                    }
                ]
            }
        ]
    }
}

def get_service_function_forwarders_uri():
    return "/restconf/config/service-function-forwarder:service-function-forwarders"

def get_service_function_forwarders_data():
    return {
    "service-function-forwarders": {
        "service-function-forwarder": [
            {
                "name": "SFF1",
                "service-node": "OVSDB2",
                "service-function-forwarder-ovs:ovs-bridge": {
                    "bridge-name": "sw2"
                },
                "service-function-dictionary": [
                    {
                        "name": "firewall-72",
                        "sff-sf-data-plane-locator": {
                            "sf-dpl-name": "2",
                            "sff-dpl-name": "sfc-tun2"
                        }
                    }
                ],
                "sff-data-plane-locator": [
                    {
                        "name": "sfc-tun2",
                        "data-plane-locator": {
                            "transport": "service-locator:vxlan-gpe",
                            "port": 6633,
                            "ip": "192.168.50.71"
                        },
                        "service-function-forwarder-ovs:ovs-options": {
                            "remote-ip": "flow",
                            "dst-port": "6633",
                            "key": "flow",
                            "nsp": "flow",
                            "nsi": "flow",
                            "nshc1": "flow",
                            "nshc2": "flow",
                            "nshc3": "flow",
                            "nshc4": "flow"
                        }
                    }
                ]
            },
            {
                "name": "SFF2",
                "service-node": "OVSDB2",
                "service-function-forwarder-ovs:ovs-bridge": {
                    "bridge-name": "sw4"
                },
                "service-function-dictionary": [
                    {
                        "name": "dpi-74",
                        "sff-sf-data-plane-locator": {
                            "sf-dpl-name": "3",
                            "sff-dpl-name": "sfc-tun4"
                        }
                    }
                ],
                "sff-data-plane-locator": [
                    {
                        "name": "sfc-tun4",
                        "data-plane-locator": {
                            "transport": "service-locator:vxlan-gpe",
                            "port": 6633,
                            "ip": "192.168.50.73"
                        },
                        "service-function-forwarder-ovs:ovs-options": {
                            "remote-ip": "flow",
                            "dst-port": "6633",
                            "key": "flow",
                            "nsp": "flow",
                            "nsi": "flow",
                            "nshc1": "flow",
                            "nshc2": "flow",
                            "nshc3": "flow",
                            "nshc4": "flow"
                        }
                    }
                ]
            }
        ]
    }
}

def get_service_function_chains_uri():
    return "/restconf/config/service-function-chain:service-function-chains/"

def get_service_function_chains_data():
    return {
    "service-function-chains": {
        "service-function-chain": [
            {
                "name": "SFCGBP",
                "symmetric": "true",
                "sfc-service-function": [
                    {
                        "name": "firewall-abstract1",
                        "type": "service-function-type:firewall"
                    },
                    {
                        "name": "dpi-abstract1",
                        "type": "service-function-type:dpi"
                    }
                ]
            }
        ]
    }
}

def get_service_function_paths_uri():
    return "/restconf/config/service-function-path:service-function-paths/"

def get_service_function_paths_data():
    return {
    "service-function-paths": {
        "service-function-path": [
            {
                "name": "SFCGBP-Path",
                "service-chain-name": "SFCGBP",
                "starting-index": 255,
                "symmetric": "true"

            }
        ]
    }
}

# Main definition - constants

# =======================
#     MENUS FUNCTIONS
# =======================

# Main menu

# =======================
#      MAIN PROGRAM
# =======================

# Main Program

def get_tunnel_oper_uri():
    return "/restconf/operational/opendaylight-inventory:nodes/"

def get_topology_oper_uri():
    return "/restconf/operational/network-topology:network-topology/topology/ovsdb:1/"

def rpc_compose_fabric_uri():
    return "/restconf/operations/fabric:compose-fabric"

def rpc_compose_fabric_data():
    return {
      "input" : {
           "name": "first fabric",
           "type":"VXLAN",
           "device-nodes" : [
             {
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW1 + "']","vtep-ip":"192.168.50.70"
              },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW6 + "']","vtep-ip":"192.168.50.75"
             }
           ]
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

def rpc_register_endpoint_data1():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "endpoint-uuid":UUID_EP1,
           "mac-address":"00:00:00:00:35:02",
           "ip-address":"10.0.35.2",
           "gateway":"10.0.35.1",
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
           "mac-address":"00:00:00:00:35:03",
           "ip-address":"10.0.35.3",
           "gateway":"10.0.35.1",
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
           "mac-address":"00:00:00:00:35:04",
           "ip-address":"10.0.35.4",
           "gateway":"10.0.35.1",
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
           "mac-address":"00:00:00:00:36:04",
           "ip-address":"10.0.36.4",
           "gateway":"10.0.36.1",
            "logic-location": {
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
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW1 + "']/network-topology:termination-point[network-topology:tp-id='vethl-h35_2']",
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
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW1 + "']/network-topology:termination-point[network-topology:tp-id='vethl-h35_3']",
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
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW6 + "']/network-topology:termination-point[network-topology:tp-id='vethl-h35_4']",
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
                "tp-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB_SW6 + "']/network-topology:termination-point[network-topology:tp-id='vethl-h36_4']",
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

    print "Do some faas special functions"
    os.system("sh demo-faas/scripts_faas.sh")
    # Some sensible defaults
    controller=os.environ.get('ODL')
    if controller == None:
        sys.exit("No controller set.")

    pause()
    print "Contacting controller at %s" % controller
    print "waiting for manager on SFFs..."
    wait_for_sff_in_datastore(get_topology_oper_uri())
    print "sending service functions"
    put(controller, DEFAULT_PORT, get_service_functions_uri(), get_service_functions_data(), True)
    print "sending service function forwarders"
    put(controller, DEFAULT_PORT, get_service_function_forwarders_uri(), get_service_function_forwarders_data(), True)
    print "waiting for switches on SFFs..."
    wait_for_sff_in_datastore(get_tunnel_oper_uri())
    print "sending service function chains"
    put(controller, DEFAULT_PORT, get_service_function_chains_uri(), get_service_function_chains_data(), True)
    print "sending service function paths"
    put(controller, DEFAULT_PORT, get_service_function_paths_uri(), get_service_function_paths_data(), True)
    print "create service path"
    post(controller, DEFAULT_PORT, get_create_service_path_uri(), get_create_service_path_data(), True)

    pause()
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

    pause()
    print "compose fabric"
    post(controller, DEFAULT_PORT, rpc_compose_fabric_uri(), rpc_compose_fabric_data(), True)

    pause()
    print "create_logic_switch"
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("vswitch-1", 1), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("vswitch-2", 2), True)

    pause()
    print "create_logic_router"
    post(controller, DEFAULT_PORT, rpc_create_logic_router_uri(), rpc_create_logic_router_data("vrouter-1"), True)

    pause()
    print "create_logic_port"
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-2"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-1", "vswitch-1-p-3"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("vswitch-2", "vswitch-2-p-1"), True)

    pause()
    print "registering endpoints"
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data1(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data2(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data3(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data4(), True)

    pause()
    print "locate endpoints"
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data1(), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data2(), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data3(), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data4(), True)

    pause()
    print "create gateway"
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("10.0.35.1", "10.0.35.0/24", "vswitch-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("10.0.36.1", "10.0.36.0/24", "vswitch-2"), True)


