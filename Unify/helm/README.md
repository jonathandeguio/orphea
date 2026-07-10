# MoveToData setup with HELM

## Create new tenant first
[Create new tenant](README-Create-New-Tenant.md)

## Setup environment 

copy env.sh to env-${PROJECT_ID}.sh modify env-${PROJECT_ID}.sh as per project id etc, make sure you are in helm directory

```
source env-${PROJECT_ID}.sh
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



# Below is done automatically part of create gke cluster script
## Make sure you create postgres db in alloyDB or somewhere before next step

## HELM install

```
helm install movetodata movetodata-gke -f charts/movetodata-gke/testCluster.yaml
```

## To Upgrade, if any changes to values etc

```
helm upgrade movetodata movetodata-gke -f charts/movetodata-gke/devCluster.yaml
```

## run post helm scripts for spark and healthcheck etc

```
./post-helm.sh env-${PROJECT_ID}.sh
```

## to tear down and remove

```
./teardown.sh
```


