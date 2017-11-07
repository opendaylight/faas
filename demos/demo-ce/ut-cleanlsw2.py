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

    print "remove logic port"
    pause()
    post(controller, DEFAULT_PORT, rpc_remove_logic_port_uri(), rpc_remove_logic_port_data("vswitch-2", "vswitch-2-p-1"), True)
    post(controller, DEFAULT_PORT, rpc_remove_logic_port_uri(), rpc_remove_logic_port_data("vswitch-2", "vswitch-2-p-2"), True)

    print "remove logic switch"
    pause()
    post(controller, DEFAULT_PORT, rpc_remove_logic_switch_uri(), rpc_remove_logic_switch_data("vswitch-2"), True)

