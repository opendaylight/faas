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

def get_jsondata(host, port, uri):
    url = 'http://' + host + ":" + port + uri
    #print url
    r = requests.get(url, auth=HTTPBasicAuth(USERNAME, PASSWORD))
    jsondata=json.loads(r.text)
    return jsondata

def get(host, port, uri):
    url='http://'+host+":"+port+uri
    r = requests.get(url, auth=HTTPBasicAuth(USERNAME, PASSWORD))
    return r

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


# =======================
#      MAIN PROGRAM
# =======================

def rpc_remove_logic_router_uri():
    return "/restconf/operations/fabric-service:rm-logical-router"

def rpc_remove_logic_router_data(lrname):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "node-id":lrname
        }
    }

def rpc_remove_gateway_uri():
    return "/restconf/operations/fabric-service:rm-gateway"

def rpc_remove_gateway_data(ipaddr):
    return {
      "input" : {
           "fabric-id": "fabric:1",
           "ip-address":ipaddr,
           "logical-router":"vrouter-1",
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


def rpc_remove_logic_switch_uri():
    return "/restconf/operations/fabric-service:rm-logical-switch"

def rpc_remove_logic_switch_data(lswname):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "node-id":lswname,
         }
    }

def rpc_remove_logic_port_uri():
    return "/restconf/operations/fabric-service:rm-logical-port"

def rpc_remove_logic_port_data(ldName, portName):
    return {
        "input" : {
           "fabric-id": "fabric:1",
           "tp-id":portName,
           "logical-device":ldName
       }
    }

def pause():
    print "press Enter key to continue..."
    raw_input()

if __name__ == "__main__":
    # Launch main menu

    # Some sensible defaults
    controller=os.environ.get('ODL')
    if controller == None:
        sys.exit("No controller set.")

    print "unregister endpoint"
    eplist = list()
    endpointResp = get(controller, DEFAULT_PORT, ENDPOINT_OPER_URI)
    if endpointResp.has_key("endpoints"):
        endpoints = endpointResp["endpoints"]
        if endpoints.has_key("endpoint"):
            for endpoint in endpoints["endpoint"]:
                eplist.append(endpoint["endpoint-uuid"]) 

    post(controller, DEFAULT_PORT, rpc_unregister_endpoint_uri(), rpc_unregister_endpoint_data(eplist))
    print "create gateway"
    pause()
    post(controller, DEFAULT_PORT, rpc_remove_gateway_uri(), rpc_remove_gateway_data("192.168.6.1"), True)
    post(controller, DEFAULT_PORT, rpc_remove_gateway_uri(), rpc_remove_gateway_data("192.168.5.1"), True)

    print "remove_logic_router"
    pause()
    post(controller, DEFAULT_PORT, rpc_remove_logic_router_uri(), rpc_remove_logic_router_data("vrouter-1"), True)

    print "remove logic port of lsw 1"
    pause()
    post(controller, DEFAULT_PORT, rpc_remove_logic_port_uri(), rpc_remove_logic_port_data("vswitch-1", "vswitch-1-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_remove_logic_port_uri(), rpc_remove_logic_port_data("vswitch-1", "vswitch-1-p-2"), True)

    print "remove logic switch 1"
    pause()
    post(controller, DEFAULT_PORT, rpc_remove_logic_switch_uri(), rpc_remove_logic_switch_data("vswitch-1"), True)

    print "remove logic port of lsw 2"
    pause()
    post(controller, DEFAULT_PORT, rpc_remove_logic_port_uri(), rpc_remove_logic_port_data("vswitch-2", "vswitch-2-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_remove_logic_port_uri(), rpc_remove_logic_port_data("vswitch-2", "vswitch-2-p-2"), True)

    print "remove logic switch 2"
    pause()
    post(controller, DEFAULT_PORT, rpc_remove_logic_switch_uri(), rpc_remove_logic_switch_data("vswitch-2"), True)

