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

# start minikube if not running
minikube status|grep Running > /dev/null
if [ $? != 0 ]; then

  if [ `uname -s` == "Darwin" ]; then
    minikube start --extra-config=apiserver.service-node-port-range=1-65535 --vm=true
  else
    minikube start --extra-config=apiserver.service-node-port-range=1-65535
  fi
  minikube addons enable ingress
fi


# # create repo directory
# if [ ! -d repos ];  then
#   mkdir repos
# fi

# cd repos

# if [ ! -d frontend ]; then
#   git clone git@github.com:Bosler-io/frontend.git  > /dev/null
# else
#   cd frontend
#   git pull  > /dev/null
#   cd ..
# fi


# docker build frontend --tag rmalik/bosler:frontend
# docker push rmalik/bosler:frontend

# if [ ! -d boson ]; then
#   git clone git@github.com:Bosler-io/boson.git  > /dev/null
# else
#   cd boson
#   git pull  > /dev/null
  
#   cd ..
# fi

# docker build boson --tag rmalik/bosler:boson
# docker push rmalik/bosler:boson


# if [ ! -d julia ]; then
#   git clone git@github.com:Bosler-io/julia.git  > /dev/null
# else
#   cd julia
#   git pull  > /dev/null
  
#   cd ..
# fi

# cd ..

# docker build julia --tag rmalik/bosler:julia
# docker push rmalik/bosler:julia


git pull > /dev/null


# minikube start --mount-string="$HOME/bosler:/bosler"

# Now we configure kubernetes
kubectl create -f configurations/bosler-namespace.yaml

# add docker registry creds to kubernetes of not already there
kubectl get secret|grep regcred > /dev/null
if [ $? != 0 ]; then
    kubectl -n bosler create secret docker-registry regcred --docker-server=https://index.docker.io/v1/ --docker-username=rmalik --docker-password=Givemeaccess1# --docker-email=rakesh@rkmalik.co.uk
fi

kubectl apply -f configurations/bosler-secrets.yaml

kubectl apply -f configurations/boson-minio-storage.yaml 

kubectl apply -f configurations/julia-deployment.yaml 

kubectl apply -f configurations/boson-db-deployment.yaml 


# Go in sleep loop until db started
echo -n "waiting for database to start"
while true
do
  kubectl -n bosler get pods |grep boson-db|grep Running > /dev/null
  if [ $? == 0 ]; then
    echo "|"
    sleep 5
    # kubectl exec -it boson-db-5dd84575cf-z6l2j -- bin/bash -c "su - postgres -c \"psql -c 'DROP DATABASE boson;'\""
    kubectl -n bosler exec -it `kubectl -n bosler get pods |awk '{print $1}'|grep boson-db` -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE boson;'\""
    break
  fi
  echo -n "."
  sleep 5
done

#create dataset bucket in minio
kubectl -n bosler exec -it `kubectl -n bosler get pods |awk '{print $1}'|grep minio` -- /bin/bash -c "mkdir /storage/datasets"

kubectl apply -f configurations/boson-deployment.yaml

# Go in sleep loop until boson started
echo -n "waiting for backend/boson to start"
while true
do
  kubectl -n bosler get pods |grep -v boson-db|grep boson|grep Running > /dev/null
  if [ $? == 0 ]; then
    echo "|"
    break
  fi
  echo -n "."
  sleep 5
done

kubectl apply -f configurations/frontend-deployment.yaml

kubectl apply -f configurations/bosler-ingress.yaml 


# Go in sleep loop until ingress started
echo -n "waiting for ingress to start"
while true
do
  kubectl -n bosler describe ingress|grep Address|grep -oE "\b([0-9]{1,3}\.){3}[0-9]{1,3}\b" > /dev/null
  if [ $? == 0 ]; then
    echo "|"
    if [ `uname -s` == "Darwin" ]; then
      open -n -a "Google Chrome" --args "--new-tab" "http://`minikube ip`"
      open -n -a "Google Chrome" --args "--new-tab" "http://`minikube ip`/swagger-ui.html"
    else
      start "http://`minikube ip`"
      start "http://`minikube ip`/swagger-ui.html"
    fi
    break
  fi
  echo -n "."
  sleep 5
done

echo ""
echo ""
echo "+--------------------------------------------------------------------------+"
echo "|                           Say hello to Bosler                            |"
echo "+--------------------------------------------------------------------------+"
echo "|                                                                          |"
echo "|                                                                          |"
echo -e "|   Here is the minikube IP : `minikube ip`                            \t   |"
echo "|                                                                          |"
echo "|   Connect to Bosler  : http://`minikube ip`                               |"
echo -e "|   Connect to Swagger : http://`minikube ip`/swagger-ui.html          \t   |"
echo "|                                                                          |"
echo "+--------------------------------------------------------------------------+"
echo ""


