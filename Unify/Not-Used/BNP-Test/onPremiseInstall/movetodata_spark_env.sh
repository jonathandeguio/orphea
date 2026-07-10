# to Build

PROJECT_ID=movetodata-bnp
COMPUTE_REGION=europe-west1
COMPUTE_ZONE=europe-west1-b
CLUSTER_NAME=movetodata
LOCALREPO=movetodata-cr

build_image()
{
  docker build . --tag ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/${LOCALREPO}/funnel --build-arg CACHEBUST=$(date +%s)
  docker push ${COMPUTE_REGION}-docker.pkg.dev/${PROJECT_ID}/${LOCALREPO}/funnel
}

# to Delete

delete_spark()
{
    sudo kubectl -n movetodata delete SparkApplications movetodata-spark

    sudo helm uninstall movetodata --namespace movetodata

    sudo kubectl -n movetodata delete clusterrolebinding spark-role
    sudo kubectl -n movetodata delete serviceaccounts movetodata-spark-operator
    sudo kubectl -n movetodata delete serviceaccounts spark
}

# to Create

create_spark()
{

    sudo helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator

    sudo cd spark-charts/spark-operator-chart
    sudo helm install -f values.yaml movetodata spark-operator/spark-operator --namespace movetodata

    sudo kubectl -n movetodata create serviceaccount spark
    sudo kubectl create clusterrolebinding spark-role --clusterrole=cluster-admin --user=system:serviceaccount:movetodata:spark
    # below is run build from cluster
    sudo kubectl create clusterrolebinding spark-role-default --clusterrole=cluster-admin --user=system:serviceaccount:movetodata:default

}

# tests

test()
{
    sudo kubectl -n movetodata get SparkApplications
    sudo kubectl -n movetodata get pods
    sudo kubectl -n movetodata logs movetodata-spark-driver
}

purge()
{
 
  sudo kubectl -n movetodata get pods|grep driver|awk -F "-driver" '{print $1}'|while read line; do kubectl -n movetodata delete sparkapplication $line; done

  sudo kubectl -n movetodata get SparkApplications|grep movetodata|awk '{print $1}'|while read line
  do
    sudo kubectl -n movetodata delete SparkApplication $line
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
