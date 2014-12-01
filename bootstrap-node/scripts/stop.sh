#!/bin/bash

PID=$(cat pid.txt)
kill $PID

echo "Kill $PID"

