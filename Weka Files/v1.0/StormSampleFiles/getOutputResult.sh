#!/bin/bash
if [ $# -ne 1 ]
then
        echo Usage: 1 parameters
        exit 1 
else
        workersName=$1
fi

for dictionary in ./*
do
        if [ -d "$dictionary" ] && [ "$dictionary" != "./sampleOutput" ] && [ "$dictionary" != "./processed" ]
        then
                java -jar getSample.jar "$dictionary" "$workersName"
        fi
done
