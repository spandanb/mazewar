#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: ./run.sh <server host> <server port>"
    exit 1
fi

java Mazewar $1 $2 

