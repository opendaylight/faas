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

UUID_EP1 = '75a4451e-eed0-4645-9194-64454bda2902'
UUID_EP2 = 'ad08c19c-32cc-4cee-b902-3f4919f51bbc'
UUID_EP3 = 'cae54555-7957-4d26-8515-7f2d1de5da55'
UUID_EP4 = '4bc83eb7-4147-435c-8e0f-a546288fd639'
UUID_EP5 = 'abc83eb7-4147-435c-8e0f-a546288fd622'
UUID_EP6 = 'dce83eb7-4147-435c-8e0f-a546288fd626'

UUID_EPX_1 = 'b598c42c-e830-4458-b7bb-0c0b61982f42'
UUID_EPX_2 = 'fd8bd945-fe1d-4727-97bf-4572cc017303'

UUID_EXT_GW = '18221321-04b6-47e1-97c1-2c1e604058ab'

NODE_ID_OVSDB = ''

def rpc_compose_fabric_uri():
    return "/restconf/operations/fabric:compose-fabric"

def rpc_compose_fabric_data1():
    return {
      "input" : {
           "name": "first fabric",
           "type":"VXLAN",
           "device-nodes" : [
             {
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s11']","vtep-ip":"192.168.20.111"
              },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s12']","vtep-ip":"192.168.20.112"
             },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s1']","vtep-ip":"192.168.20.101"
             }
           ]
       }
    }

def rpc_compose_fabric_data2():
    return {
      "input" : {
           "name": "second fabric",
           "type":"VXLAN",
           "device-nodes" : [
             {
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s21']","vtep-ip":"192.168.20.121"
              },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s2']","vtep-ip":"192.168.20.102"
             }
           ]
       }
    }

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

def rpc_rm_logic_switch_uri():
    return "/restconf/operations/fabric-service:rm-logical-switch"

def rpc_rm_logic_switch_data(fabricId, name, vni):
    return {
        "input" : {
           "fabric-id": fabricId,
           "node-id":name
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

def rpc_create_logic_port_data_vlan(fabricId, deviceName, portName, vlan):
    return {
        "input" : {
           "fabric-id": fabricId,
           "name":portName,
           "logical-device":deviceName,
           "attribute":{
               "port-layer" : {
                   "layer-2-info":{
                       "access-type":"vlan",
                       "access-segment":vlan
                   }
               }
           }
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
           "mac-address":"62:02:1a:00:b7:12",
           "ip-address":"172.16.1.12",
           "gateway":"172.16.1.1",
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
           "mac-address":"62:02:1a:00:b7:13",
           "ip-address":"172.16.1.13",
           "gateway":"172.16.1.1",
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
           "mac-address":"62:02:1a:00:b7:22",
           "ip-address":"172.16.2.22",
           "gateway":"172.16.2.1",
            "logical-location": {
                "node-id":"vswitch-2",
                "tp-id":"vswitch-2-p-1"
            }
       }
    }

def rpc_register_endpoint_data5():
    return {
        "input" : {
           "fabric-id": "fabric:2",
           "endpoint-uuid":UUID_EP5,
           "mac-address":"62:02:1a:00:b7:14",
           "ip-address":"172.16.1.14",
           "gateway":"172.16.1.1",
            "logical-location": {
                "node-id":"vswitch-1",
                "tp-id":"vswitch-1-p-1"
            }
       }
    }

def rpc_register_endpoint_data6():
    return {
        "input" : {
           "fabric-id": "fabric:2",
           "endpoint-uuid":UUID_EP6,
           "mac-address":"62:02:1a:00:b7:23",
           "ip-address":"172.16.2.23",
           "gateway":"172.16.2.1",
            "logical-location": {
                "node-id":"vswitch-2",
                "tp-id":"vswitch-2-p-1"
            }
       }
    }

def rpc_reg_inter_conn_endpoint_data(fabricId, epId, ipaddr):
    return {
        "input" : {
           "fabric-id": fabricId,
           "endpoint-uuid":epId,
           "mac-address":"80:38:bC:A1:33:c7",
           "ip-address":ipaddr,
            "logical-location": {
                "node-id":"lsw-inter-con",
                "tp-id":"inter-con-p-1"
            }
       }
    }

def rpc_reg_external_gw_ep_data(fabricid, epId, ipaddr, pDevice, pPort):
    return {
        "input" : {
           "fabric-id": fabricid,
           "endpoint-uuid":epId,
           "mac-address":"80:38:bC:A1:33:c8",
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

def rpc_unregister_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:unregister-endpoint"

def rpc_unregister_endpoint_data():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "ids":(UUID_EP1,UUID_EP2,UUID_EP3,UUID_EP4, UUID_EP5, UUID_EP6, UUID_EPX_1, UUID_EPX_2)
       }
    }


def rpc_locate_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:locate-endpoint"

def rpc_locate_endpoint_data(fabricId, epId, pDevice, pPort):
    return {
      "input" : {
           "fabric-id": fabricId,
           "endpoint-id":epId,
            "location": {
                "node-ref": OVS_BR_P % (NODE_ID_OVSDB + "/bridge/" + pDevice),
                "tp-ref": OVS_TP_P % (NODE_ID_OVSDB + "/bridge/" + pDevice, pPort),
                "access-type":"exclusive"
            }
       }
    }

def rpc_port_binding_uri():
    return "/restconf/operations/fabric-service:port-binding-logical-to-fabric"

def rpc_port_binding_data5():
    return {
      "input" : {
           "fabric-id": "fabric:2",
           "logical-device":"vswitch-1",
           "logical-port":"vswitch-1-p-1",
           "fabric-port": NODE_ID_OVSDB + "/bridge/s22 - s22-eth1"
       }
    }

def rpc_port_binding_dev_uri():
    return "/restconf/operations/fabric-service:port-binding-logical-to-device"

def rpc_port_binding_dev_data6():
    return {
      "input" : {
           "fabric-id": "fabric:2",
           "logical-device":"vswitch-2",
           "logical-port":"vswitch-2-p-1",
           "physical-port":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='" + NODE_ID_OVSDB + "/bridge/s22']/network-topology:termination-point[network-topology:tp-id='s22-eth2']"
       }
    }

def rpc_port_binding_dev_data(fabricId, vswitch, logicalport, physicalDev, physicalPort):
    return {
      "input" : {
           "fabric-id": fabricId,
           "logical-device":vswitch,
           "logical-port":logicalport,
           "physical-port":OVS_TP_P % (NODE_ID_OVSDB + "/bridge/" + physicalDev, physicalPort)
       }
    }

def rpc_create_gateway_uri():
    return "/restconf/operations/fabric-service:create-gateway"


def rpc_create_gateway_data(fabricId, ipaddr, network, switchName):
    return {
      "input" : {
           "fabric-id": fabricId,
           "ip-address":ipaddr,
           "network":network,
           "logical-router":"vrouter-1",
           "logical-switch":switchName
       }
    }

def rpc_add_route_uri():
    return "/restconf/operations/fabric-service:add-static-route"

def rpc_add_route(fabricId, destIp, nexthop, interface):
    return {
      "input" : {
           "fabric-id" : fabricId,
           "node-id" : "vrouter-1",
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
           "logical-device" : "vrouter-1",
           "logical-port" : "192.168.1.0",
           "port-function" : {
               "ip-mapping" :[
                     { "external-ip" : extIp,
                       "internal-ip" : interIp
                     }
               ]
            }
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

    print "compose fabric1"
    post(controller, DEFAULT_PORT, rpc_compose_fabric_uri(), rpc_compose_fabric_data1(), True)

    print "compose fabric2"
    post(controller, DEFAULT_PORT, rpc_compose_fabric_uri(), rpc_compose_fabric_data2(), True)

    print "create_logic_switch ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("fabric:1", "vswitch-1", 1), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("fabric:1", "vswitch-2", 2), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("fabric:2", "vswitch-1", 1), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("fabric:2", "vswitch-2", 2), True)

    print "create_logic_router ..."
    pause() 
    post(controller, DEFAULT_PORT, rpc_create_logic_router_uri(), rpc_create_logic_router_data("fabric:1", "vrouter-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_router_uri(), rpc_create_logic_router_data("fabric:2", "vrouter-1"), True)

    print "create_logic_port ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("fabric:1", "vswitch-1", "vswitch-1-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("fabric:1", "vswitch-1", "vswitch-1-p-2"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("fabric:1", "vswitch-1", "vswitch-1-p-3"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("fabric:1", "vswitch-2", "vswitch-2-p-1"), True)

    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("fabric:2", "vswitch-1", "vswitch-1-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("fabric:2", "vswitch-2", "vswitch-2-p-1"), True)

    print "registering endpoints ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data1(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data2(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data3(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data4(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data5(), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_register_endpoint_data6(), True)

    print "locate endpoints ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data("fabric:1", UUID_EP1, "s11", "s11-eth1"), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data("fabric:1", UUID_EP2, "s11", "s11-eth2"), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data("fabric:1", UUID_EP3, "s12", "s12-eth1"), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data("fabric:1", UUID_EP4, "s12", "s12-eth2"), True)

    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data("fabric:2", UUID_EP5, "s21", "s21-eth1"), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data("fabric:2", UUID_EP6, "s21", "s21-eth2"), True)

    #print "binding physical port"
    #pause()
    #post(controller, DEFAULT_PORT, rpc_port_binding_uri(), rpc_port_binding_data5(), True)
    #post(controller, DEFAULT_PORT, rpc_port_binding_dev_uri(), rpc_port_binding_dev_data6(), True)

    #---------------------------------- layer2 connectivity -------------------------------------
    print "create layer2 forwarder..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data_vlan("fabric:1", "vswitch-1", "vswitch-1-p-con", 100), True)
    post(controller, DEFAULT_PORT, rpc_port_binding_dev_uri(), rpc_port_binding_dev_data("fabric:1", "vswitch-1", "vswitch-1-p-con", "s1", "s1-eth1"), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data_vlan("fabric:2", "vswitch-1", "vswitch-1-p-con", 100), True)
    post(controller, DEFAULT_PORT, rpc_port_binding_dev_uri(), rpc_port_binding_dev_data("fabric:2", "vswitch-1", "vswitch-1-p-con", "s2", "s2-eth1"), True)


    print "create gateway ..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("fabric:1", "172.16.1.1", "172.16.1.0/24", "vswitch-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("fabric:1", "172.16.2.1", "172.16.2.0/24", "vswitch-2"), True)

    #----------------------------------- layer3 connectivity -------------------------------------
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("fabric:2", "172.16.1.1", "172.16.1.0/24", "vswitch-1"), True)
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("fabric:2", "172.16.2.1", "172.16.2.0/24", "vswitch-2"), True)

    print "create inter-connect switch..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("fabric:1", "lsw-inter-con", 3), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_logic_switch_data("fabric:2", "lsw-inter-con", 3), True)

    print "create inter-connect port..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data_vlan("fabric:1", "lsw-inter-con", "inter-con-p-1", 300), True)
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data_vlan("fabric:2", "lsw-inter-con", "inter-con-p-1", 300), True)

    print "binding inter-connect port..."
    pause()
    post(controller, DEFAULT_PORT, rpc_port_binding_dev_uri(), rpc_port_binding_dev_data("fabric:1", "lsw-inter-con", "inter-con-p-1", "s1", "s1-eth1"), True)
    post(controller, DEFAULT_PORT, rpc_port_binding_dev_uri(), rpc_port_binding_dev_data("fabric:2", "lsw-inter-con", "inter-con-p-1", "s2", "s2-eth1"), True)

    print "create inter-connect gateway..."
    pause()
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("fabric:1", "10.0.0.1", "10.0.0.0/24", "lsw-inter-con"), True)
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("fabric:2", "10.0.0.2", "10.0.0.0/24", "lsw-inter-con"), True)

    print "create layer3 static route..."
    pause()
    post(controller, DEFAULT_PORT, rpc_add_route_uri(), rpc_add_route("fabric:1", "172.16.1.14/32", "10.0.0.2", "10.0.0.1"), True)
    post(controller, DEFAULT_PORT, rpc_add_route_uri(), rpc_add_route("fabric:1", "172.16.2.23/32", "10.0.0.2", "10.0.0.1"), True)
    post(controller, DEFAULT_PORT, rpc_add_route_uri(), rpc_add_route("fabric:2", "172.16.1.11/32", "10.0.0.1", "10.0.0.2"), True)
    post(controller, DEFAULT_PORT, rpc_add_route_uri(), rpc_add_route("fabric:2", "172.16.1.12/32", "10.0.0.1", "10.0.0.2"), True)
    post(controller, DEFAULT_PORT, rpc_add_route_uri(), rpc_add_route("fabric:2", "172.16.2.13/32", "10.0.0.1", "10.0.0.2"), True)
    post(controller, DEFAULT_PORT, rpc_add_route_uri(), rpc_add_route("fabric:2", "172.16.2.22/32", "10.0.0.1", "10.0.0.2"), True)

    print "create layer3 inter-connect endpoint..."
    pause()
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_reg_inter_conn_endpoint_data("fabric:1", UUID_EPX_1, "10.0.0.2"), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data("fabric:1", UUID_EPX_1,  "s1", "s1-eth1"), True)
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_reg_inter_conn_endpoint_data("fabric:2", UUID_EPX_2, "10.0.0.1"), True)
    post(controller, DEFAULT_PORT, rpc_locate_endpoint_uri(), rpc_locate_endpoint_data("fabric:2", UUID_EPX_2,  "s2", "s2-eth1"), True)

    #----------------------------------- NAT -------------------------------------
    print "enable NAT Function..."
    pause()
    # create external logical switch
    post(controller, DEFAULT_PORT, rpc_create_logic_switch_uri(), rpc_create_external_switch_data("fabric:2", "lsw-external"), True)
    # create logical port
    post(controller, DEFAULT_PORT, rpc_create_logic_port_uri(), rpc_create_logic_port_data("fabric:2", "lsw-external", "lsw-external-p-1"), True)
    # register external gateway endpoint
    post(controller, DEFAULT_PORT, rpc_register_endpoint_uri(), rpc_reg_external_gw_ep_data("fabric:2", UUID_EXT_GW, "192.168.1.1", "s2", "s2-eth2"), True)
    # create outgoing logical port
    post(controller, DEFAULT_PORT, rpc_create_gateway_uri(), rpc_create_gateway_data("fabric:2", "192.168.1.0", "192.168.1.0/24", "lsw-external"), True)
    # add default routing
    post(controller, DEFAULT_PORT, rpc_add_route_uri(), rpc_add_route("fabric:2", "192.168.2.0/24", "192.168.1.1", "192.168.1.0"), True)
    # add NAT function
    post(controller, DEFAULT_PORT, rpc_add_function_uri(), rpc_add_function_data("fabric:2", "172.16.1.14", "192.168.1.2"), True)
