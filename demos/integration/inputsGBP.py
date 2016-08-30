import inputsCommon

#===============================================================================# 
getLogicalNetworksUri_gc = "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operational/faas-logical-networks:tenant-logical-networks/"
getUlnUri_gc = "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operational/faas:logical-networks"
getMappedEntities_gc="http://"+inputsCommon.odlIpAddr_gc+"/restconf/operational/faas:mapped-tenants-entities"

#===============================================================================# 
def get_tenant_data_layer2_acl(tenantId):
    return {
        "policy:tenant": {
          "id": tenantId,
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
                "id": "flood-domain-1",
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
                "parent": "flood-domain-1",
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
                "faas-epg-scope-type": "public",
                #"faas-public-contract-id": "webToAppContract",
                "faas-epg-subnet": [
                      "subnet-10.0.35.0/24"
                 ],
                "provider-named-selector": []
              },
              {
                "consumer-named-selector": [],
                "id": "app",
                "faas-epg-subnet": [
                    "subnet-10.0.36.0/24"
                 ],
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

def get_tenant_uri(tenantId):
    return "http://"+inputsCommon.odlIpAddr_gc+"/restconf/config/policy:tenants/policy:tenant/"+tenantId

def get_endpoint_data(tenantId):
    return {
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
        "faas-node-connector-id": "openflow:1:1", 
        "faas-node-id": "openflow:1",
        "tenant": tenantId
    }
  }

def get_endpoint_data2(tenantId):
     return  {
    "input": {

        "endpoint-group": "web", 

        "network-containment" : "subnet-10.0.35.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:35:03", 

        "l3-address": [
            {
                "ip-address": "10.0.35.3", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:1:2", 
        "faas-node-id": "openflow:1",
        "tenant": tenantId
    }
  }

def get_endpoint_data3(tenantId):
    return  {
    "input": {

        "endpoint-group": "web", 

        "network-containment" : "subnet-10.0.35.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:35:04", 

        "l3-address": [
            {
                "ip-address": "10.0.35.4", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:1:3", 
        "faas-node-id": "openflow:1",
        "tenant": tenantId
    }
  }

def get_endpoint_data4(tenantId):
    return  {
    "input": {

        "endpoint-group": "web", 

        "network-containment" : "subnet-10.0.35.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:35:05", 

        "l3-address": [
            {
                "ip-address": "10.0.35.5", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:1:4", 
        "faas-node-id": "openflow:1",
        "tenant": tenantId
    }
  }

def get_endpoint_data5(tenantId):
     return {
    "input": {

        "endpoint-group": "app", 

        "network-containment" : "subnet-10.0.36.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:36:02", 

        "l3-address": [
            {
                "ip-address": "10.0.36.2", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:2:1", 
        "faas-node-id": "openflow:2",
        "tenant": tenantId
    }
  }

def get_endpoint_data6(tenantId):
    return  {
    "input": {

        "endpoint-group": "app", 

        "network-containment" : "subnet-10.0.36.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:36:03", 

        "l3-address": [
            {
                "ip-address": "10.0.36.3", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:2:2", 
        "faas-node-id": "openflow:2",
        "tenant": tenantId
    }
  }

def get_endpoint_data7(tenantId):
     return   {
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
        "faas-node-connector-id": "openflow:2:3", 
        "faas-node-id": "openflow:2",
        "tenant": tenantId
    }
  }

def get_endpoint_data8(tenantId):
    return   {
    "input": {

        "endpoint-group": "app", 

        "network-containment" : "subnet-10.0.36.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:36:05", 

        "l3-address": [
            {
                "ip-address": "10.0.36.5", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:2:4", 
        "faas-node-id": "openflow:2",
        "tenant": tenantId
    }
  }

def get_endpoint_uri():
    return "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operations/endpoint:register-endpoint"

def get_l3prefix_endpoint_uri():
    return "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operations/endpoint:register-l3-prefix-endpoint"

def get_unreg_endpoint_uri():
    return "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operations/endpoint:unregister-endpoint"

def get_endpoint_location_uri():
    return "http://"+inputsCommon.odlIpAddr_gc+"/restconf/operations/faas-endpoints-locations:register-endpoint-location"

#===============================================================================#
def get_tenant_data_layer3_acl(tenantId):
        return {
        "policy:tenant": {
          "id": tenantId,
          "name": "GBPPOC",
          "forwarding-context": {
            "l2-bridge-domain": [
              {
                "id": "bridge-domain1",
                "parent": "l3-context-vrf-red"
              },
              {
                "id": "bridge-domain2",
                "parent": "l3-context-vrf-red"
              }
            ],
            "l2-flood-domain": [
              {
                "id": "flood-domain2",
                "parent": "bridge-domain2"
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

#===============================================================================#
def get_tenant_data_layer3_sfc(tenantId):
        return {
        "policy:tenant": {
          "id": tenantId,
          "name": "GBPPOC",
          "forwarding-context": {
            "l2-bridge-domain": [
              {
                "id": "bridge-domain1",
                "parent": "l3-context-vrf-red"
              },
              {
                "id": "bridge-domain2",
                "parent": "l3-context-vrf-red"
              }
            ],
            "l2-flood-domain": [
              {
                "id": "flood-domain2",
                "parent": "bridge-domain2"
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
                    "name": "sfc-clause",
                    "subject-refs": [
                      "allow-http-subject"
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
                          }
                        ],
                        "action-ref": [
                          {
                            "name": "chain1",
                            "order": 0
                          }
                        ],
                        "name": "http-sfc-rule-in"
                      },
                      {
                        "name": "http-out-rule",
                        "classifier-ref": [
                        {
                              "name": "http-src",
                              "instance-name": "http-src",
                              "direction": "out"
                        }
                        ],
                          "action-ref": [
                            {
                              "name": "allow1",
                              "order": 0
                            }
                        ]
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
                  "name": "http-src",
                  "classifier-definition-id": "Classifier-L4",
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
                }
              ],
              "action-instance": [
                {
                  "name": "chain1",
                  "action-definition-id": "Action-Chain",
                  "parameter-value": [
                    {
                      "name": "sfc-chain-name",
                      "string-value": "SFCGBP"
                    }
                  ]
                },
                {
                    "name": "allow1",
                    "action-definition-id": "Action-Allow"
                }
              ]
            }
          }
        }
      }

#===============================================================================# 
def get_endpoint_data_layer3(tenantId):
    return [{
    "input": {
        "endpoint-group": "web", 
        "network-containment" : "subnet-10.0.35.0/24",
        "l2-context": "bridge-domain2", 
        "mac-address": "00:00:00:00:35:02", 
        "l3-address": [
            {
                "ip-address": "10.0.35.2", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-port-ref-id": "165b3a20-adc7-11e5-bf7f-feff819cdc9f",
        "tenant": tenantId
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
        "tenant": tenantId
    }
  }]

def get_endpoint_location_data(nc_id1, nc_id2, nodeId1, nodeId2):
    return [{
    "input": {
        "node-connector-id": nc_id1,
        "node-id": nodeId1,
        "faas-port-ref-id": "165b3a20-adc7-11e5-bf7f-feff819cdc9f"
    }
    },{
    "input": {
        "node-connector-id": nc_id2,
        "node-id": nodeId2,
        "faas-port-ref-id": "6c82ea5c-ae43-11e5-bf7f-feff819cdc9f"
    }
   }]


def get_endpoint_location_data_old():
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

def get_endpoint_data_layer3_old(tenantId):
    return [{
    "input": {

        "endpoint-group": "web", 

        "network-containment" : "subnet-10.0.35.0/24",

        "l2-context": "bridge-domain2", 
        "mac-address": "00:00:00:00:35:02", 

        "l3-address": [
            {
                "ip-address": "10.0.35.2", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:1:1", 
        "faas-node-id": "openflow:1",
        "tenant": tenantId
    }
},
            {
    "input": {

        "endpoint-group": "web", 

        "network-containment" : "subnet-10.0.35.0/24",

        "l2-context": "bridge-domain2", 
        "mac-address": "00:00:00:00:35:03", 

        "l3-address": [
            {
                "ip-address": "10.0.35.3", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:1:2", 
        "faas-node-id": "openflow:1",
        "tenant": tenantId
    }
},
            {
    "input": {

        "endpoint-group": "web", 

        "network-containment" : "subnet-10.0.35.0/24",

        "l2-context": "bridge-domain2", 
        "mac-address": "00:00:00:00:35:04", 

        "l3-address": [
            {
                "ip-address": "10.0.35.4", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:1:3", 
        "faas-node-id": "openflow:1",
        "tenant": tenantId
    }
},
            {
    "input": {

        "endpoint-group": "web", 

        "network-containment" : "subnet-10.0.35.0/24",

        "l2-context": "bridge-domain2", 
        "mac-address": "00:00:00:00:35:05", 

        "l3-address": [
            {
                "ip-address": "10.0.35.5", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:1:4", 
        "faas-node-id": "openflow:1",
        "tenant": tenantId
    }
},
            {
    "input": {

        "endpoint-group": "app", 

        "network-containment" : "subnet-10.0.36.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:36:02", 

        "l3-address": [
            {
                "ip-address": "10.0.36.2", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:2:1", 
        "faas-node-id": "openflow:2",
        "tenant": tenantId
    }
},
            {
    "input": {

        "endpoint-group": "app", 

        "network-containment" : "subnet-10.0.36.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:36:03", 

        "l3-address": [
            {
                "ip-address": "10.0.36.3", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:2:2", 
        "faas-node-id": "openflow:2",
        "tenant": tenantId
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
        "faas-node-connector-id": "openflow:2:3", 
        "faas-node-id": "openflow:2",
        "tenant": tenantId
    }
},{
    "input": {

        "endpoint-group": "app", 

        "network-containment" : "subnet-10.0.36.0/24",

        "l2-context": "bridge-domain1", 
        "mac-address": "00:00:00:00:36:05", 

        "l3-address": [
            {
                "ip-address": "10.0.36.5", 
                "l3-context": "l3-context-vrf-red"
            }
        ], 
        "faas-node-connector-id": "openflow:2:4", 
        "faas-node-id": "openflow:2",
        "tenant": tenantId
    }
}]

#===============================================================================#
def get_unreg_endpoint_data_layer3():
    return {
    "input": {

       "l2": [
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
                "ip-address": "10.0.35.2",
                "l3-context": "l3-context-vrf-red"
            },
            {
                "ip-address": "10.0.36.4",
                "l3-context": "l3-context-vrf-red"
            }
        ]
    }
}

def get_unreg_endpoint_data_layer3_old():
    return {
    "input": {
      
       "l2": [
          {
             "l2-context": "bridge-domain2", 
             "mac-address": "00:00:00:00:35:02"
          },
          {
             "l2-context": "bridge-domain2", 
             "mac-address": "00:00:00:00:35:03"
          },
          {
             "l2-context": "bridge-domain2", 
             "mac-address": "00:00:00:00:35:04"
          },
          {
             "l2-context": "bridge-domain2", 
             "mac-address": "00:00:00:00:35:05"
          },
          {
             "l2-context": "bridge-domain1", 
             "mac-address": "00:00:00:00:36:02"
          },
          {
            "l2-context": "bridge-domain1", 
            "mac-address": "00:00:00:00:36:03"
          },
          {
            "l2-context": "bridge-domain1", 
            "mac-address": "00:00:00:00:36:04"
          },
          {
            "l2-context": "bridge-domain1", 
            "mac-address": "00:00:00:00:36:05"
          }
        ],      

        "l3": [
            {
                "ip-address": "10.0.35.2", 
                "l3-context": "l3-context-vrf-red"
            },
            {
                "ip-address": "10.0.35.3", 
                "l3-context": "l3-context-vrf-red"
            },
            {
                "ip-address": "10.0.35.4", 
                "l3-context": "l3-context-vrf-red"
            },
            {
                "ip-address": "10.0.35.5", 
                "l3-context": "l3-context-vrf-red"
            },
            {
                "ip-address": "10.0.36.2", 
                "l3-context": "l3-context-vrf-red"
            },
            {
                "ip-address": "10.0.36.3", 
                "l3-context": "l3-context-vrf-red"
            },
            {
                "ip-address": "10.0.36.4", 
                "l3-context": "l3-context-vrf-red"
            },
            {
                "ip-address": "10.0.36.5", 
                "l3-context": "l3-context-vrf-red"
            }
        ]
    }
}
