import http.server
import socketserver
import sys
import json





class HTTPHandler(http.server.BaseHTTPRequestHandler):

    def print_info(self):
        print("Command: %s" % self.command)
        print("Path: %s" % self.path)
        print("Headers:")
        for i in self.headers.items(): print("\t%s" % str(i))
        
    def do_HEAD(self):
        self.send_response(200)
        self.send_header("Content-type", "text/plain")
        self.end_headers()
        
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type", "text/plain")
        self.end_headers()
        
        self.print_info()
        
                          
    def do_POST(self):
        self.send_response(200)
        self.send_header("Content-type", "text/plain")
        self.end_headers()
        
        self.print_info()
        
        data = self.rfile.read(int(self.headers.get('content-length')))
        dataStr = data.decode("utf-8")
        if 'application/json' in self.headers.get('content-type'):
            prettyjson = json.dumps(json.loads(dataStr), sort_keys=False, indent=4, separators=(',', ': '))
            print(prettyjson)
        else:
            print(data)
  
        
    
if __name__ == "__main__":
    if(len(sys.argv) < 3):
        print("Prameters: host port, e.g. localhost 1234")
        sys.exit()
        
    HOST = sys.argv[1]
    PORT = int(sys.argv[2])
    print("Host: %s, Port: %d" % (HOST, PORT))

    httpd = socketserver.TCPServer((HOST, PORT), HTTPHandler)

    print("serving....")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("Shutdown")
        httpd.shutdown()