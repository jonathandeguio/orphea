!/usr/bin/env bash


docker ps --format '{{ .Names }}' | while read line
do
    docker stop $line
done