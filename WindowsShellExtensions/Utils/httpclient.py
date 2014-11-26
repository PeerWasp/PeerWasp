import requests, json

# Base url -- change port if necessary
api_port = 30000
api_url = 'http://localhost:%d' % api_port

# API call for file versions
versions = '/contextmenu/versions'

# command to send
data = json.dumps({'path':'/home/albrecht/PeerBox_andreas/testfile.txt'}) 

cmd = versions

# issue request
headers = {'content-type': 'application/json'}
r = requests.post(api_url + cmd, data, headers=headers)

print r.text