import inputsCommon

#===============================================================================#
vc001Url_gc = 'http://'+inputsCommon.odlIpAddr_gc+'/restconf/operations/vcontainer-topology:create-vcontainer'

def rpc_create_vcontainer_data(tenantId, vfabricId):
    return {
      "input" : {
           "tenant-id":tenantId,
           "vcontainer-config": {
               "vfabric": [
                   { "vfabric-id" : vfabricId }
                ]
           }
       }
    }


def rpc_create_vcontainer_data2(tenantId, vfabricId):
    return {
      "input" : {
           "tenant-id":tenantId,
           "vcontainer-config": {
               "vfabric": [
                   { "vfabric-id" : vfabricId },
                   { "vfabric-id" : vfabricId+"1" }
                ]
           }
       }
    }

#===============================================================================#
vc002Url_gc = 'http://'+inputsCommon.odlIpAddr_gc+'/restconf/operations/vc-net-node:create-lne-layer2'

def rpc_netnode_create_lsw_data(tenantId, vfabricId):
    return {
      "input" : {
           "tenant-id":tenantId,
           "vfabric-id" : vfabricId,
           "lsw-uuid" : "c369dcb0-82e1-11e4-b116-123b93f75cba",
           "name": "lsw002",
           "segment-id":100
      }
    }

#===============================================================================#
vc003Url_gc = 'http://'+inputsCommon.odlIpAddr_gc+'/restconf/operations/vc-net-node:create-lne-layer3'

def rpc_netnode_create_lr_data(tenantId, vfabricId):
    return {
      "input" : {
           "tenant-id":tenantId,
           "vfabric-id" : vfabricId,
           "lr-uuid" : "c369dcb0-82e1-11e4-b116-123b93f75cbb", 
           "name": "lr003"
      }
    }


#===============================================================================# 
vc004Url_gc = 'http://'+inputsCommon.odlIpAddr_gc+'/restconf/config/ietf-access-control-list:access-lists/'


