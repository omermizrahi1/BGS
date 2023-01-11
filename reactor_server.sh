#!/bin/bash

echo "Enter port address:"
read port

echo "Enter number of threads:"
read threads

cd Server

mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="$port $threads"
