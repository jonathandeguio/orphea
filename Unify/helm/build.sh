#!/usr/bin/env bash

# JM 29/09/2021 - Initial hack of Kuber start script for GKE.

# get environment variables
if [ $# -eq 0 ]
  then
    echo "No arguments supplied."
fi

source env-${PROJECT_ID}.sh

ACTIVE_ID=$(gcloud auth list --filter=status:ACTIVE --format="value(account)")
PROMPT_MSG="Active gcloud ID is $ACTIVE_ID and $PROJECT_ID. Do you wish to proceed? (Y|n) >  "
echo -n $PROMPT_MSG
read confirm

if [[ "$confirm" == [yY] || "$confirm" == [yY][eE][sS] ]]; then
    echo "Continuing"
else
    echo $confirm
    echo \n exiting...
    exit 27
    # return 1
fi

#
git --version 2>&1 >/dev/null # improvement by moi
GIT_IS_AVAILABLE=$?
# ...
if [ $GIT_IS_AVAILABLE -ne 0 ]; then
  echo "Error:  git client is not installed; it is essential."
  exit
fi


login()
{

# connect first
# gcloud config set project $PROJECT_ID
# gcloud config set compute/region $COMPUTE_REGION
# gcloud config set compute/zone $COMPUTE_ZONE

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
  git clone git@github.com:MoveToData-io/frontend.git  > /dev/null
  else
  cd frontend
  git pull  > /dev/null
  # git checkout e9fdca97236703f0250f103b9c5e59f266771a96
  cd ..
  fi
  docker build frontend --tag ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/frontend
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/frontend

  # restart deployment to pick new image
  kubectl -n movetodata scale deploy frontend --replicas=0
  kubectl -n movetodata scale deploy frontend --replicas=1
}


parler() 
{
  if [ ! -d parler ]; then
  git clone git@github.com:MoveToData-io/parler.git  > /dev/null
  else
  cd parler
  git pull  > /dev/null
  cd ..
  fi
  docker build parler --tag ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/parler
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/parler

  # restart deployment to pick new image
  kubectl -n movetodata scale deploy parler --replicas=0
  kubectl -n movetodata scale deploy parler --replicas=1
}


boson()
{
  
  if [ ! -d boson ]; then
    git clone git@github.com:MoveToData-io/boson.git  > /dev/null
  else
    cd boson
    git pull  > /dev/null
    
    cd ..
  fi

  docker build boson --tag ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/boson
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/boson

  # restart deployment to pick new image
  kubectl -n movetodata scale deploy boson --replicas=0
  kubectl -n movetodata scale deploy boson --replicas=1

}


funnel()
{
  
  if [ ! -d funnel ]; then
    git clone git@github.com:MoveToData-io/funnel.git  > /dev/null
  else
    cd funnel
    git pull  > /dev/null
    
    cd ..
  fi

  docker build funnel --tag ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/funnel
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/funnel

}

callisto()
{
  
  if [ ! -d callisto ]; then
    git clone git@github.com:MoveToData-io/callisto.git  > /dev/null
  else
    cd callisto
    git pull  > /dev/null
    
    cd ..
  fi

  docker build callisto --tag ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/callisto
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/callisto

  # restart deployment to pick new image
  kubectl -n movetodata scale deploy callisto --replicas=0
  kubectl -n movetodata scale deploy callisto --replicas=1

}

tycho()
{
  
  if [ ! -d tycho ]; then
    git clone git@github.com:MoveToData-io/tycho.git  > /dev/null
  else
    # rm -rf tycho
    # git clone git@github.com:MoveToData-io/tycho.git  > /dev/null
    cd tycho
    
    git pull  > /dev/null
    
    cd ..
  fi

  docker build tycho --tag ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/tycho
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/movetodata-cr/tycho

  cd tycho/helm/superset
  sed -e "s/movetodata-334213/$PROJECT_ID/g" values.yaml > new-values.yaml
  mv new-values.yaml values.yaml
  ./install_helm_superset.sh

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
    "tycho")
        tycho
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

