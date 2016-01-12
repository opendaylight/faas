import inputsCommon

#===============================================================================#
def get_acl_uri():
    return "http://"+inputsCommon.odlIpAddr_gc+"/restconf/config/ietf-access-control-list:access-lists/"

def get_acl_data():
    return {
    "ietf-access-control-list:access-lists": {
        "acl": [
            {
            "acl-name":"acl-udp-permit",
            "acl-type":"",
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
            }
            ]
        }
    }


def get_acl_data2():
    return {
    "ietf-access-control-list:access-lists": {
        "acl": [
            {
            "acl-name":"acl-icmp-deny",
            "acl-type":"",
            "access-list-entries":{
                "ace" :[
                    {
                    "rule-name":"rule-icmp-deny",
                    "matches":{
                        "destination-ipv4-network":"172.16.1.0/24",
                        "source-ipv4-network":"172.16.2.0/24",
                        "protocol":1
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

def get_acl_data3():
    return {
    "ietf-access-control-list:access-lists": {
        "acl": [
            {
            "acl-name":"acl-udp-permit",
            "acl-type":"",
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
            "acl-type":"",
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
            "acl-type":"",
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
