import time
import datetime
import urllib2
from urllib2 import Request, urlopen, URLError, HTTPError
import constants
import json
import sys

#
# macros and constants
#
SUPER_URL = 'http://127.0.0.1:8181/restconf'
USER_NAME = 'admin'
USER_PASSWD = 'admin'
REST_REQ_HEADERS = { 'Content-Type': 'application/yang.data+json',
                     'Cache-Control': 'no-cache'}

#
# global variables
#

#
# Function implementations
#

#===============================================================================# 
def setupAuthentication():

  passman = urllib2.HTTPPasswordMgrWithDefaultRealm()

  # Because we have put None at the start it will always use this username/password 
  # combination for  urls for which SUPER_URL is a super-url
  passman.add_password(None, SUPER_URL, USER_NAME, USER_PASSWD)

  authhandler = urllib2.HTTPBasicAuthHandler(passman)
  opener = urllib2.build_opener(authhandler)
  urllib2.install_opener(opener)

  return constants.OK_STR


#===============================================================================# 
def requestGET(url):

  # Send the GET request
  req = urllib2.Request(url, None, REST_REQ_HEADERS)
  req.get_method = lambda: 'GET'
  response = ''

  try:
    response = urllib2.urlopen(req)  
  except HTTPError as e:
    print 'The server couldn\'t fulfill the request.'
    print 'Error code: ', e.code
    return constants.ERROR_STR
  except URLError as e:
    print 'We failed to reach a server.'
    print 'Reason: ', e.reason
    return constants.ERROR_STR
  else:
	  # Read the response
    return response.read()

#===============================================================================# 
def requestDELETE(url):

  # Send the DELETE request
  req = urllib2.Request(url, None, REST_REQ_HEADERS)
  req.get_method = lambda: 'DELETE'
  response = ''

  try:
    response = urllib2.urlopen(req)
  except HTTPError as e:
    print 'The server couldn\'t fulfill the request.'
    print 'Error code: ', e.code
    return constants.ERROR_STR
  except URLError as e:
    print 'We failed to reach a server.'
    print 'Reason: ', e.reason
    return constants.ERROR_STR
  else:
          # Read the response
    return response.read()

#===============================================================================# 
def requestPOST(url, data):

  # Send the POST request
  req = urllib2.Request(url, data, REST_REQ_HEADERS)
  req.get_method = lambda: 'POST'
  response = ''

  try:
    response = urllib2.urlopen(req)  
  except HTTPError as e:
    print 'The server couldn\'t fulfill the request.'
    print 'Error code: ', e.code
    return constants.ERROR_STR
  except URLError as e:
    print 'We failed to reach a server.'
    print 'Reason: ', e.reason
    return constants.ERROR_STR
  else:
	  # Read the response
    return response.read()


#===============================================================================# 
def requestPUT(url, data):

  res = constants.ERROR_STR
  response = ''

  # Send the PUT request
  req = urllib2.Request(url, data, REST_REQ_HEADERS)
  req.get_method = lambda: 'PUT'

  try:
    response = urllib2.urlopen(req)  
  except HTTPError as e:
    print 'The server couldn\'t fulfill the request.'
    print 'Error code: ', e.code
    res = constants.ERROR_STR
  except URLError as e:
    print 'We failed to reach a server.'
    print 'Reason: ', e.reason
    res = constants.ERROR_STR
  else:
	  # Read the response
    res = response.read()

  return res

#===============================================================================# 
def formatTime(seconds):

  timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y%m%d %H:%M:%S')

  return time.strftime('%H:%M:%S', time.gmtime(seconds))


#===============================================================================# 
def parseJson2():

  mail_accounts = []
  da = {}
  try:
    s = '[{"i":"imap.gmail.com","p":"aaaa"},{"i":"imap.aol.com","p":"bbbb"},{"i":"333imap.com","p":"ccccc"},{"i":"444ap.gmail.com","p":"ddddd"},{"i":"555imap.gmail.com","p":"eee"}]'
    jdata = json.loads(s)
    for d in jdata:
        for key, value in d.iteritems():
            if key not in da:
                da[key] = value
            else:
                da = {}
                da[key] = value
        mail_accounts.append(da)
  except Exception, err:
    sys.stderr.write('Exception Error: %s' % str(err))

def parseNetNodeConfig(data, tag):
  jdata = json.loads(data.decode('utf8'))
  #print json.dumps(jdata, sort_keys=True, indent=2, separators=(',', ': '))
  print json.dumps(jdata)

#===============================================================================# 
def printJson(data, tag):
  try:
    jdata = json.loads(data)
    #print jdata
    #print json.dumps(jdata, sort_keys=True, indent=2, separators=(',', ': '))
    for key, value in jdata.iteritems():
      #print '{k}: {v}'.format(k=key, v=value) 
      if key == 'output':
        parseNetNodeConfig(value['nodeConfig'], 'EVIFS')
  except Exception, err:
      sys.stderr.write('ERROR: printJson: %s\n' % str(err))

#===============================================================================#
def runRequestGET(url, callerName):
  resp = requestGET(url)

  if resp == constants.ERROR_STR:
    print "ERROR: ", callerName, " failed"
    return resp

  return resp

#===============================================================================#
def runRequestDELETE(url, callerName):
  resp = requestDELETE(url)

  if resp == constants.ERROR_STR:
    print "ERROR: ", callerName, " failed"
    return resp

  return resp


#===============================================================================#
def runRequestPUT(url, inputData, callerName):
  resp = requestPUT(url, inputData)

  if resp == constants.ERROR_STR:
    print "ERROR: ", callerName, " failed"
    return resp

  return resp

#===============================================================================# 
def runRequestPOST(url, inputData, callerName):
  resp = requestPOST(url, inputData)

  if resp == constants.ERROR_STR:
    print "ERROR: ", callerName, " failed"
    return resp

  return resp


