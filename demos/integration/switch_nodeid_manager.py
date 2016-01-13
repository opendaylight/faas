import util
import json
import inputsCommon
import constants

#
# Function Implimentations
#

class SwitchNodeIdManager():
    def __init__(self, **params):
        self.mapping = {}

    def getNodeIdByName(self, switchName):
        if (self.mapping):
            return self.mapping[switchName]

        self.loadSwitchNodes()

        if self.mapping.has_key(switchName):
            return self.mapping[switchName]
        else:
            assert False, "the switch has not been registed to ODL. [%s]" % switchName
            

    def loadSwitchNodes(self):
        url = 'http://%s/restconf/operational/network-topology:network-topology/topology/ovsdb:1' % inputsCommon.odlIpAddr_gc

        resp = util.requestGET(url)
        if resp == constants.ERROR_STR:
            print "ERROR: loadSwitchNodes() failed"
            return None

        print resp

        jsondata=json.loads(resp)["topology"]
        for topo_item in jsondata:
            if topo_item["node"] is not None:
                for ovsdb_node in topo_item["node"]:
                    if ovsdb_node.has_key("ovsdb:datapath-id"): #it's a bridge node
                        self.mapping[ovsdb_node["ovsdb:bridge-name"]] = ovsdb_node["node-id"]

