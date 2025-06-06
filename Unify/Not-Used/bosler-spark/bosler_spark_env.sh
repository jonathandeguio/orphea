# to Delete

build_image()
{
  docker build . --tag europe-west6-docker.pkg.dev/bosler-dev2/bosler-cr/funnel --build-arg CACHEBUST=$(date +%s)
  docker push europe-west6-docker.pkg.dev/bosler-dev2/bosler-cr/funnel
}

# to Delete

delete_spark()
{
    kubectl -n bosler delete SparkApplications bosler-spark

    helm uninstall bosler --namespace bosler

    kubectl -n bosler delete clusterrolebinding spark-role
    kubectl -n bosler delete serviceaccounts bosler-spark-operator
    kubectl -n bosler delete serviceaccounts spark
}

# to Create

create_spark()
{
    helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator

    cd charts/spark-operator-chart
    helm install -f values.yaml bosler spark-operator/spark-operator --namespace bosler

    kubectl -n bosler create serviceaccount spark
    kubectl create clusterrolebinding spark-role --clusterrole=cluster-admin --user=system:serviceaccount:bosler:spark
    # below is run build from cluster
    kubectl create clusterrolebinding spark-role-default --clusterrole=cluster-admin --user=system:serviceaccount:bosler:default

    # to deploy

    kubectl -n bosler apply -f bosler-spark-test.yaml
}

# tests

test()
{
    kubectl -n bosler get SparkApplications
    kubectl -n bosler get pods
    kubectl -n bosler logs bosler-spark-driver
}

upgrade_spark() {

    cd charts/spark-operator-chart
    helm upgrade -f values.yaml bosler spark-operator/spark-operator --namespace bosler
}

purge()
{
 
  kubectl -n bosler get pods|grep driver|awk -F "-driver" '{print $1}'|while read line; do kubectl -n bosler delete sparkapplication $line; done

  kubectl -n bosler get SparkApplications|grep bosler|awk '{print $1}'|while read line
  do
    kubectl -n bosler delete SparkApplication $line
  done
}


usage()
{
  echo "$0 create|delete|upgrade|purge|test|all"
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
    "upgrade")
        upgrade_spark
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
