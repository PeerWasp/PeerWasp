##### connect to server:
```
ssh -i ~/.ssh/path_to_private_key uzh_peerbox@hostname
```

##### server: install java
```
su
yum install java-1.8.0-openjdk.x86_64
exit
```

##### server: create folder for node
```
mkdir peerbox
```

##### local: install most recent PeerBox, H2H version in local maven repo
```
mvn install -DskipTests
```

##### local: package the boostrap node project
```
mvn package -DskipTests
```

##### local: upload the bootstrap.txt file
```
scp bootstrapnodes.txt uzh_peerbox@hostname:~/peerbox/.
```

##### local: create archive and upload it 
```
cd target

tar -zcvf bootstrap.tar.gz bootstrap-node-0.0.1-SNAPSHOT.jar lib/

scp bootstrap.tar.gz  uzh_peerbox@hostname:~/peerbox/. 
```

##### server: extract archive
```
tar zxfv bootstrap.tar.gz
```

##### server: run node 
-Djava.net.preferIPv4Stack=true ensures that we listen on the IPv4 IP and not on IPv6 interfaces (which would be the default)
```
java -Djava.net.preferIPv4Stack=true -jar bootstrap-node-0.0.1-SNAPSHOT.jar
```


#### TODO
- update readme to final version
