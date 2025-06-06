#!/usr/bin/env bash


kubectl delete all --all -n bosler

kubectl delete pv `kubectl get pv|grep -v NAME|awk '{print $1}'`


