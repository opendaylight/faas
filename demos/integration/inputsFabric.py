import inputsCommon

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
fa002Url_gc = "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operations/fabric-service:create-logic-switch"

def rpc_create_logic_switch_data(name, vni, fabricId):
    return {
        "input" : {
           "fabric-id":fabricId,
           "name":name,
           "vni":vni
         }
    }

#===============================================================================#
fa003Url_gc = "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operations/fabric-service:create-logic-router"

def rpc_create_logic_router_data(name, fabricId):
    return {
        "input" : {
           "fabric-id":fabricId,
           "name":name
        }
    }


