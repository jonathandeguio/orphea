# Bosker setup with HELM

## Create new tenant first
[Create new tenant](README-Create-New-Tenant.md)

## Setup environment 

modify env.sh as per project id etc

```
source env.sh
```

## Login 

```
./build.sh login
```

This will open browser, authenticate with appropriate account

## Create cluster

```
./create_gke_cluster.sh
```

This will create

cluster
repos


## Make sure you create postgres db in alloyDB or somewhere before next step

## HELM install

```
helm install movetodata movetodata-gke -f movetodata-gke/testCluster.yaml
```

## To Upgrade, if any changes to values etc

```
helm upgrade movetodata movetodata-gke -f movetodata-gke/testCluster.yaml
```

## run post helm scripts for spark and healthcheck etc

```
./post-helm.sh
```

# Create database otherwise boson start will fail - only needed if db not created

```
create postgres db in alloy db, it will give an IP. put this IP in values.yaml

➜  helm git:(main) ✗ kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep boson-db` -- /bin/bash 
bash-5.1# 
bash-5.1# 
bash-5.1# 
bash-5.1# su - postgres
boson-db-5946d5dc6d-8snn9:~$ psql -h 10.91.96.2 -U postgres  # <-- replace IP with db IP
Password for user postgres: 
psql (11.17, server 14.4)
WARNING: psql major version 11, server major version 14.
         Some psql features might not work.
SSL connection (protocol: TLSv1.3, cipher: TLS_AES_256_GCM_SHA384, bits: 256, compression: off)
Type "help" for help.

postgres=> create database boson
postgres-> ;
CREATE DATABASE
postgres=> 

```


## to tear down and remove

```
./teardown.sh
```