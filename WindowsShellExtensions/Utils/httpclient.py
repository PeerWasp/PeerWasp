import sys
import requests, json

# This script can be used to test context menu requests. It simply generates json
# messages and sends the request to the server.


# Base url -- change port if necessary
api_port = 30000
api_url = 'http://localhost:%d' % api_port
cmd_url = ''
data = ''


def request():
    global api_port, api_url, cmd_url, data
    
    # issue request
    request_url = api_url + cmd_url
    headers = {'content-type': 'application/json'}
    
    print '-- Request --'
    print 'URL: ', request_url
    print 'Payload: ', data
    print 'Header: ', headers
    
    r = requests.post(request_url, data, headers=headers)

    print '-- Response --'
    print r.text
    
def delete(paths):
    global api_port, api_url, cmd_url, data
    
    cmd_url = '/contextmenu/delete'
    data = json.dumps({
        'paths' : paths
    })
    
    
    request()

def versions(path):
    global api_port, api_url, cmd_url, data
    
    cmd_url = '/contextmenu/versions'
    data = json.dumps({
        'path' : path
    })
    
    request()

def share(path):
    global api_port, api_url, cmd_url, data
    
    cmd_url = '/contextmenu/share'
    data = json.dumps({
        'path' : path
    })
    
    request()

if __name__ == '__main__':
    if len(sys.argv) <= 1:
        print 'Please specify command as argument.'
        print '\tdelete [one or multiple files to delete]'
        print '\tversions [one file]'
        print '\tshare [one folder]'
        sys.exit(-1)
    
    cmd = sys.argv[1]
    
    if cmd == 'delete':
        if len(sys.argv) <= 2:
            print 'Please specify files (as arguments).'
            sys.exit(-1)
            
        delete(sys.argv[2:])
        
    elif cmd == 'versions':
        if len(sys.argv) <= 2:
            print 'Please specify file (as argument).'
            sys.exit(-1)
        elif len(sys.argv) >= 4:
            print 'Command versions works only with one file at a time.'
            sys.exit(-1)
            
        versions(sys.argv[2])
        
    elif cmd == 'share':
        if len(sys.argv) <= 2:
            print 'Please specify folder (as argument).'
            sys.exit(-1)
        elif len(sys.argv) >= 4:
            print 'Command share works only with one file at a time.'
            sys.exit(-1)
            
        share(sys.argv[2])
        
    else:
        print 'Command not recognized.'
        
