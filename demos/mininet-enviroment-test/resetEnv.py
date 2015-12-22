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
UUID_EP1 = '75a4451e-eed0-4645-9194-64454bda2902'
UUID_EP2 = 'ad08c19c-32cc-4cee-b902-3f4919f51bbc'
UUID_EP3 = 'cae54555-7957-4d26-8515-7f2d1de5da55'
UUID_EP4 = '4bc83eb7-4147-435c-8e0f-a546288fd639'
NODE_ID_OVSDB = ''

def rpc_decompose_fabric_uri():
    return "/restconf/operations/fabric:decompose-fabric"

def rpc_decompose_fabric_data():
    return {
      "input" : {
           "fabric-id": "fabric:1"
       }
    }

def rpc_unregister_endpoint_uri():
    return "/restconf/operations/fabric-endpoint:unregister-endpoint"

def rpc_unregister_endpoint_data():
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "ids":[UUID_EP1,UUID_EP2,UUID_EP3,UUID_EP4]
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

    print "unRegister Endpoint"
    post(controller, DEFAULT_PORT, rpc_unregister_endpoint_uri(), rpc_unregister_endpoint_data(), True)

    print "decompose fabric"
    post(controller, DEFAULT_PORT, rpc_decompose_fabric_uri(), rpc_decompose_fabric_data(), True)

    pause()
    print "delete acl"
    delete(controller, DEFAULT_PORT, "/restconf/config/ietf-access-control-list:access-lists/", True)

    print "delete flow table"
    delete(controller, DEFAULT_PORT, "/restconf/config/opendaylight-inventory:nodes/", True)

    print "delete fabric number"
    delete(controller, DEFAULT_PORT, "/restconf/config/fabric-impl:fabrics-setting/", True)
