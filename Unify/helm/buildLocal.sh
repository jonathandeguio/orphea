#!/usr/bin/env bash

# JM 29/09/2021 - Initial hack of Kuber start script for GKE.

COMPUTE_REGION=europe-west2
PROJECT_ID=aprbos23

# get environment variables
if [ $# -eq 0 ]
  then
    echo "No arguments supplied."
fi

#
git --version 2>&1 >/dev/null # improvement by moi
GIT_IS_AVAILABLE=$?
# ...
if [ $GIT_IS_AVAILABLE -ne 0 ]; then
  echo "Error:  git client is not installed; it is essential."
  exit
fi



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
  # git checkout e9fdca97236703f0250f103b9c5e59f266771a96
  cd ..
  fi
  docker build frontend --tag ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/orphea-cr/frontend

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

  # restart deployment to pick new image
  kubectl -n orphea scale deploy boson --replicas=0
  kubectl -n orphea scale deploy boson --replicas=1

}


funnel()
{
  
  if [ ! -d funnel ]; then
    git clone git@github.com:Orphea-io/funnel.git  > /dev/null
  else
    cd funnel
    git pull  > /dev/null
    
    cd ..
  fi

  docker build funnel --tag ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/orphea-cr/funnel

}

callisto()
{
  
  if [ ! -d callisto ]; then
    git clone git@github.com:Orphea-io/callisto.git  > /dev/null
  else
    cd callisto
    git pull  > /dev/null
    
    cd ..
  fi

  docker build callisto --tag ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/orphea-cr/callisto

  # restart deployment to pick new image
  kubectl -n orphea scale deploy callisto --replicas=0
  kubectl -n orphea scale deploy callisto --replicas=1

}


all()
{
  #ACTIVE_REPOS="funnel parler spark-history-server boson callisto frontend julia orphea-docs shyne"
  ACTIVE_REPOS="frontend"

  for image in $(echo $ACTIVE_REPOS); do
     echo "`date` : Building $image"

     if [ ! -d $image ]; then
       git clone git@github.com:Orphea-io/$image  > /dev/null
     else 
       cd $image 
       git pull  > /dev/null
    
       cd ..
     fi
  
     docker build $image --tag ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/orphea-cr/$image
  
     # restart deployment to pick new image
     kubectl -n orphea scale deploy callisto --replicas=0 > /dev/null
     kubectl -n orphea scale deploy callisto --replicas=1 > /dev/null
     echo "`date` : Built $image"
  done
}

usage()
{
  echo "$0 frontend|boson|julia|funnel|callisto|tycho|login|all"
}



opt="$1";

case "$opt" in
    "frontend")
        #login
        frontend
        ;;
    "boson")
        boson
        ;;
    "funnel")
        funnel
        ;;
    "callisto")
        callisto
        ;;                
    "parler")
        parler
        ;;
    "all")
        all
        ;;
    *)
        usage; 
        exit 1;
        ;;
esac


cd ..
git pull > /dev/null

