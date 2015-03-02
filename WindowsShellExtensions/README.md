ShellExtension
==============

# Context Menu Shell Extension for PeerWasp

## Registry Entries
### Register Extension
To register the extension:

```regsvr32 /s ContextMenu.dll```

To unregister the extension: 

```regsvr32 /s /u ContextMenu.dll```

### Root path
The extension queries the Windows registry for the root path in order to decide whether it should be visible or not. The following key is required: 

```HKCU\Software\PeerWasp\rootpath ```

Thus, create and set it to an existing path (e.g. C:\PeerWasp) or edit and use the provided .reg file:

```regedit .\Utils\registry\rootpath.reg ```

### Server port
The communication between the extension and the Java application is based on simple HTTP POST requests with JSON payload. 
Thus, the extension has to know the port where the HTTP server is listening at runtime. The extension queries the Windows registry for the following key:

```HKCU\Software\PeerWasp\api_server_port ```

Thus, create and set it to a valid port (integer), e.g. 30000. Alternatively, use the provided .reg file: 

```regedit .\Utils\registry\api_server_port.reg ```

# Debugging
In order to debug the shell extension, register the extension in the registry and set the required values in the registry (root path, server port). Afterwards, launch the Windows Explorer and attach the debugger to the process ```explorer.exe``` (Visual Studio: Debug -> Attach to Process).


## Helper Utilities
### HTTP Server for Development

There is a simple python based HTTP server that can be used during development and for debugging purposes. It prints json requests to standard output. 
Run (python3 required):

```python.exe .\Utils\httpserver.py localhost 9999 ```

shutdown the server with: 

```CTRL+C ```

### HTTP Client for Development
The script ```.\Utils\httpclient.py``` contains code snippets to issue HTTP POST requests with JSON messages.
