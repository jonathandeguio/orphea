az group create --name MoveToDataStaging --location uksouth
az acr create --resource-group MoveToDataStaging --name movetodatastagingacr --sku Basic
az aks create -g MoveToDataStaging -n MoveToDataStagingAKS --location uksouth --attach-acr movetodatastagingacr --generate-ssh-keys

az aks get-credentials --resource-group MoveToDataStaging --name MoveToDataStagingAKS
