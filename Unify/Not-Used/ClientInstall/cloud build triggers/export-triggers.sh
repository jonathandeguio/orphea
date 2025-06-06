#!/bin/bash
# List triggers:  gcloud beta builds triggers list

for i in $(gcloud beta builds triggers list | awk '/^name:/ {print $2}') ; do
     gcloud beta builds triggers export $i --destination="./${i}.yaml"
done

