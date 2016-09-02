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

ENDPOINT_OPER_URI='/restconf/operational/fabric-endpoint:endpoints'

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

def delete(host, port, uri, debug=False):
    '''Perform a DELETE rest operation, using the URL and data provided'''

    url='http://'+host+":"+port+uri

    headers = {'Content-type': 'application/yang.data+json',
               'Accept': 'application/yang.data+json'}
    if debug == True:
        print "DELETE %s" % url
    r = requests.delete(url, headers=headers, auth=HTTPBasicAuth(USERNAME, PASSWORD))
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
NODE_ID_OVSDB = ''

def get_tenant_uri(tenantId):
    return "/restconf/config/policy:tenants/policy:tenant/%s" % tenantId

def get_endpoint_uri():
    return "/restconf/operations/endpoint:unregister-endpoint"

def rpc_decompose_fabric_uri():
    return "/restconf/operations/fabric:decompose-fabric"

def rpc_decompose_fabric_data1():
    return {
      "input" : {
           "fabric-id": "fabric:1"
       }
    }
def rpc_decompose_fabric_data2():
    return {
      "input" : {
           "fabric-id": "fabric:2"
       }
    }

def rpc_unregister_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:unregister-endpoint"

def rpc_unregister_endpoint_data(eplist):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "ids":eplist
       }
    }


def pause():
    print "press Enter key to continue..."
    raw_input()

if __name__ == "__main__":
    # Launch main menu

    controller=os.environ.get('ODL')
    if controller == None:
        sys.exit("No controller set.")

    resp=get(controller,DEFAULT_PORT,'/restconf/operational/endpoint:endpoints')
    l2_eps=resp['endpoints']['endpoint']
    l3_eps=resp['endpoints']['endpoint-l3']

    tenants = list()
    resp=get(controller,DEFAULT_PORT,'/restconf/config/policy:tenants')
    if resp.has_key('tenants'):
        if resp['tenants'].has_key('tenant'):
            tenants=resp['tenants']['tenant']

    print "deleting tenant"
    for tenant in tenants:
        delete(controller, DEFAULT_PORT, get_tenant_uri(tenant['id']), True)

    print "unregistering L2 endpoints"
    for endpoint in l2_eps:
        data={ "input": { "l2": [ { "l2-context": endpoint['l2-context'] ,"mac-address": endpoint['mac-address'] } ] } }
        post(controller, DEFAULT_PORT, get_endpoint_uri(),data,True)

    print "unregistering L3 endpoints"
    for endpoint in l3_eps:
        data={ "input": { "l3": [ { "l3-context": endpoint['l3-context'] ,"ip-address": endpoint['ip-address'] } ] } }
        post(controller, DEFAULT_PORT, get_endpoint_uri(),data,True)

    pause()

    print "unregister fabric endpoint"
    eplist = list()
    endpointResp = get(controller, DEFAULT_PORT, ENDPOINT_OPER_URI)
    if endpointResp.has_key("endpoints"):
        endpoints = endpointResp["endpoints"]
        if endpoints.has_key("endpoint"):
            for endpoint in endpoints["endpoint"]:
                eplist.append(endpoint["endpoint-uuid"]) 

    post(controller, DEFAULT_PORT, rpc_unregister_endpoint_uri(), rpc_unregister_endpoint_data(eplist))
    time.sleep(1)

    print "decompose fabric"
    post(controller, DEFAULT_PORT, rpc_decompose_fabric_uri(), rpc_decompose_fabric_data1(), True)
    post(controller, DEFAULT_PORT, rpc_decompose_fabric_uri(), rpc_decompose_fabric_data2(), True)

    print "delete acl"
    delete(controller, DEFAULT_PORT, "/restconf/config/ietf-access-control-list:access-lists/", True)

    print "delete flow table"
    delete(controller, DEFAULT_PORT, "/restconf/config/opendaylight-inventory:nodes/", True)

    print "delete fabric number"
    delete(controller, DEFAULT_PORT, "/restconf/config/fabric-impl:fabrics-setting/", True)
