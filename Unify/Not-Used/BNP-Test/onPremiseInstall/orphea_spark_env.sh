# to Build

PROJECT_ID=orphea-bnp
COMPUTE_REGION=europe-west1
COMPUTE_ZONE=europe-west1-b
CLUSTER_NAME=orphea
LOCALREPO=orphea-cr

build_image()
{
  docker build . --tag ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/${LOCALREPO}/funnel --build-arg CACHEBUST=$(date +%s)
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/${LOCALREPO}/funnel
}

# to Delete

delete_spark()
{
    sudo kubectl -n orphea delete SparkApplications orphea-spark

    sudo helm uninstall orphea --namespace orphea

    sudo kubectl -n orphea delete clusterrolebinding spark-role
    sudo kubectl -n orphea delete serviceaccounts orphea-spark-operator
    sudo kubectl -n orphea delete serviceaccounts spark
}

# to Create

create_spark()
{

    sudo helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator

    sudo cd spark-charts/spark-operator-chart
    sudo helm install -f values.yaml orphea spark-operator/spark-operator --namespace orphea

    sudo kubectl -n orphea create serviceaccount spark
    sudo kubectl create clusterrolebinding spark-role --clusterrole=cluster-admin --user=system:serviceaccount:orphea:spark
    # below is run build from cluster
    sudo kubectl create clusterrolebinding spark-role-default --clusterrole=cluster-admin --user=system:serviceaccount:orphea:default

}

# tests

test()
{
    sudo kubectl -n orphea get SparkApplications
    sudo kubectl -n orphea get pods
    sudo kubectl -n orphea logs orphea-spark-driver
}

purge()
{
 
  sudo kubectl -n orphea get pods|grep driver|awk -F "-driver" '{print $1}'|while read line; do kubectl -n orphea delete sparkapplication $line; done

  sudo kubectl -n orphea get SparkApplications|grep orphea|awk '{print $1}'|while read line
  do
    sudo kubectl -n orphea delete SparkApplication $line
  done
}


usage()
{
  echo "$0 create|delete|test|all"
}

opt="$1";

case "$opt" in
    "build")
        build_image
        ;;
    "create")
        create_spark
        ;;
    "delete")
        delete_spark
        ;;
    "purge")
        purge
        ;;
    "test")
        test
        ;;
    "all")
        delete_spark
        create_spark
        test
        ;;
    *)
        usage; 
        exit 1;
        ;;
esac
