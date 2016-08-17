{
  "tenant-logical-networks": {
    "tenant-logical-network": [
      {
        "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
        "security-rule-groups-container": {
          "security-rule-groups": [
            {
              "uuid": "d300c6ad-0a90-4e09-8571-8cf9fe31e58d",
              "security-rule-group": [
                {
                  "name": "allow-http-subject",
                  "security-rule": [
                    {
                      "name": "http-sfc-rule-in",
                      "rule-classifier": [
                        {
                          "name": "http-dest",
                          "additional-info": "Classifier-L4",
                          "parameter-value": [
                            {
                              "name": "proto",
                              "int-value": 6
                            },
                            {
                              "name": "destport",
                              "int-value": 80
                            }
                          ],
                          "direction": "in"
                        }
                      ],
                      "rule-action": [
                        {
                          "name": "chain1",
                          "additional-info": "Action-Chain",
                          "parameter-value": [
                            {
                              "name": "sfc-chain-name",
                              "string-value": "SFCGBP"
                            }
                          ],
                          "order": 0
                        }
                      ]
                    },
                    {
                      "name": "http-out-rule",
                      "rule-classifier": [
                        {
                          "name": "http-src",
                          "additional-info": "Classifier-L4",
                          "parameter-value": [
                            {
                              "name": "sourceport",
                              "int-value": 80
                            },
                            {
                              "name": "proto",
                              "int-value": 6
                            }
                          ],
                          "direction": "out"
                        }
                      ],
                      "rule-action": [
                        {
                          "name": "allow1",
                          "additional-info": "Action-Allow",
                          "order": 0
                        }
                      ]
                    }
                  ]
                }
              ],
              "description": "gbp-contract",
              "ports": [
                "f018d38f-6167-49d4-987b-3fad51c8f952"
              ],
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "webToAppContract"
            },
            {
              "uuid": "ef9269b0-e09c-48da-ab60-6bbba97f0b1d",
              "security-rule-group": [
                {
                  "name": "allow-http-subject",
                  "security-rule": [
                    {
                      "name": "http-sfc-rule-in",
                      "rule-classifier": [
                        {
                          "name": "http-dest",
                          "additional-info": "Classifier-L4",
                          "parameter-value": [
                            {
                              "name": "proto",
                              "int-value": 6
                            },
                            {
                              "name": "destport",
                              "int-value": 80
                            }
                          ],
                          "direction": "in"
                        }
                      ],
                      "rule-action": [
                        {
                          "name": "chain1",
                          "additional-info": "Action-Chain",
                          "parameter-value": [
                            {
                              "name": "sfc-chain-name",
                              "string-value": "SFCGBP"
                            }
                          ],
                          "order": 0
                        }
                      ]
                    },
                    {
                      "name": "http-out-rule",
                      "rule-classifier": [
                        {
                          "name": "http-src",
                          "additional-info": "Classifier-L4",
                          "parameter-value": [
                            {
                              "name": "sourceport",
                              "int-value": 80
                            },
                            {
                              "name": "proto",
                              "int-value": 6
                            }
                          ],
                          "direction": "out"
                        }
                      ],
                      "rule-action": [
                        {
                          "name": "allow1",
                          "additional-info": "Action-Allow",
                          "order": 0
                        }
                      ]
                    }
                  ]
                }
              ],
              "description": "gbp-contract",
              "ports": [
                "96f36c2b-ebd0-4a39-8bd0-0f34042f8a49"
              ],
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "appToDbContract"
            }
          ]
        },
        "subnets": {
          "subnet": [
            {
              "uuid": "a4b58b3a-cf90-4b7a-ac64-0c790185336c",
              "ip-prefix": "10.0.36.1/24",
              "enable-dhcp": false,
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "subnet-10.0.36.0/24",
              "description": "gbp-subnet",
              "port": [
                "ad503c1b-5498-414a-a493-c784a83b80f8",
                "16c63889-e6a9-4997-bca4-37635931519e",
                "494f4112-d246-446e-9a92-ca822e5c5ece"
              ],
              "virtual-router-ip": "10.0.36.1"
            },
            {
              "uuid": "22db872a-d4e8-40fb-b53f-176e34b84b42",
              "ip-prefix": "10.0.35.1/24",
              "enable-dhcp": false,
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "subnet-10.0.35.0/24",
              "description": "gbp-subnet",
              "port": [
                "7ed1834f-d84b-49cc-b870-e1883ac43c65",
                "c1f23726-f02b-4c84-aae9-6aaa1862eec2"
              ],
              "virtual-router-ip": "10.0.35.1"
            },
            {
              "uuid": "866fe943-4b0a-4568-b76e-b925a2ac61f4",
              "ip-prefix": "10.0.37.1/24",
              "enable-dhcp": false,
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "subnet-10.0.37.0/24",
              "description": "gbp-subnet",
              "port": [
                "a7cc7c82-9e3a-4595-bedf-3490f8df339f",
                "d98659d6-d810-482d-8a97-acabcc9d386c"
              ],
              "virtual-router-ip": "10.0.37.1"
            }
          ]
        },
        "logical-switches": {
          "logical-switch": [
            {
              "uuid": "cc74b30e-95dc-4737-baa0-dd1f621485f2",
              "admin-state-up": true,
              "description": "gbp-epg",
              "port": [
                "1352e640-935f-4f2c-98ba-700c3a3eebb2",
                "8137d9e5-a5da-4dec-989b-8ea5916f9c55"
              ],
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "web"
            },
            {
              "uuid": "259336f5-1a6e-4ad8-b665-ef8458e3b221",
              "admin-state-up": true,
              "description": "gbp-epg",
              "port": [
                "b1c1b8d0-ed5b-45b1-9c16-7d3ef8f258cd",
                "096769aa-1b66-4faa-9130-4fa0d0e92dbb"
              ],
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "app"
            },
            {
              "uuid": "6e8381d2-4f95-4ac2-8c57-f068185ce2a8",
              "admin-state-up": true,
              "description": "gbp-epg",
              "port": [
                "2f0fc89d-0486-4f6a-b89e-a316bcd8dbf8",
                "4ae21cb9-f2b8-4176-bc26-69522ab5f4db"
              ],
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "app"
            },
            {
              "uuid": "0587db2f-0eb6-43ff-aa5c-68a964fe0ffe",
              "admin-state-up": true,
              "description": "gbp-epg",
              "port": [
                "7c9ee37b-4ecd-4044-b25a-9c40569fc292",
                "66ebc2ac-fc12-4b3d-9289-e3e4257cfc72"
              ],
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "db"
            }
          ]
        },
        "edges": {
          "edge": [
            {
              "uuid": "68a446c2-2a63-41f2-a9c2-f939e3d61e28",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "d98659d6-d810-482d-8a97-acabcc9d386c",
              "left-port-id": "0121a445-2e57-41d6-9894-9910d8fc1c23"
            },
            {
              "uuid": "01d52391-98e5-43ec-9c4d-7b9b951c7ba6",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "66ebc2ac-fc12-4b3d-9289-e3e4257cfc72",
              "left-port-id": "6c0b2ac0-4adf-434f-9279-8afe5b3840bf"
            },
            {
              "uuid": "48767954-8868-4532-badf-7a910c905d67",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "16c63889-e6a9-4997-bca4-37635931519e",
              "left-port-id": "32f3592b-0fb5-4399-b170-f6e9ef6e7ce6"
            },
            {
              "uuid": "8d211200-e86e-469e-85a2-6bcc89e96fd0",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "7ed1834f-d84b-49cc-b870-e1883ac43c65",
              "left-port-id": "1352e640-935f-4f2c-98ba-700c3a3eebb2"
            },
            {
              "uuid": "d801b44a-3f89-474b-a3c1-eab2dec3ef55",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "494f4112-d246-446e-9a92-ca822e5c5ece",
              "left-port-id": "b1c1b8d0-ed5b-45b1-9c16-7d3ef8f258cd"
            },
            {
              "uuid": "32bac9ca-9df4-42eb-adda-a3aba7b5d828",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "ad503c1b-5498-414a-a493-c784a83b80f8",
              "left-port-id": "4ae21cb9-f2b8-4176-bc26-69522ab5f4db"
            },
            {
              "uuid": "ed04bf36-9098-40f9-b1fd-30c53e83cad1",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "8137d9e5-a5da-4dec-989b-8ea5916f9c55",
              "left-port-id": "4671f908-c3a8-425d-85d8-129a14df9e94"
            },
            {
              "uuid": "1347617d-3ff9-42e7-b6c5-56034a6cf729",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "096769aa-1b66-4faa-9130-4fa0d0e92dbb",
              "left-port-id": "f446f1f1-ecf3-4e9c-a5f0-bfc79bc79fc6"
            },
            {
              "uuid": "0bf9e370-1787-45ce-8ce8-3d1d7eb11867",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "a7cc7c82-9e3a-4595-bedf-3490f8df339f",
              "left-port-id": "7c9ee37b-4ecd-4044-b25a-9c40569fc292"
            },
            {
              "uuid": "e40a9f70-7084-4fc6-a441-b68dedd11ef0",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "c1f23726-f02b-4c84-aae9-6aaa1862eec2",
              "left-port-id": "5ae32b88-2dc2-46a3-ab87-8d6d14762ce8"
            },
            {
              "uuid": "5bf2f21e-94c5-441f-bdb9-df5dfef76b9e",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "f018d38f-6167-49d4-987b-3fad51c8f952",
              "left-port-id": "a2a6a65f-2ef7-413c-81d7-90f1898c77e7"
            },
            {
              "uuid": "af885ab2-5487-495e-b6db-0babd8ae7490",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "96f36c2b-ebd0-4a39-8bd0-0f34042f8a49",
              "left-port-id": "6713d35e-15fb-43d2-8388-9d4d99d8d300"
            },
            {
              "uuid": "34bbfdbe-e654-4a2e-82c5-edb62ffc285c",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "right-port-id": "2f0fc89d-0486-4f6a-b89e-a316bcd8dbf8",
              "left-port-id": "fa30103a-934f-43b0-8fa9-de75a562d6d1"
            }
          ]
        },
        "ports": {
          "port": [
            {
              "uuid": "1352e640-935f-4f2c-98ba-700c3a3eebb2",
              "location-type": "switch-type",
              "admin-state-up": true,
              "location-id": "cc74b30e-95dc-4737-baa0-dd1f621485f2",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "8d211200-e86e-469e-85a2-6bcc89e96fd0"
            },
            {
              "uuid": "494f4112-d246-446e-9a92-ca822e5c5ece",
              "location-type": "subnet-type",
              "admin-state-up": true,
              "location-id": "a4b58b3a-cf90-4b7a-ac64-0c790185336c",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "d801b44a-3f89-474b-a3c1-eab2dec3ef55"
            },
            {
              "uuid": "5ae32b88-2dc2-46a3-ab87-8d6d14762ce8",
              "location-type": "endpoint-type",
              "mac-address": "00:00:00:00:35:02",
              "admin-state-up": true,
              "location-id": "2f452e68-aa6a-4400-a028-ad780dcc95af",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "e40a9f70-7084-4fc6-a441-b68dedd11ef0",
              "private-ips": [
                {
                  "subnet-id": "22db872a-d4e8-40fb-b53f-176e34b84b42",
                  "ip-address": "10.0.35.2"
                }
              ]
            },
            {
              "uuid": "ad503c1b-5498-414a-a493-c784a83b80f8",
              "location-type": "subnet-type",
              "admin-state-up": true,
              "location-id": "a4b58b3a-cf90-4b7a-ac64-0c790185336c",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "32bac9ca-9df4-42eb-adda-a3aba7b5d828"
            },
            {
              "uuid": "6713d35e-15fb-43d2-8388-9d4d99d8d300",
              "location-type": "router-type",
              "admin-state-up": true,
              "location-id": "98698614-19a6-4af0-9313-e451c649a3f4",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "af885ab2-5487-495e-b6db-0babd8ae7490"
            },
            {
              "uuid": "6c0b2ac0-4adf-434f-9279-8afe5b3840bf",
              "location-type": "router-type",
              "admin-state-up": true,
              "location-id": "18840290-f083-43d6-a62f-923f17ddc60d",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "01d52391-98e5-43ec-9c4d-7b9b951c7ba6"
            },
            {
              "uuid": "7ed1834f-d84b-49cc-b870-e1883ac43c65",
              "location-type": "subnet-type",
              "admin-state-up": true,
              "location-id": "22db872a-d4e8-40fb-b53f-176e34b84b42",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "8d211200-e86e-469e-85a2-6bcc89e96fd0"
            },
            {
              "uuid": "b1c1b8d0-ed5b-45b1-9c16-7d3ef8f258cd",
              "location-type": "switch-type",
              "admin-state-up": true,
              "location-id": "259336f5-1a6e-4ad8-b665-ef8458e3b221",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "d801b44a-3f89-474b-a3c1-eab2dec3ef55"
            },
            {
              "uuid": "f018d38f-6167-49d4-987b-3fad51c8f952",
              "location-type": "router-type",
              "admin-state-up": true,
              "location-id": "47e3a4ce-b81a-433a-8575-b22a65878ee3",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "5bf2f21e-94c5-441f-bdb9-df5dfef76b9e",
              "security-rules-groups": [
                "d300c6ad-0a90-4e09-8571-8cf9fe31e58d"
              ]
            },
            {
              "uuid": "4671f908-c3a8-425d-85d8-129a14df9e94",
              "location-type": "router-type",
              "admin-state-up": true,
              "location-id": "c6dc0bb7-bd69-4df1-a4ef-7c2cc5f65ca5",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "ed04bf36-9098-40f9-b1fd-30c53e83cad1"
            },
            {
              "uuid": "d98659d6-d810-482d-8a97-acabcc9d386c",
              "location-type": "subnet-type",
              "admin-state-up": true,
              "location-id": "866fe943-4b0a-4568-b76e-b925a2ac61f4",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "68a446c2-2a63-41f2-a9c2-f939e3d61e28"
            },
            {
              "uuid": "096769aa-1b66-4faa-9130-4fa0d0e92dbb",
              "location-type": "switch-type",
              "admin-state-up": true,
              "location-id": "259336f5-1a6e-4ad8-b665-ef8458e3b221",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "1347617d-3ff9-42e7-b6c5-56034a6cf729"
            },
            {
              "uuid": "f446f1f1-ecf3-4e9c-a5f0-bfc79bc79fc6",
              "location-type": "router-type",
              "admin-state-up": true,
              "location-id": "47e3a4ce-b81a-433a-8575-b22a65878ee3",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "1347617d-3ff9-42e7-b6c5-56034a6cf729"
            },
            {
              "uuid": "4ae21cb9-f2b8-4176-bc26-69522ab5f4db",
              "location-type": "switch-type",
              "admin-state-up": true,
              "location-id": "6e8381d2-4f95-4ac2-8c57-f068185ce2a8",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "32bac9ca-9df4-42eb-adda-a3aba7b5d828"
            },
            {
              "uuid": "32f3592b-0fb5-4399-b170-f6e9ef6e7ce6",
              "location-type": "endpoint-type",
              "mac-address": "00:00:00:00:36:04",
              "admin-state-up": true,
              "location-id": "7b074719-fe3b-4b8b-8cca-1c55374c8b1d",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "48767954-8868-4532-badf-7a910c905d67",
              "private-ips": [
                {
                  "subnet-id": "a4b58b3a-cf90-4b7a-ac64-0c790185336c",
                  "ip-address": "10.0.36.4"
                }
              ]
            },
            {
              "uuid": "a2a6a65f-2ef7-413c-81d7-90f1898c77e7",
              "location-type": "router-type",
              "admin-state-up": true,
              "location-id": "c6dc0bb7-bd69-4df1-a4ef-7c2cc5f65ca5",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "5bf2f21e-94c5-441f-bdb9-df5dfef76b9e"
            },
            {
              "uuid": "a7cc7c82-9e3a-4595-bedf-3490f8df339f",
              "location-type": "subnet-type",
              "admin-state-up": true,
              "location-id": "866fe943-4b0a-4568-b76e-b925a2ac61f4",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "0bf9e370-1787-45ce-8ce8-3d1d7eb11867"
            },
            {
              "uuid": "fa30103a-934f-43b0-8fa9-de75a562d6d1",
              "location-type": "router-type",
              "admin-state-up": true,
              "location-id": "98698614-19a6-4af0-9313-e451c649a3f4",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "34bbfdbe-e654-4a2e-82c5-edb62ffc285c"
            },
            {
              "uuid": "c1f23726-f02b-4c84-aae9-6aaa1862eec2",
              "location-type": "subnet-type",
              "admin-state-up": true,
              "location-id": "22db872a-d4e8-40fb-b53f-176e34b84b42",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "e40a9f70-7084-4fc6-a441-b68dedd11ef0"
            },
            {
              "uuid": "2f0fc89d-0486-4f6a-b89e-a316bcd8dbf8",
              "location-type": "switch-type",
              "admin-state-up": true,
              "location-id": "6e8381d2-4f95-4ac2-8c57-f068185ce2a8",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "34bbfdbe-e654-4a2e-82c5-edb62ffc285c"
            },
            {
              "uuid": "7c9ee37b-4ecd-4044-b25a-9c40569fc292",
              "location-type": "switch-type",
              "admin-state-up": true,
              "location-id": "0587db2f-0eb6-43ff-aa5c-68a964fe0ffe",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "0bf9e370-1787-45ce-8ce8-3d1d7eb11867"
            },
            {
              "uuid": "96f36c2b-ebd0-4a39-8bd0-0f34042f8a49",
              "location-type": "router-type",
              "admin-state-up": true,
              "location-id": "18840290-f083-43d6-a62f-923f17ddc60d",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "af885ab2-5487-495e-b6db-0babd8ae7490",
              "security-rules-groups": [
                "ef9269b0-e09c-48da-ab60-6bbba97f0b1d"
              ]
            },
            {
              "uuid": "16c63889-e6a9-4997-bca4-37635931519e",
              "location-type": "subnet-type",
              "admin-state-up": true,
              "location-id": "a4b58b3a-cf90-4b7a-ac64-0c790185336c",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "48767954-8868-4532-badf-7a910c905d67"
            },
            {
              "uuid": "8137d9e5-a5da-4dec-989b-8ea5916f9c55",
              "location-type": "switch-type",
              "admin-state-up": true,
              "location-id": "cc74b30e-95dc-4737-baa0-dd1f621485f2",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "ed04bf36-9098-40f9-b1fd-30c53e83cad1"
            },
            {
              "uuid": "66ebc2ac-fc12-4b3d-9289-e3e4257cfc72",
              "location-type": "switch-type",
              "admin-state-up": true,
              "location-id": "0587db2f-0eb6-43ff-aa5c-68a964fe0ffe",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "01d52391-98e5-43ec-9c4d-7b9b951c7ba6"
            },
            {
              "uuid": "0121a445-2e57-41d6-9894-9910d8fc1c23",
              "location-type": "endpoint-type",
              "mac-address": "00:00:00:00:37:02",
              "admin-state-up": true,
              "location-id": "7d0fb274-f243-4e8f-8f61-826f1e46bae5",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "edge-id": "68a446c2-2a63-41f2-a9c2-f939e3d61e28",
              "private-ips": [
                {
                  "subnet-id": "866fe943-4b0a-4568-b76e-b925a2ac61f4",
                  "ip-address": "10.0.37.2"
                }
              ]
            }
          ]
        },
        "logical-routers": {
          "logical-router": [
            {
              "uuid": "18840290-f083-43d6-a62f-923f17ddc60d",
              "admin-state-up": true,
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "db",
              "public": false,
              "description": "gbp-epg",
              "port": [
                "6c0b2ac0-4adf-434f-9279-8afe5b3840bf",
                "96f36c2b-ebd0-4a39-8bd0-0f34042f8a49"
              ]
            },
            {
              "uuid": "98698614-19a6-4af0-9313-e451c649a3f4",
              "admin-state-up": true,
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "app",
              "public": false,
              "description": "gbp-epg",
              "port": [
                "6713d35e-15fb-43d2-8388-9d4d99d8d300",
                "fa30103a-934f-43b0-8fa9-de75a562d6d1"
              ]
            },
            {
              "uuid": "c6dc0bb7-bd69-4df1-a4ef-7c2cc5f65ca5",
              "admin-state-up": true,
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "web",
              "public": false,
              "description": "gbp-epg",
              "port": [
                "4671f908-c3a8-425d-85d8-129a14df9e94",
                "a2a6a65f-2ef7-413c-81d7-90f1898c77e7"
              ]
            },
            {
              "uuid": "47e3a4ce-b81a-433a-8575-b22a65878ee3",
              "admin-state-up": true,
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "app",
              "public": false,
              "description": "gbp-epg",
              "port": [
                "f446f1f1-ecf3-4e9c-a5f0-bfc79bc79fc6",
                "f018d38f-6167-49d4-987b-3fad51c8f952"
              ]
            }
          ]
        },
        "endpoints-locations": {
          "endpoint-location": [
            {
              "uuid": "2f452e68-aa6a-4400-a028-ad780dcc95af",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "bridge-domain2",
              "node-id": "ovsdb://uuid/67a42b09-526c-4904-95f1-af6e2c5fe1d0/bridge/sw1",
              "faas-port-ref-id": "165b3a20-adc7-11e5-bf7f-feff819cdc9f",
              "description": "gbp-endpoint",
              "node-connector-id": "vethl-h35-2",
              "port": "5ae32b88-2dc2-46a3-ab87-8d6d14762ce8"
            },
            {
              "uuid": "7b074719-fe3b-4b8b-8cca-1c55374c8b1d",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "bridge-domain1",
              "node-id": "ovsdb://uuid/f44aecc4-8d5e-4a4b-bc6b-71e84452b0f1/bridge/sw6",
              "faas-port-ref-id": "6c82ea5c-ae43-11e5-bf7f-feff819cdc9f",
              "description": "gbp-endpoint",
              "node-connector-id": "vethl-h36-4",
              "port": "32f3592b-0fb5-4399-b170-f6e9ef6e7ce6"
            },
            {
              "uuid": "7d0fb274-f243-4e8f-8f61-826f1e46bae5",
              "tenant-id": "f5c7d344-d1c7-4208-8531-2c2693657e12",
              "name": "bridge-domain3",
              "node-id": "ovsdb://uuid/d2a56e91-cb8c-438e-841c-30302c779665/bridge/sw7",
              "faas-port-ref-id": "24eb3395-6c59-443a-9048-4c0b02ce1471",
              "description": "gbp-endpoint",
              "node-connector-id": "vethl-h37-2",
              "port": "0121a445-2e57-41d6-9894-9910d8fc1c23"
            }
          ]
        }
      }
    ]
  }
}
