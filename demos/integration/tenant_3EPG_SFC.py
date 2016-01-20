import inputsCommon

#===============================================================================#
def get_tenant_data(tenantId):
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
              },
              {
                "id": "subnet-10.0.37.0/24",
                "ip-prefix": "10.0.37.1/24",
                "parent": "flood-domain3",
                "virtual-router-ip": "10.0.37.1"
              }
            ]
          },
          "policy": {
            "contract": [
              {
                "id": "webToAppContract",
                "clause": [
                  {
                    "name": "sfc-clause",
                    "subject-refs": [
                      "allow-http-subject"
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
              },
              {
                "id": "appToDbContract",
                "clause": [
                  {
                    "name": "sfc-clause",
                    "subject-refs": [
                      "allow-http-subject"
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
                      "name": "sfc_chain_name",
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
  }]

def get_endpoint_location_data(nc_id1, nc_id2, nc_id3, nodeId1, nodeId2, nodeId3):
    return [{
    "input": {
        "node-connector-id": nc_id1,
        "node-id": nodeId1,
        "faas-port-ref-id": "165b3a20-adc7-11e5-bf7f-feff819cdc9f"
      }
    },
    {
    "input": {
        "node-connector-id": nc_id2,
        "node-id": nodeId2,
        "faas-port-ref-id": "6c82ea5c-ae43-11e5-bf7f-feff819cdc9f"
      }
    },
    {
    "input": {
        "node-connector-id": nc_id3,
        "node-id": nodeId3,
        "faas-port-ref-id": "24eb3395-6c59-443a-9048-4c0b02ce1471"
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

