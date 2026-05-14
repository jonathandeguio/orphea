az group create --name OrpheaStaging --location uksouth
az acr create --resource-group OrpheaStaging --name orpheastagingacr --sku Basic
az aks create -g OrpheaStaging -n OrpheaStagingAKS --location uksouth --attach-acr orpheastagingacr --generate-ssh-keys

az aks get-credentials --resource-group OrpheaStaging --name OrpheaStagingAKS
