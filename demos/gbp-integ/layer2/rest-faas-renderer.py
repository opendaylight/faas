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
    

def get_tenant_data():
    return {
        "policy:tenant": {
          "id": "tenant-dobre",
          "name": "GBPPOC",
          "forwarding-context": {
            "l2-bridge-domain": [
              {
                "id": "bridge-domain1",
                "parent": "l3-context-vrf-red"
              }
            ],
            "l2-flood-domain": [
              {
                "id": "flood-domain2",
                "parent": "bridge-domain1"
              },
              {
                "id": "flood-domain1",
                "parent": "bridge-domain1"
              }
            ],
            "l3-context": [
              {
                "id": "l3-context-vrf-red"
              }
            ],
            "subnet": [
              {
                "id": "subnet-10.0.35.0/24",
                "ip-prefix": "10.0.35.1/24",
                "parent": "flood-domain2",
                "virtual-router-ip": "10.0.35.1"
              },
              {
                "id": "subnet-10.0.36.0/24",
                "ip-prefix": "10.0.36.1/24",
                "parent": "flood-domain1",
                "virtual-router-ip": "10.0.36.1"
              }
            ]
          },
          "policy": {
            "contract": [
              {
                "clause": [
                  {
                    "name": "allow-http-clause",
                    "subject-refs": [
                      "allow-http-subject",
                      "allow-icmp-subject"
                    ]
                  }
                ],
                "id": "webToAppContract",
                "subject": [
                  {
                    "name": "allow-http-subject",
                    "rule": [
                      {
                        "classifier-ref": [
                          {
                            "direction": "in",
                            "name": "http-dest",
                            "instance-name": "http-dest"
                          },
                          {
                            "direction": "out",
                            "name": "http-src",
                            "instance-name": "http-src"
                          }
                        ],
                        "action-ref": [
                          {
                            "name": "allow1",
                            "order": 0
                          }
                        ],
                        "name": "allow-http-rule"
                      }
                    ]
                  },
                  {
                    "name": "allow-icmp-subject",
                    "rule": [
                      {
                        "classifier-ref": [
                          {
                            "name": "icmp",
                            "instance-name": "icmp"
                          }
                        ],
                        "action-ref": [
                          {
                            "name": "allow1",
                            "order": 0
                          }
                        ],
                        "name": "allow-icmp-rule"
                      }
                    ]
                  }
                ]
              }
            ],
            "endpoint-group": [
              {
                "consumer-named-selector": [
                  {
                    "contract": [
                      "webToAppContract"
                    ],
                    "name": "app-web-webToAppContract"
                  }
                ],
                "id": "web",
                "provider-named-selector": []
              },
              {
                "consumer-named-selector": [],
                "id": "app",
                "provider-named-selector": [
                  {
                    "contract": [
                      "webToAppContract"
                    ],
                    "name": "app-web-webToAppContract"
                  }
                ]
              }
            ],
            "external-implicit-group": [
               {
                 "id": "web"
               }
            ],
            "subject-feature-instances": {
              "classifier-instance": [
                {
                  "classifier-definition-id": "Classifier-L4",
                  "name": "http-dest",
                  "parameter-value": [
                    {
                      "int-value": "6",
                      "name": "proto"
                    },
                    {
                      "int-value": "80",
                      "name": "destport"
                    }
                  ]
                },
                {
                  "classifier-definition-id": "Classifier-L4",
                  "name": "http-src",
                  "parameter-value": [
                    {
                      "int-value": "6",
                      "name": "proto"
                    },
                    {
                      "int-value": "80",
                      "name": "sourceport"
                    }
                  ]
                },
                {
                  "classifier-definition-id": "Classifier-IP-Protocol",
                  "name": "icmp",
                  "parameter-value": [
                    {
                      "int-value": "1",
                      "name": "proto"
                    }
                  ]
                }
              ],
              "action-instance": [
                {
                  "name": "allow1",
                  "action-definition-id": "Action-Allow"
                }
              ]
            }
          }
        }
    }

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
    return [{
    "input": {

        "endpoint-group": "web", 

        "network-containment" : "subnet-10.0.35.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:35:02", 

        "l3-address": [
            {
                "ip-address": "10.0.35.2", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-port-ref-id": "165b3a20-adc7-11e5-bf7f-feff819cdc9f",
        "tenant": "tenant-dobre"
    }
},
{
    "input": {

        "endpoint-group": "app", 

        "network-containment" : "subnet-10.0.36.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:36:04", 

        "l3-address": [
            {
                "ip-address": "10.0.36.4", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-port-ref-id": "6c82ea5c-ae43-11e5-bf7f-feff819cdc9f",
        "tenant": "tenant-dobre"
    }
}]

def get_endpoint_uri():
    return "/restconf/operations/endpoint:register-endpoint"

def get_endpoint_location_data():
    return [{
    "input": {
        "node-connector-id": "openflow:1:1", 
        "node-id": "openflow:1",
        "faas-port-ref-id": "165b3a20-adc7-11e5-bf7f-feff819cdc9f"
    }
},{
    "input": {
        "node-connector-id": "openflow:2:1", 
        "node-id": "openflow:2",
        "faas-port-ref-id": "6c82ea5c-ae43-11e5-bf7f-feff819cdc9f"
    }
}]

def get_endpoint_location_uri():
    return "/restconf/operations/faas-endpoints-locations:register-endpoint-location"

if __name__ == "__main__":
    # Launch main menu


    # Some sensible defaults
    controller=os.environ.get('ODL')
    if controller == None:
        sys.exit("No controller set.")
    

    #print "tenants"
    #tenants=get(controller,DEFAULT_PORT,CONF_TENANT)
    #print tenants
    
    print "creating tenant Policy"
    put(controller, DEFAULT_PORT, get_tenant_uri(), get_tenant_data(),False)
    
    print "registering endpoints"
    for endpoint in get_endpoint_data():
        post(controller, DEFAULT_PORT, get_endpoint_uri(),endpoint,False)

    print "registering endpoints locations"
    for loc in get_endpoint_location_data():
        post(controller, DEFAULT_PORT, get_endpoint_location_uri(),loc,False)
        
        
    
    
