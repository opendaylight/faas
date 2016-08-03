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

ACL_CONFIG='/restconf/config/ietf-access-controll-list:access-lists/'

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

def get_acl_uri():
    return "/restconf/config/ietf-access-control-list:access-lists/"

def get_acl_data(nsi, nsp):
    return {
    "ietf-access-control-list:access-lists": {
        "acl": [
            {
            "acl-name":"acl-sfc-redirect-1",
            "acl-type":"ipv4-acl",
            "access-list-entries":{
                "ace" :[
                    { 
                    "rule-name":"rule-sfc-redirect-1",
                    "matches":{
                        "destination-ipv4-network":"10.0.36.0/24",
                        "source-ipv4-network":"10.0.35.0/24",
                        "protocol":6,
                        "source-port-range":{
                            "lower-port":0
                            },
                        "destination-port-range":{
                            "lower-port":80
                            },
                     },
                     "actions": {
                                        "dest-ip":"192.168.50.71",
                                        "dest-port":6633,
                                        "nsi":nsi,
                                        "nsp":nsp
                     }
                    }
                ]}
            }
            ]
        }
    }

def rpc_add_acl_uri():
    return "/restconf/operations/fabric-service:add-acl"

def rpc_add_switch_acl_data(switchName, aclName):
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "logical-device":switchName,
           "acl-name":aclName
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

    print "put SFC redirect ACL, param 1 is nsi value, param 2 is nsp value"
    if (len(sys.argv) == 3):
        put(controller, DEFAULT_PORT, get_acl_uri(), get_acl_data(sys.argv[1], sys.argv[2]), True)
    else:
        print "Need nsi and nsp values!!"
        sys.exit(0)

    pause()
    print "put acl-sfc-redirect-1 ACL to Logic Switch"
    post(controller, DEFAULT_PORT, rpc_add_acl_uri(), rpc_add_switch_acl_data("vswitch-1", "acl-sfc-redirect-1"), True)




