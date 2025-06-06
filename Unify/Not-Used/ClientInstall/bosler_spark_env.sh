# to Build

PROJECT_ID=bosler-bnp
COMPUTE_REGION=europe-west1
COMPUTE_ZONE=europe-west1-b
CLUSTER_NAME=bosler
LOCALREPO=bosler-cr

build_image()
{
  docker build . --tag ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/${LOCALREPO}/funnel --build-arg CACHEBUST=$(date +%s)
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/${LOCALREPO}/funnel
}

# to Delete

delete_spark()
{
    sudo kubectl -n bosler delete SparkApplications bosler-spark

    sudo helm uninstall bosler --namespace bosler

    sudo kubectl -n bosler delete clusterrolebinding spark-role
    sudo kubectl -n bosler delete serviceaccounts bosler-spark-operator
    sudo kubectl -n bosler delete serviceaccounts spark
}

# to Create

create_spark()
{

    sudo helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator

    sudo cd spark-charts/spark-operator-chart
    sudo helm install -f values.yaml bosler spark-operator/spark-operator --namespace bosler

    sudo kubectl -n bosler create serviceaccount spark
    sudo kubectl create clusterrolebinding spark-role --clusterrole=cluster-admin --user=system:serviceaccount:bosler:spark
    # below is run build from cluster
    sudo kubectl create clusterrolebinding spark-role-default --clusterrole=cluster-admin --user=system:serviceaccount:bosler:default

}

# tests

test()
{
    sudo kubectl -n bosler get SparkApplications
    sudo kubectl -n bosler get pods
    sudo kubectl -n bosler logs bosler-spark-driver
}

purge()
{
 
  sudo kubectl -n bosler get pods|grep driver|awk -F "-driver" '{print $1}'|while read line; do kubectl -n bosler delete sparkapplication $line; done

  sudo kubectl -n bosler get SparkApplications|grep bosler|awk '{print $1}'|while read line
  do
    sudo kubectl -n bosler delete SparkApplication $line
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
