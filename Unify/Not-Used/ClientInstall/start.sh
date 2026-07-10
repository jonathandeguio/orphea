#!/usr/bin/env bash

PROJECT_ID=movetodata-bnp
COMPUTE_REGION=europe-west1
COMPUTE_ZONE=europe-west1-b
CLUSTER_NAME=movetodata

# connect first
#the following auth login is only needed the first time you authorize the gloud utility. 
#gcloud auth login
gcloud container clusters get-credentials $CLUSTER_NAME --zone $COMPUTE_ZONE --project $PROJECT_ID

# Now we configure kubernetes
kubectl create -f configurations/movetodata-namespace.yaml


# add docker registry creds to kubernetes of not already there
#kubectl get secret|grep regcred > /dev/null
kubectl get secret regcred --namespace movetodata | grep regcred >/dev/null
if [ $? != 0 ]; then
    kubectl -n movetodata create secret docker-registry regcred --docker-server=https://index.docker.io/v1/ --docker-username=rmalik --docker-password=Givemeaccess1# --docker-email=rakesh@rkmalik.co.uk
fi

kubectl apply -f configurations/movetodata-secrets.yaml

kubectl apply -f configurations/movetodata-backend-config.yaml

kubectl apply -f configurations/boson-minio-storage.yaml 

kubectl apply -f configurations/boson-db-deployment.yaml


# Go in sleep loop until db started
echo -n "waiting for database to start"
while true
do
  kubectl -n movetodata get pods |grep boson-db|grep Running > /dev/null
  if [ $? == 0 ]; then
    echo "|"
    sleep 5
    # kubectl exec -it boson-db-5dd84575cf-z6l2j -- bin/bash -c "su - postgres -c \"psql -c 'DROP DATABASE boson;'\""
    kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep boson-db` -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE boson;'\""
    kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep boson-db` -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE kepler;'\""
    break
  fi
  echo -n "."
  sleep 5
done

#create dataset bucket in minio
kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep minio` -- /bin/bash -c "mkdir -p /storage/datasets"
kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep minio` -- /bin/bash -c "mkdir -p /storage/spark-streaming/checkpoint"

kubectl apply -f configurations/boson-app-deployment.yaml

# Go in sleep loop until boson started
echo -n "waiting for backend/boson to start"
while true
do
  kubectl -n movetodata get pods |grep -v boson-db|grep boson|grep Running > /dev/null
  if [ $? == 0 ]; then
    echo "|"
    break
  fi
  echo -n "."
  sleep 5
done

kubectl apply -f configurations/frontend-deployment.yaml
kubectl apply -f configurations/parler-deployment.yaml

# if ! gcloud compute addresses list | grep -q "$CLUSTER_NAME" ; then
#   echo "External static IP for $CLUSTER_NAME not found!"
#   exit
# fi

kubectl apply -f configurations/movetodata-ingress.yaml

## Install Spark and Spark_history
./movetodata_spark_env.sh create
kubectl apply -f configurations/movetodata-spark-history-server.yaml
#
## Install Tycho/Superset
# Now obsolete
#( cd tycho-helm && ./install_helm_superset.sh ) && cd -

## Go in sleep loop until ingress started
#echo -n "waiting for ingress to start"
#while true
#do
#  kubectl -n movetodata describe ingress|grep Address|grep -oE "\b([0-9]{1,3}\.){3}[0-9]{1,3}\b" > /dev/null
#  if [ $? == 0 ]; then
#    echo "|"
#    if [ `uname -s` == "Darwin" ]; then
#      open -n -a "Google Chrome" --args "--new-tab" "http://`minikube ip`"
#      open -n -a "Google Chrome" --args "--new-tab" "http://`minikube ip`/swagger-ui.html"
#    else
#      start "http://`minikube ip`"
#      start "http://`minikube ip`/swagger-ui.html"
#    fi
#    break
#  fi
#  echo -n "."
#  sleep 5
#done
#
#echo ""
#echo ""
#echo "+--------------------------------------------------------------------------+"
#echo "|                           Say hello to MoveToData                            |"
#echo "+--------------------------------------------------------------------------+"
#echo "|                                                                          |"
#echo "|                                                                          |"
#echo -e "|   Here is the minikube IP : `minikube ip`                            \t   |"
#echo "|                                                                          |"
#echo "|   Connect to MoveToData  : http://`minikube ip`                               |"
#echo -e "|   Connect to Swagger : http://`minikube ip`/swagger-ui.html          \t   |"
#echo "|                                                                          |"
#echo "+--------------------------------------------------------------------------+"
#echo ""
