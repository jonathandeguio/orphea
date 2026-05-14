# to Build

# get environment variables
if [ $# -eq 0 ]; then
    echo "No arguments supplied. You need to use env-{project}.sh as argument"
fi

source env-${PROJECT_ID}.sh

build_image() {
    docker build . --tag ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/${LOCALREPO}/funnel --build-arg CACHEBUST=$(date +%s)
    docker push ${COMPUTE_REGION}-docker.pkg.dev/${REPO_ID}/${LOCALREPO}/funnel
}

# to Delete

delete_spark() {
    kubectl -n orphea delete SparkApplications orphea-spark

    helm uninstall orphea-spark --namespace orphea

    kubectl delete clusterrolebinding spark-role
    kubectl delete clusterrolebinding spark-role-default
    kubectl -n orphea delete serviceaccounts orphea-spark-operator
    kubectl -n orphea delete serviceaccounts spark
}

# to Create

create_spark() {

    helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator

    cd charts/spark-operator
    helm install -f values.yaml orphea-spark spark-operator/spark-operator --namespace orphea

    kubectl -n orphea create serviceaccount spark
    kubectl create clusterrolebinding spark-role --clusterrole=cluster-admin --user=system:serviceaccount:orphea:spark
    # below is run build from cluster
    kubectl create clusterrolebinding spark-role-default --clusterrole=cluster-admin --user=system:serviceaccount:orphea:default

}

# tests

test() {
    kubectl -n orphea get SparkApplications
    kubectl -n orphea get pods
    kubectl -n orphea logs orphea-spark-driver
}

purge() {

    kubectl -n orphea get pods | grep driver | awk -F "-driver" '{print $1}' | while read line; do kubectl -n orphea delete sparkapplication $line; done

    kubectl -n orphea get SparkApplications | grep orphea | awk '{print $1}' | while read line; do
        kubectl -n orphea delete SparkApplication $line
    done
}

usage() {
    echo "$0 create|delete|test|all"
}

opt="$1"

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
    usage
    exit 1
    ;;
esac
