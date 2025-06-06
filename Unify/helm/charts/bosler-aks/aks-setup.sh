az group create --name BoslerStaging --location uksouth
az acr create --resource-group BoslerStaging --name boslerstagingacr --sku Basic
az aks create -g BoslerStaging -n BoslerStagingAKS --location uksouth --attach-acr boslerstagingacr --generate-ssh-keys

az aks get-credentials --resource-group BoslerStaging --name BoslerStagingAKS
