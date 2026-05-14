#!/usr/bin/env bash

#
git --version 2>&1 >/dev/null # improvement by moi
GIT_IS_AVAILABLE=$?
# ...
if [ $GIT_IS_AVAILABLE -ne 0 ]; then
  echo "Error:  git client is not installed; it is essential."
  exit
fi

# check if docker is runnnig docker
docker ps > /dev/null
if [ $? != 0 ]; then
    echo "Error : docker is not running!"
    exit 3
fi

# login to docker registry
docker login -u rmalik -p Givemeaccess1#


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
    cd ..
  fi


  docker build frontend --tag rmalik/orphea:frontend
  docker push rmalik/orphea:frontend

  # restart deployment to pick new image
  kubectl -n orphea scale deploy frontend --replicas=0
  kubectl -n orphea scale deploy frontend --replicas=1
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

  docker build boson --tag rmalik/orphea:boson
  docker push rmalik/orphea:boson

  # restart deployment to pick new image
  kubectl -n orphea scale deploy boson --replicas=0
  kubectl -n orphea scale deploy boson --replicas=1
}

# Julia : git server
julia()
{
  if [ ! -d julia ]; then
  git clone git@github.com:Orphea-io/julia.git  > /dev/null
  else
    cd julia
    git pull  > /dev/null
    
    cd ..
  fi

  docker build julia --tag rmalik/orphea:julia
  docker push rmalik/orphea:julia
}


usage()
{
  echo "$0 frontend|backend|julia|login|all"
}


opt="$1";

case "$opt" in
    "frontend")     # alternate format: --first=date
        frontend
        ;;
    "boson")
        boson
        ;;
    "julia")
        julia
        ;;
    "all")
        frontend
        boson
        julia
        ;;
    *)
        usage; 
        exit 1;
        ;;
esac

