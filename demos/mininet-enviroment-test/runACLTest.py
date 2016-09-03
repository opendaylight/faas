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

def get_acl_data():
    return {
    "ietf-access-control-list:access-lists": {
        "acl": [
            {
            "acl-name":"acl-icmp-deny",
            "acl-type":"ipv4-acl",
            "access-list-entries":{
                "ace" :[
                    {
                    "rule-name":"rule-icmp-deny",
                    "matches":{
                        "destination-ipv4-network":"172.16.1.0/24",
                        "source-ipv4-network":"172.16.2.0/24",
                        "source-port-range":{
                            "lower-port":1,
                            },
                        "destination-port-range":{
                            "lower-port":1
                            },
                        "protocol":1
                        },
                        "actions": {
                            "deny" : "true"
                        }
                    }
                ]}
            },
            {
            "acl-name":"acl-udp-permit",
            "acl-type":"ipv4-acl",
            "access-list-entries":{
                "ace" :[
                    {
                    "rule-name":"rule-udp-permit",
                    "matches":{
                        "destination-ipv4-network":"172.16.1.0/24",
                        "source-ipv4-network":"172.16.2.0/24",
                        "protocol":17,
                        "source-port-range":{
                            "lower-port":22
                            },
                        "destination-port-range":{
                            "lower-port":11
                            },
                     },
                     "actions": {
                            "permit" : "true"
                     }
                    }
                ]}
            },
            {
            "acl-name":"acl-eth-deny",
            "acl-type":"ipv4-acl",
            "access-list-entries":{
                "ace" :[
                    {
                    "rule-name":"rule-eth-deny",
                    "matches":{
                        "destination-mac-address":"62:02:1a:00:b7:12",
                        },
                        "actions": {
                            "deny" : "true"
                        }
                    }
                ]}
            },
            {
            "acl-name":"acl-tcp-deny",
            "acl-type":"ipv4-acl",
            "access-list-entries":{
                "ace" :[
                    {
                    "rule-name":"rule-tcp-deny",
                    "matches":{
                        "destination-ipv4-network":"172.16.1.0/24",
                        "source-ipv4-network":"172.16.2.0/24",
                        "protocol":6,
                        "source-port-range":{
                            "lower-port":22
                            },
                        "destination-port-range":{
                            "lower-port":11
                            },
                     },
                     "actions": {
                            "deny" : "true"
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

def rpc_add_port_acl_data(switchName, portName, aclName):
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "logical-device":switchName,
           "logical-port":portName,
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

    print "put ACL"
    put(controller, DEFAULT_PORT, get_acl_uri(), get_acl_data(), True)

    print "add acl-icmp-deny ACL to Logic Switch"
    pause()
    post(controller, DEFAULT_PORT, rpc_add_acl_uri(), rpc_add_switch_acl_data("vswitch-1", "acl-icmp-deny"), True)

    print "add acl-tcp-deny to Logic Router"
    pause()
    post(controller, DEFAULT_PORT, rpc_add_acl_uri(), rpc_add_switch_acl_data("vrouter-1", "acl-tcp-deny"), True)
    
    pause()
    print "add acl-udp-permit ACL to Logic Port"
    post(controller, DEFAULT_PORT, rpc_add_acl_uri(), rpc_add_port_acl_data("vswitch-1", "vswitch-1-p-1", "acl-udp-permit"), True)

    #pause()
    #print "put acl-eth-deny ACL to Logic Switch"
    #post(controller, DEFAULT_PORT, rpc_add_acl_uri(), rpc_add_switch_acl_data("vswitch-1", "acl-eth-deny"), True)

    #pause()
    #print "put acl-tcp-deny ACL to Logic Switch"
    #post(controller, DEFAULT_PORT, rpc_add_acl_uri(), rpc_add_switch_acl_data("vswitch-1", "acl-tcp-deny"), True)
