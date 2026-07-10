# to Delete

build_image()
{
  docker build . --tag europe-west6-docker.pkg.dev/movetodata-dev2/movetodata-cr/funnel --build-arg CACHEBUST=$(date +%s)
  docker push europe-west6-docker.pkg.dev/movetodata-dev2/movetodata-cr/funnel
}

# to Delete

delete_spark()
{
    kubectl -n movetodata delete SparkApplications movetodata-spark

    helm uninstall movetodata --namespace movetodata

    kubectl -n movetodata delete clusterrolebinding spark-role
    kubectl -n movetodata delete serviceaccounts movetodata-spark-operator
    kubectl -n movetodata delete serviceaccounts spark
}

# to Create

create_spark()
{
    helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator

    cd charts/spark-operator-chart
    helm install -f values.yaml movetodata spark-operator/spark-operator --namespace movetodata

    kubectl -n movetodata create serviceaccount spark
    kubectl create clusterrolebinding spark-role --clusterrole=cluster-admin --user=system:serviceaccount:movetodata:spark
    # below is run build from cluster
    kubectl create clusterrolebinding spark-role-default --clusterrole=cluster-admin --user=system:serviceaccount:movetodata:default

    # to deploy

    kubectl -n movetodata apply -f movetodata-spark-test.yaml
}

# tests

test()
{
    kubectl -n movetodata get SparkApplications
    kubectl -n movetodata get pods
    kubectl -n movetodata logs movetodata-spark-driver
}

upgrade_spark() {

    cd charts/spark-operator-chart
    helm upgrade -f values.yaml movetodata spark-operator/spark-operator --namespace movetodata
}

purge()
{
 
  kubectl -n movetodata get pods|grep driver|awk -F "-driver" '{print $1}'|while read line; do kubectl -n movetodata delete sparkapplication $line; done

  kubectl -n movetodata get SparkApplications|grep movetodata|awk '{print $1}'|while read line
  do
    kubectl -n movetodata delete SparkApplication $line
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
