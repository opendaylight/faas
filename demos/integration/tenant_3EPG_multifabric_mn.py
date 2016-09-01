import inputsCommon

#===============================================================================#
def get_tenant_data(tenantId):
  return {
        "policy:tenant": {
          "id": tenantId,
          "name": "GBP-FAAS-POC",
          "forwarding-context": {
            "l2-bridge-domain": [
              {
                "id": "bridge-domain1",
                "parent": "l3-context-vrf-red"
              },
              {
                "id": "bridge-domain2",
                "parent": "l3-context-vrf-red"
              },
              {
                "id": "bridge-external",
                "parent": "l3-context-vrf-red"
              },
              {
                "id": "bridge-domain3",
                "parent": "l3-context-vrf-red"
              }

            ],
            "l2-flood-domain": [
              {
                "id": "flood-domain3",
                "parent": "bridge-domain3"
              },
              {
                "id": "flood-domain2",
                "parent": "bridge-domain2"
              },
              {
                "id": "flood-domain1",
                "parent": "bridge-domain1"
              },
              {
                "id": "flood-external",
                "parent": "bridge-external"
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
                "ip-prefix": "10.0.35.0/24",
                "parent": "flood-domain2",
                "virtual-router-ip": "10.0.35.1"
              },
              {
                "id": "subnet-10.0.36.0/24",
                "ip-prefix": "10.0.36.0/24",
                "parent": "flood-domain1",
                "virtual-router-ip": "10.0.36.1"
              },
              {
                "id": "subnet-10.0.37.0/24",
                "ip-prefix": "10.0.37.0/24",
                "parent": "flood-domain3",
                "virtual-router-ip": "10.0.37.1"
              },
              {
                "id": "subnet-192.168.1.0/24",
                "ip-prefix": "192.168.1.0/24",
                "parent": "flood-external",
                "virtual-router-ip": "192.168.1.254"
              }
            ]
          },
          "policy": {
            "contract": [
              {
                "id": "webToAppContract",
                "clause": [
                  {
                    "name": "web2app-clause",
                    "subject-refs": [
                      "allow-http-subject",
                      "allow-icmp-subject"
                    ]
                  }
                ],
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
                            "name": "allow1",
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
                  },
                  {
                    "name": "allow-icmp-subject",
                    "rule": [
                      {
                        "name": "allow-icmp-rule",
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
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                "id": "appToDbContract",
                "clause": [
                  {
                    "name": "app2db-clause",
                    "subject-refs": [
                      "allow-sql-subject",
                      "allow-icmp-subject"
                    ]
                  }
                ],
                "subject": [
                  {
                    "name": "allow-sql-subject",
                    "rule": [
                      {
                        "name": "sql-sfc-rule-in",
                        "classifier-ref": [
                          {
                            "direction": "in",
                            "name": "sql-dest",
                            "instance-name": "sql-dest"
                          }
                        ],
                        "action-ref": [
                          {
                            "name": "allow1",
                            "order": 0
                          }
                        ]
                      },
                      {
                        "name": "sql-out-rule",
                        "classifier-ref": [
                          {
                              "name": "sql-src",
                              "instance-name": "sql-src",
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
                  },
                  {
                    "name": "allow-icmp-subject",
                    "rule": [
                      {
                        "name": "allow-icmp-rule",
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
                        ]
                      }
                    ]
                  }
                ]
              }
            ],
#            "external-implicit-group": [
#              {"id" : "internet-group"}
#            ],
            "endpoint-group": [
              { "id": "internet-group",
                "name": "internet-group",
                "consumer-named-selector": [
                  {
                  "name": "webservers-clients-icmp-http-contract",
                  "contract": [
                    "webToAppContract"
                    ]
                  }
                ]
              },
              {
                "id": "web",
                "consumer-named-selector": [
                  {
                    "contract": [
                      "webToAppContract"
                    ],
                    "name": "app-web-webToAppContract"
                  }
                ],
                "provider-named-selector": []
              },
              {
                "id": "app",
                "consumer-named-selector": [
                  {
                    "contract": [
                      "appToDbContract"
                    ],
                    "name": "app-db-appToDbContract"
                  }
                ],
                "provider-named-selector": [
                  {
                    "contract": [
                      "webToAppContract"
                    ],
                    "name": "app-web-webToAppContract"
                  }
                ]
              },
              {
                "id": "db",
                "consumer-named-selector": [],
                "provider-named-selector": [
                  {
                    "contract": [
                      "appToDbContract"
                    ],
                    "name": "app-db-appToDbContract"
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
                  "name": "sql-dest",
                  "parameter-value": [
                    {
                      "int-value": "6",
                      "name": "proto"
                    },
                    {
                      "int-value": "1433",
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
                },
                {
                  "name": "sql-src",
                  "classifier-definition-id": "Classifier-L4",
                  "parameter-value": [
                      {
                        "int-value": "6",
                        "name": "proto"
                      },
                      {
                        "int-value": "1433",
                        "name": "sourceport"
                      }
                  ]
                },
                {
                  "name": "icmp",
                  "classifier-definition-id": "Classifier-IP-Protocol",
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
def get_l3prefix_endpoint_data(tenantId):
    return [{
    "input": {
        "endpoint-group": "internet-group", 
        "l3-context" : "l3-context-vrf-red",
        "ip-prefix" : "192.168.2.0/24",
        "endpoint-l2-gateways": [
            {
                "l2-context": "bridge-external", 
                "mac-address": "80:38:bC:A1:33:c8"
            }
        ], 
        "endpoint-l3-gateways" : [
            {                
                "ip-address": "192.168.1.253", 
                "l3-context": "l3-context-vrf-red"
            }
        ],
        "tenant": tenantId
    }
    }]

def get_endpoint_data(tenantId):
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
        "nat-address" :"192.168.1.2",
        "faas-port-ref-id": "165b3a20-adc7-11e5-bf7f-feff819cdc9f",
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
        "faas-port-ref-id": "265b3a20-adc7-11e5-bf7f-feff819cdc9a",
        "tenant": tenantId
    }
  },
  {
    "input": {
        "endpoint-group": "web",
        "network-containment" : "subnet-10.0.35.0/24",
        "l2-context": "bridge-domain2",
        "mac-address": "00:00:00:00:35:08",
        "l3-address": [
            {
                "ip-address": "10.0.35.8",
                "l3-context": "l3-context-vrf-red"
            }
        ],
        "faas-port-ref-id": "fb691f41-3a9a-4f1f-a485-e7108a8d6e6c",
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
  },
  {
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
        "faas-port-ref-id": "7c82ea5c-ae43-11e5-bf7f-feff819cdc8a",
        "tenant": tenantId
    }
  },
  {
    "input": {
        "endpoint-group": "app",
        "network-containment" : "subnet-10.0.36.0/24",
        "l2-context": "bridge-domain1",
        "mac-address": "00:00:00:00:36:08",
        "l3-address": [
            {
                "ip-address": "10.0.36.8",
                "l3-context": "l3-context-vrf-red"
            }
        ],
        "faas-port-ref-id": "e67f264a-9948-4354-a948-d41d98846ae0",
        "tenant": tenantId
    }
  },  
  {
    "input": {
        "endpoint-group": "db",
        "network-containment" : "subnet-10.0.37.0/24",
        "l2-context": "bridge-domain3",
        "mac-address": "00:00:00:00:37:02",
        "l3-address": [
            {
                "ip-address": "10.0.37.2",
                "l3-context": "l3-context-vrf-red"
            }
        ],
        "faas-port-ref-id": "24eb3395-6c59-443a-9048-4c0b02ce1471",
        "tenant": tenantId
    }
  },
  {
    "input": {
        "endpoint-group": "db",
        "network-containment" : "subnet-10.0.37.0/24",
        "l2-context": "bridge-domain3",
        "mac-address": "00:00:00:00:37:03",
        "l3-address": [
            {
                "ip-address": "10.0.37.3",
                "l3-context": "l3-context-vrf-red"
            }
        ],
        "faas-port-ref-id": "34eb3395-6c59-443a-9048-4c0b02ce1472",
        "tenant": tenantId
    }
  },
  {
    "input": {
        "endpoint-group": "db",
        "network-containment" : "subnet-10.0.37.0/24",
        "l2-context": "bridge-domain3",
        "mac-address": "00:00:00:00:37:08",
        "l3-address": [
            {
                "ip-address": "10.0.37.8",
                "l3-context": "l3-context-vrf-red"
            }
        ],
        "faas-port-ref-id": "397962d5-1a05-47df-aa8b-af57604fd4e0",
        "tenant": tenantId
    }
  },
  {
    "input": {
        "endpoint-group": "internet-group",
        "network-containment" : "subnet-192.168.1.0/24",
        "l2-context": "bridge-external",
        "mac-address": "80:38:bC:A1:33:c8",
        "l3-address": [
            {
                "ip-address": "192.168.1.254",
                "l3-context": "l3-context-vrf-red"
            }
        ],
        "faas-port-ref-id": "bb72befb-d1c4-4a10-808b-d10b5cf3ae39",
        "tenant": tenantId
    }
  }
  ]

def get_endpoint_location_data(nodeId1, nodeId2, nodeId3, nodeId4, nodeId5):
    return [{
    "input": {
        "node-connector-id": "sw11-eth352",
        "node-id": nodeId1,
        "faas-port-ref-id": "165b3a20-adc7-11e5-bf7f-feff819cdc9f"
      }
    },
    {
    "input": {
        "node-connector-id": "sw11-eth353",
        "node-id": nodeId1,
        "faas-port-ref-id": "265b3a20-adc7-11e5-bf7f-feff819cdc9a"
      }
    },
    {
    "input": {
        "node-connector-id": "sw12-eth364",
        "node-id": nodeId2,
        "faas-port-ref-id": "6c82ea5c-ae43-11e5-bf7f-feff819cdc9f"
      }
    },
    {
    "input": {
        "node-connector-id": "sw12-eth365",
        "node-id": nodeId2,
        "faas-port-ref-id": "7c82ea5c-ae43-11e5-bf7f-feff819cdc8a"
      }
    },
    {
    "input": {
        "node-connector-id": "sw13-eth372",
        "node-id": nodeId3,
        "faas-port-ref-id": "24eb3395-6c59-443a-9048-4c0b02ce1471"
      }
    },
    {
    "input": {
        "node-connector-id": "sw13-eth373",
        "node-id": nodeId3,
        "faas-port-ref-id": "34eb3395-6c59-443a-9048-4c0b02ce1472"
      }
    },
    {
    "input": {
        "node-connector-id": "sw21-eth358",
        "node-id": nodeId4,
        "faas-port-ref-id": "fb691f41-3a9a-4f1f-a485-e7108a8d6e6c"
      }
    },
    {
    "input": {
        "node-connector-id": "sw21-eth368",
        "node-id": nodeId4,
        "faas-port-ref-id": "e67f264a-9948-4354-a948-d41d98846ae0"
      }
    },
    {
    "input": {
        "node-connector-id": "sw21-eth378",
        "node-id": nodeId4,
        "faas-port-ref-id": "397962d5-1a05-47df-aa8b-af57604fd4e0"
      }
    },
    {
    "input": {
        "node-connector-id": 'sw2-eth2',
        "node-id": nodeId5,
        "faas-port-ref-id": "bb72befb-d1c4-4a10-808b-d10b5cf3ae39"
      }
    }
  ]


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

