import util
import urllib
import constants
#from enum import Enum
import json
import sys
import getopt
import inputsCommon
import inputsVcontainer
import inputsFabric
import inputsGBP
import inputsSFC
import tenant_2EPG_SFC
import tenant_3EPG_SFC
import tenant_3EPG_demo
import tenant_3EPG_multifabric

#
# Manifested constants
#
#class RestMethods_e(Enum):
get_c = 1
put_c = 2
post_c = 3
post_data_array_c = 4
delete_c = 5

#
# Global variables
#
testCases_ga = {}


#
# Function Implimentations
#

#===============================================================================# 
def printTestCase(desc):
  key = 0
  key2 = 0
  for key in sorted(testCases_ga.keys()):
    print key, ": ", testCases_ga[key][1]

  for key2 in sorted(testCases2_ga.keys()):
    print key2, ": ", testCases2_ga[key2][3]

  return constants.OK_STR

#===============================================================================#
def getTopology(desc):

	# Set the request URL
  #url = 'http://127.0.0.1:8181/restconf/config/network-topology:network-topology'
  url = 'http://127.0.0.1:8181/restconf/operational/network-topology:network-topology'

  # Send the GET request
  resp = util.requestGET(url)

  if resp == constants.ERROR_STR:
    print "ERROR: getTopology() failed"
    return resp

  return resp

#===============================================================================# 
def registerEndpointLocation_acl(desc):
  nc_id1 = "sw1-eth1"
  nc_id2 = "sw2-eth3"
  nodeId1 = util.getOvsdbNodeIdByName("sw1")

  if nodeId1 == constants.ERROR_STR: 
    return constants.ERROR_STR

  nodeId2 = util.getOvsdbNodeIdByName("sw2")
  if nodeId1 == constants.ERROR_STR: 
    return constants.ERROR_STR

  for inputData in inputsGBP.get_endpoint_location_data(nc_id1, nc_id2, nodeId1, nodeId2):
    result = util.runRequestPOST(inputsGBP.get_endpoint_location_uri(), json.dumps(inputData), sys._getframe().f_code.co_name) 

  return result

#===============================================================================# 
def registerEndpointLocation_sfc(desc):
  nc_id1 = "vethl-h35-2"
  nc_id2 = "vethl-h36-4"
  nodeId1 = util.getOvsdbNodeIdByName("sw1")

  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  nodeId2 = util.getOvsdbNodeIdByName("sw6")
  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  for inputData in tenant_2EPG_SFC.get_endpoint_location_data(nc_id1, nc_id2, nodeId1, nodeId2):
    result = util.runRequestPOST(inputsGBP.get_endpoint_location_uri(), json.dumps(inputData), sys._getframe().f_code.co_name)

  return result

#===============================================================================# 
def registerEndpointLocation_3epg_sfc(desc):
  nc_id1 = "vethl-h35-2"
  nc_id2 = "vethl-h36-4"
  nc_id3 = "vethl-h37-2"

  nodeId1 = util.getOvsdbNodeIdByName("sw1")
  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  nodeId2 = util.getOvsdbNodeIdByName("sw6")
  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  nodeId3 = util.getOvsdbNodeIdByName("sw7")
  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  for inputData in tenant_3EPG_SFC.get_endpoint_location_data(nc_id1, nc_id2, nc_id3, nodeId1, nodeId2, nodeId3):
    result = util.runRequestPOST(inputsGBP.get_endpoint_location_uri(), json.dumps(inputData), sys._getframe().f_code.co_name)

  return result

#===============================================================================# 
def registerEndpointLocation_3epg_sfc_acl(desc):
  nc_id1a = "vethl-h35-2"
  nc_id1b = "vethl-h35-3"
  nc_id2a = "vethl-h36-4"
  nc_id2b = "vethl-h36-5"
  nc_id3a = "vethl-h37-2"
  nc_id3b = "vethl-h37-3"

  nodeId1 = util.getOvsdbNodeIdByName("sw1")
  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  nodeId2 = util.getOvsdbNodeIdByName("sw6")
  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  nodeId3 = util.getOvsdbNodeIdByName("sw7")
  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  for inputData in tenant_3EPG_demo.get_endpoint_location_data(nc_id1a, nc_id1b, nc_id2a, nc_id2b, nc_id3a, nc_id3b, nodeId1, nodeId2, nodeId3):
    result = util.runRequestPOST(inputsGBP.get_endpoint_location_uri(), json.dumps(inputData), sys._getframe().f_code.co_name)

  return result

#===============================================================================# 
def registerEndpointLocation_multifabric(desc):
  nc_id1a = "vethl-h35-2"
  nc_id1b = "vethl-h35-3"
  nc_id2a = "vethl-h36-4"
  nc_id2b = "vethl-h36-5"
  nc_id3a = "vethl-h37-2"
  nc_id3b = "vethl-h37-3"
  nc_id4a = "vethl-h35-8"
  nc_id4b = "vethl-h36-8"
  nc_id4c = "vethl-h37-8"

  nodeId1 = util.getOvsdbNodeIdByName("sw11")
  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  nodeId2 = util.getOvsdbNodeIdByName("sw12")
  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  nodeId3 = util.getOvsdbNodeIdByName("sw13")
  if nodeId1 == constants.ERROR_STR:
    return constants.ERROR_STR

  nodeId4 = util.getOvsdbNodeIdByName("sw21")
  if nodeId4 == constants.ERROR_STR:
    return constants.ERROR_STR

  for inputData in tenant_3EPG_multifabric.get_endpoint_location_data(nc_id1a, nc_id1b, nc_id2a, nc_id2b, nc_id3a, nc_id3b, nc_id4a, nc_id4b, nc_id4c,nodeId1, nodeId2, nodeId3, nodeId4):
    result = util.runRequestPOST(inputsGBP.get_endpoint_location_uri(), json.dumps(inputData), sys._getframe().f_code.co_name)

  return result
#===============================================================================#  
BRIDGE_REF_P="/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='%s']"
TP_REF_P="/network-topology:network-topology/network-topology:topology[network-topology:topology-id='ovsdb:1']/network-topology:node[network-topology:node-id='%s']/network-topology:termination-point[network-topology:tp-id='%s']"
def epMigrationDemo(desc):
   # unregister h35-3
   fabricId = "fabric:1" 
   mac = "00:00:00:00:35:03" 
   ip = "10.0.35.3"
   resp = util.runRequestGET(inputsFabric.get_epList_uri(), sys._getframe().f_code.co_name)
   if resp == constants.ERROR_STR:
      print "ERROR: getOvsdbNodeIdByName() failed"
      return constants.ERROR_STR
  
   eplist = list()
   lswId = ''
   lport = ''
   endpointResp=json.loads(resp)
   if endpointResp.has_key("endpoints"):
      endpoints = endpointResp["endpoints"]
      if endpoints.has_key("endpoint"):
         for endpoint in endpoints["endpoint"]:
            if endpoint["ip-address"] == ip:
               eplist.append(endpoint["endpoint-uuid"])
               logicLocation = endpoint["logic-location"]
               lswId = logicLocation["node-id"]
               lport = logicLocation["tp-id"]
               break

   result = util.runRequestPOST(inputsFabric.get_unregister_endpoint_uri(), json.dumps(inputsFabric.get_unregister_endpoint_data("fabric:1", eplist)), sys._getframe().f_code.co_name)

   # register h35-4
   fabricId = "fabric:1"
   mac = "00:00:00:00:35:04"
   ip = "10.0.35.4"
   gw = "10.0.35.1"
   port = "vethl-h35-4"
   nodeid = util.getOvsdbNodeIdByName("sw6")
   nodeRef = BRIDGE_REF_P % (nodeid)
   tpRef = TP_REF_P % (nodeid, port)

   if nodeid == constants.ERROR_STR:
      return constants.ERROR_STR

   result = util.runRequestPOST(inputsFabric.get_register_endpoint_uri(), json.dumps(inputsFabric.get_register_endpoint_data(fabricId, mac, ip, gw, lswId, lport, nodeRef, tpRef)), sys._getframe().f_code.co_name) 

   return desc

#===============================================================================# 
testCases_ga = {'0': (printTestCase, 'Print test case table'),
   'p1': (getTopology, 'Print Topology'),
   'vc05': (registerEndpointLocation_acl, 'Register endpoint locations for Layer 3 ULN with ACL'),
   'vc052': (registerEndpointLocation_sfc, 'Register endpoint locations for Layer 3 ULN with SFC'),
   'vc053': (registerEndpointLocation_3epg_sfc, 'Register endpoint locations for 3EPG-Layer3-ULN with SFC'),
   'vc054': (registerEndpointLocation_3epg_sfc_acl, 'Register 6 endpoints locations for 3EPG-Layer3-ULN with SFC'),
   'vc055': (registerEndpointLocation_multifabric, 'Register 9 endpoints locations for 3EPG-Layer3-ULN with SFC on multi-fabric'),
   'vc064': (epMigrationDemo, 'EP Migration from h35-3 on sw1 to h35-4 on sw6'),
}

testCases2_ga = {
  'vc01': (post_c, 
            inputsFabric.fa001Url_gc, 
            inputsFabric.rpc_compose_fabric_data(), 
            'Compose Fabric'),
  'vc011': (post_c, 
            inputsFabric.fa002Url_gc, 
            inputsFabric.rpc_create_logic_switch_data("lsw001", 100, "fabric:1"), 
            'Create LSW on Fabric'),
  'vc012': (post_c, 
            inputsFabric.fa003Url_gc, 
            inputsFabric.rpc_create_logic_router_data("lr001", "fabric:1"), 
            'Create LR on Fabric'),
  'vc02': (post_c, 
            inputsVcontainer.vc001Url_gc, 
            inputsVcontainer.rpc_create_vcontainer_data(inputsCommon.tenant1Id_gc, "fabric:1"), 
            'create vcontainer with one Fabric'),
  'vc021': (post_c, 
            inputsVcontainer.vc002Url_gc, 
            inputsVcontainer.rpc_netnode_create_lsw_data(inputsCommon.tenant1Id_gc, "fabric:1"), 
            'Create NetNode LSW'),
  'vc022': (post_c, 
            inputsVcontainer.vc003Url_gc, 
            inputsVcontainer.rpc_netnode_create_lr_data(inputsCommon.tenant1Id_gc, "fabric:1"), 
            'Create NetNode LR'),
  'vc023': (post_c, 
            inputsVcontainer.vc001Url_gc, 
            inputsVcontainer.rpc_create_vcontainer_multifabric(inputsCommon.tenant1Id_gc, ("fabric:1","fabric:2")), 
            'create vcontainer with two Fabric'),
  'vc03': (put_c, 
           inputsGBP.get_tenant_uri(inputsCommon.tenant1Id_gc), 
           inputsGBP.get_tenant_data_layer3_acl(inputsCommon.tenant1Id_gc),
           'Create tenant policy for a Layer 3 ULN with ACL'),
  'vc031': (put_c, 
            inputsGBP.get_tenant_uri(inputsCommon.tenant1Id_gc), 
            inputsGBP.get_tenant_data_layer2_acl(inputsCommon.tenant1Id_gc),
            'Create tenant of Layer 2 ULN with ACL'),
  'vc032': (put_c, 
            inputsGBP.get_tenant_uri(inputsCommon.tenant1Id_gc), 
            tenant_2EPG_SFC.get_tenant_data(inputsCommon.tenant1Id_gc),
            'Create tenant of Layer 3 ULN with SFC'),
  'vc033': (put_c, 
            inputsGBP.get_tenant_uri(inputsCommon.tenant1Id_gc), 
            tenant_3EPG_SFC.get_tenant_data(inputsCommon.tenant1Id_gc),
            'Create 3-EPG tenant of Layer 3 ULN with SFC'),
  'vc034': (put_c, 
            inputsGBP.get_tenant_uri(inputsCommon.tenant1Id_gc), 
            tenant_3EPG_demo.get_tenant_data(inputsCommon.tenant1Id_gc),
            'Create 3-EPG tenant of Layer 3 ULN with SFC and ACL and 6 endpoints'),
  'vc035': (put_c, 
            inputsGBP.get_tenant_uri(inputsCommon.tenant1Id_gc), 
            tenant_3EPG_multifabric.get_tenant_data(inputsCommon.tenant1Id_gc),
            'Create 3-EPG tenant of Layer 3 ULN with SFC and ACL and 6 endpoints'),
  'vc04': (post_data_array_c, 
           inputsGBP.get_endpoint_uri(), 
           inputsGBP.get_endpoint_data_layer3(inputsCommon.tenant1Id_gc),
           'Register endpoints for Layer 3 ULN with ACL'),
  'vc041': (post_c, 
            inputsGBP.get_endpoint_uri(), 
            inputsGBP.get_endpoint_data(inputsCommon.tenant1Id_gc),
            'Register endpoints on Layer 2 ULN with ACL'),
  'vc042': (post_data_array_c, 
           inputsGBP.get_endpoint_uri(), 
           tenant_2EPG_SFC.get_endpoint_data(inputsCommon.tenant1Id_gc),
           'Register endpoints for Layer 3 ULN with SFC'),
  'vc043': (post_data_array_c, 
           inputsGBP.get_endpoint_uri(), 
           tenant_3EPG_SFC.get_endpoint_data(inputsCommon.tenant1Id_gc),
           'Register endpoints for 3EPG-Layer3-ULN with SFC'),
  'vc044': (post_data_array_c, 
           inputsGBP.get_endpoint_uri(), 
           tenant_3EPG_demo.get_endpoint_data(inputsCommon.tenant1Id_gc),
           'Register 6 endpoints for 3EPG-Layer3-ULN with SFC and ACL'),
  'vc045': (post_data_array_c, 
           inputsGBP.get_endpoint_uri(), 
           tenant_3EPG_multifabric.get_endpoint_data(inputsCommon.tenant1Id_gc),
           'Register 9 endpoints for 3EPG-Layer3-ULN with SFC and ACL on multi-fabric'),
  'vc051': (post_data_array_c, 
           inputsGBP.get_endpoint_location_uri(),
           inputsGBP.get_endpoint_location_data_old(),
           'Register endpoint locations for Layer 3 ULN with ACL in sanity environment (i.e., no ovs running)'),
  'vc06': (post_c, 
           inputsGBP.get_unreg_endpoint_uri(), 
           inputsGBP.get_unreg_endpoint_data_layer3(),
           'Unregister endpoints on Layer 3 ULN'),
  'vc07': (delete_c, 
           inputsGBP.get_tenant_uri(inputsCommon.tenant1Id_gc), 
           'unused param',
           'Delete tenant of Layer 3 ULN'),
  'p2': (get_c, 
          inputsGBP.getLogicalNetworksUri_gc, 
          'unused param',
          'Get the logical network entities for all tenants on the uln-mapper side'),
  'p3': (get_c, 
          inputsGBP.getUlnUri_gc, 
          'unused param',
          'Get ULN on the GBP Faas renderer side'),
  'p4': (get_c, 
          inputsGBP.getMappedEntities_gc, 
          'unused param',
          'Get mapped IDs on the GBP FAAS renderer side'),
  'p5': (get_c, 
          inputsSFC.get_acl_uri(), 
          'unused param',
          'Get ACL list'),
  'p6': (get_c, 
          inputsSFC.get_invmgr_uri(), 
          'unused param',
          'Get flow rules'),
  'p7': (get_c, 
          inputsSFC.get_sfcList_uri(), 
          'unused param',
          'Get SFC list'),
  'p8': (get_c, 
          inputsSFC.get_sfList_uri(), 
          'unused param',
          'Get SF list'),
  'p9': (get_c, 
          inputsSFC.get_sffList_uri(), 
          'unused param',
          'Get service function forwarder(SFF) list'),
  'p10': (get_c, 
          inputsSFC.get_sfpList_uri(), 
          'unused param',
          'Get service function paths'),
  'p11': (get_c, 
          inputsFabric.get_epList_uri(), 
          'unused param',
          'Get registered endpoint list'),
  'acl01': (put_c, 
           inputsSFC.get_acl_uri(), 
           inputsSFC.get_acl_data(),
           'Create ACL'),
  'acl02': (put_c, 
           inputsSFC.get_acl_uri(), 
           inputsSFC.get_acl_data2(),
           'Create ACL')
}

def main(argv):
  inputfile = ''
  outputfile = ''
  testNum = '0'
  result = ''

  try:
      opts, args = getopt.getopt(argv,"hi:o:t:n:",["ifile=","ofile=","testcase=", "netnode="])
  except getopt.GetoptError:
    print 'test.py -i <inputfile> -o <outputfile>'
    sys.exit(2)
  for opt, arg in opts:
    if opt == '-h':
      print 'test.py -i <inputfile> -o <outputfile> -t <num>'
      sys.exit()
    elif opt in ("-i", "--ifile"):
      inputfile = arg
    elif opt in ("-o", "--ofile"):
      outputfile = arg
    elif opt in ("-t", "--testcase"):
      #testNum = int(arg)
      testNum = arg
    else:
      assert False, "unhandled option"

  #workName = raw_input('Please enter a work name:')
  res = util.setupAuthentication()

  if res != constants.OK_STR:
    sys.exit(1)

  if testNum in testCases2_ga.keys():
    print testCases2_ga[testNum][3]
    if testCases2_ga[testNum][0] == put_c:
      result = util.runRequestPUT(testCases2_ga[testNum][1], json.dumps(testCases2_ga[testNum][2]), sys._getframe().f_code.co_name) 
    elif testCases2_ga[testNum][0] == post_c:
      #print json.dumps(testCases2_ga[testNum][2])
      result = util.runRequestPOST(testCases2_ga[testNum][1], json.dumps(testCases2_ga[testNum][2]), sys._getframe().f_code.co_name) 
    elif testCases2_ga[testNum][0] == post_data_array_c:
      #print json.dumps(testCases2_ga[testNum][2])
      for inputData in (testCases2_ga[testNum][2]):
        result = util.runRequestPOST(testCases2_ga[testNum][1], json.dumps(inputData), sys._getframe().f_code.co_name) 
    elif testCases2_ga[testNum][0] == get_c:
      result = util.runRequestGET(testCases2_ga[testNum][1], sys._getframe().f_code.co_name) 
      print result
    elif testCases2_ga[testNum][0] == delete_c:
      result = util.runRequestDELETE(testCases2_ga[testNum][1], sys._getframe().f_code.co_name) 
      print result
  else:
    result = testCases_ga[testNum][0](testCases_ga[testNum][1]) 
    print result


#===============================================================================#  
# Main code
#
if __name__ == "__main__":
   main(sys.argv[1:])



