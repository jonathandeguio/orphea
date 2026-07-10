

helm repo add superset https://apache.github.io/superset

helm dependency update
helm upgrade --install superset . --namespace movetodata
