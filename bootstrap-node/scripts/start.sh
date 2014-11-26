#!/bin/bash

nohup java -Djava.net.preferIPv4Stack=true -jar bootstrap-node-0.0.1-SNAPSHOT.jar $1 > /dev/null &
PID=$!
echo $PID > pid.txt

echo "Running PID $PID"

