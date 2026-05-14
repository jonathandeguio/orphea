#!/usr/bin/env bash

# JM 29/09/2021 - Initial hack of Kuber start script for GKE.

#
git --version 2>&1 >/dev/null # improvement by moi
GIT_IS_AVAILABLE=$?
# ...
if [ $GIT_IS_AVAILABLE -ne 0 ]; then
  echo "Error:  git client is not installed; it is essential."
  exit
fi

# login to docker registry
# You hqve to login on gcloud
PROJECT_ID=orphea-bnp
COMPUTE_REGION=europe-west1
COMPUTE_ZONE=europe-west1-b
CLUSTER_NAME=orphea


login()
{

# connect first
gcloud auth login
gcloud auth configure-docker ${COMPUTE_REGION}-docker.pkg.dev
gcloud container clusters get-credentials $CLUSTER_NAME --zone $COMPUTE_ZONE --project $PROJECT_ID
}

# create repo directory
if [ ! -d repos ];  then
  mkdir repos
fi

cd repos


frontend() 
{
  if [ ! -d frontend ]; then
  git clone git@github.com:Orphea-io/frontend.git  > /dev/null
  else
  cd frontend
  git pull  > /dev/null
  # git checkout 19a0418cf524bc5a02fbe7b492549e403f9f5f1a
  cd ..
  fi
  docker build frontend --tag ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/orphea-cr/frontend
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/orphea-cr/frontend

  # restart deployment to pick new image
  kubectl -n orphea scale deploy frontend --replicas=0
  kubectl -n orphea scale deploy frontend --replicas=1
}


parler() 
{
  if [ ! -d parler ]; then
  git clone git@github.com:Orphea-io/parler.git  > /dev/null
  else
  cd parler
  git pull  > /dev/null
  cd ..
  fi
  docker build parler --tag ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/orphea-cr/parler
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/orphea-cr/parler

  # restart deployment to pick new image
  kubectl -n orphea scale deploy parler --replicas=0
  kubectl -n orphea scale deploy parler --replicas=1
}


boson()
{
  
  if [ ! -d boson ]; then
    git clone git@github.com:Orphea-io/boson.git  > /dev/null
  else
    cd boson
    git pull  > /dev/null
    
    cd ..
  fi

  docker build boson --tag ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/orphea-cr/boson
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/orphea-cr/boson

  # restart deployment to pick new image
  kubectl -n orphea scale deploy boson --replicas=0
  kubectl -n orphea scale deploy boson --replicas=1

}


usage()
{
  echo "$0 frontend|backend|julia|login|all"
}



opt="$1";

case "$opt" in
    "frontend")
        login
        frontend
        ;;
    "boson")
        boson
        ;;
    "parler")
        parler
        ;;
    "login")
        login
        ;;
    "all")
        login
        frontend
        boson
        parler
        ;;
    *)
        usage; 
        exit 1;
        ;;
esac


cd ..
git pull > /dev/null

