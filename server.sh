#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Usage: ./server.sh  <port>"
    exit 1
fi

java Server $1 

