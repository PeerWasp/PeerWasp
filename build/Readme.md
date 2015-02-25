# Packaging and Windows Installer

## Requirements
- Note: build script is taylored to Windows x64 
- [Inno Setup 5] (http://www.jrsoftware.org/isinfo.php): install the *QuickStart Pack*.
- [ant] (https://ant.apache.org/): extract and include /bin folder in PATH variable.
- [maven] (https://maven.apache.org/): extract and include /bin folder in PATH variable.
- [maven ant tasks] (http://maven.apache.org/ant-tasks/): copy jar into the /lib folder of ant.
- [launch4j] (http://launch4j.sourceforge.net/): install the Launch4j Executable Wrapper.
- [Visual C++] (http://www.visualstudio.com/): Microsoft Visual Studio environment with Visual C++ 2013 compiler.

## Build Windows installer
Creating the installer works as follows. In general, the ant build script is responsible for the coordination of all subtasks. First, the Java code is compiled and a jar package is created (using *maven*). Furthermore, all Java dependencies are copied into an external folder (lib/).
Second, the shell extension is compiled (using the *Visual C++ Compiler*) and packaged in a DLL. Two versions are created: x86 and x64 DLL. The DLLs and their dependencies are copied as well.
Next, an exe launcher for the Java application respectively the jar is created (using *launch4j*). The launcher is responsible for launching the Java VM with the correct parameter. 
Finally, everything is wrapped in an installer (using *Inno Setup*).

### Steps
#### Property files and preparations
- Open ```App.properties``` and set the property ```app.version``` to the current version (same as in the ```pom.xml``` file). It should have the form ```x.x.x``` (integers only).
- Paths to helper applications (e.g. Visual Studio) are specified in *.properties files. It is probably not required to adapt these files if default locations are used (such as C:\Program Files\\...)
 
#### Build
- ```cd``` into the ```build``` folder.
- run ```ant win_package``` in order to package and create an installer.
- Note: the installer works with Windows x64 and x86 and contains all files required to run on either platform. Hence, packaging must be done on a Windows x64 machine due to 64-bit compilation.




## Development
### Snapshots
To build an installer using the current ```x.x.x-SNAPSHOT``` version, temporarily set the property ```app.jarname``` in ```App.properties``` to the packaged jar.

### H2H Packaging
If snapshot versions are required (e.g. during development or testing), the H2H processframework and core must be installed in the local maven repository. This is only required during development. 

H2H processframework:
```
cd into process framework folder
mvn install -DskipTests -Dmaven.javadoc.skip=true
```

H2H core:
```
cd into Hive2Hive folder
mvn install -DskipTests -Dmaven.javadoc.skip=true
```

## Open Issues and TODOs: 
...
