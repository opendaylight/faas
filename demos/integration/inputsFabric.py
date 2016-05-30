import inputsCommon

#===============================================================================#
def get_unregister_endpoint_uri():
    return "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operations/fabric-endpoint:unregister-endpoint"

#===============================================================================#
def get_register_endpoint_uri():
    return "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operations/fabric-endpoint:register-endpoint"

#===============================================================================#
def get_epList_uri():
    return "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operational/fabric-endpoint:endpoints"

#===============================================================================#
def get_unregister_endpoint_data(fabricId, eplist):
    return {
        "input" : {
           "fabric-id": fabricId,
           "ids":eplist
       }
    }

#===============================================================================#
def get_register_endpoint_data(fabricId, mac, ip, gw, lsw, lport, nodeRef, tpRef):
    return {
        "input" : {
            "fabric-id":fabricId,
            "mac-address":mac,
            "ip-address":ip,
            "gateway":gw,
            "logic-location" : {
                "node-id": lsw,
                "tp-id": lport
            },
            "location" : {
                "node-ref": nodeRef,
                "tp-ref": tpRef,
                "access-type":"exclusive"
            }
        }
    }

#===============================================================================#
fa001Url_gc = "http://"+inputsCommon.odlIpAddr_gc+'/restconf/operations/fabric:compose-fabric'

def rpc_compose_fabric_data():
    return {
      "input" : {
           "name": "first fabric",
           "type":"VXLAN",
           "device-nodes" : [
             {
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='/bridge/s1']","vtep-ip":"192.168.20.101"
              },{
                "device-ref":"/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='/bridge/s2']","vtep-ip":"192.168.20.102"
             }
           ]
       }
    }

#===============================================================================#  
fa002Url_gc = "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operations/fabric-service:create-logical-switch"

def rpc_create_logic_switch_data(name, vni, fabricId):
    return {
        "input" : {
           "fabric-id":fabricId,
           "name":name,
           "vni":vni
         }
    }

#===============================================================================#
fa003Url_gc = "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operations/fabric-service:create-logical-router"

def rpc_create_logic_router_data(name, fabricId):
    return {
        "input" : {
           "fabric-id":fabricId,
           "name":name
        }
    }


