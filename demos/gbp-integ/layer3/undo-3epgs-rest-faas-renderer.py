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

def get(host, port, uri):
    url='http://'+host+":"+port+uri
    #print url
    r = requests.get(url, auth=HTTPBasicAuth(USERNAME, PASSWORD))
    jsondata=json.loads(r.text)
    return jsondata

def delete(host, port, uri):
    url='http://'+host+":"+port+uri
    #print url
    r = requests.delete(url, auth=HTTPBasicAuth(USERNAME, PASSWORD))
    r.raise_for_status()

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
def get_tenant_uri():
    return "/restconf/config/policy:tenants/policy:tenant/tenant-dobre"

def get_endpoint_data():
    return {
    "input": {
      
       "l2": [
          {
             "l2-context": "bridge-domain3", 
             "mac-address": "00:00:00:00:34:02"
          },
          {
             "l2-context": "bridge-domain2", 
             "mac-address": "00:00:00:00:35:02"
          },
          {
            "l2-context": "bridge-domain1", 
            "mac-address": "00:00:00:00:36:04",
          }
        ],      

        "l3": [
            {
                "ip-address": "10.0.34.2", 
                "l3-context": "l3-context-vrf-red"
            },
            {
                "ip-address": "10.0.35.2", 
                "l3-context": "l3-context-vrf-red"
            },
            {
                "ip-address": "10.0.36.2", 
                "l3-context": "l3-context-vrf-red"
            }
        ]
    }
}

def get_endpoint_uri():
    return "/restconf/operations/endpoint:unregister-endpoint"

if __name__ == "__main__":
    # Launch main menu


    # Some sensible defaults
    controller=os.environ.get('ODL')
    if controller == None:
        sys.exit("No controller set.")
    
    print "unregistering endpoints"
    post(controller, DEFAULT_PORT, get_endpoint_uri(),get_endpoint_data(),False)
    
    print "delete tenant"
    delete(controller,DEFAULT_PORT,get_tenant_uri())
        
    
    
