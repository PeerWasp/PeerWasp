ShellExtension
==============

Shell Extension for PeerBox

To register the extension:

```regsvr32 /s ContextMenu.dll```

To unregister the extension: 

```regsvr32 /s /u ContextMenu.dll```

Extension queries windows registry for a root path:
create the following key: 

```HKCU\Software\PeerBox\rootpath ```

and set it to an existing path (e.g. C:\PeerBox) or edit and use the provided .reg file:

```regedit .\Utils\peerbox_rootpath.reg```

There is a simple http server that can be used for debugging purposes:

```python.exe .\Utils\httpserver.py localhost 9999```

shutdown the server with: 

```CTRL+C ```
