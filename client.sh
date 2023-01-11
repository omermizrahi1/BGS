#!/bin/bash

echo "Enter server IP address:"
read ip

echo "Enter server port:"
read port

cd Client
./bin/BGSClient $ip $port
