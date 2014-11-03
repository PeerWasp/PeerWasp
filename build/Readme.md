# Build Windows Installer

## Requirements
- build script is taylored to Windows x64 
- [Inno Setup 5] (http://www.jrsoftware.org/isinfo.php): install the QuickStart Pack
- [ant] (https://ant.apache.org/): extract and include /bin folder in PATH variable
- [maven] (https://maven.apache.org/): extract and include /bin folder in PATH variable
- [maven ant tasks] (http://maven.apache.org/ant-tasks/): copy jar into the /lib folder of ant
- [launch4j] (http://launch4j.sourceforge.net/): install launch4j
- Visual Studio environment with Visual C++ 2013 compiler

## Build installers
- ```cd``` into the ```build``` folder
- run ```ànt windows_package_all``` (or ```ànt windows_package_x86``` / ```ànt windows_package_x64```)

The build script builds the PeerBox jar package, copies dependencies, compiles the shell extension and creates an exe launcher for the peerbox jar. Finally, everything is wrapped in an installer.

## Property files
- Paths to helper applications (e.g. Visual Studio) are specified in *.properties files (probably not required to adapt if default location is used)

## TODO
- make launch4j.xml config a template where version and jar name is inserted dynamically (see win_installer*.iss config template)
