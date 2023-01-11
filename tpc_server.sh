#!/bin/bash

echo "Enter port:"
read port

cd Server

mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args="$port"
