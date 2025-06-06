#!/usr/bin/env zsh

# https://cloud.google.com/community/tutorials/nginx-ingress-gke

kubectl create deployment hello-app --image=gcr.io/google-samples/hello-app:1.0
kubectl expose deployment hello-app --port=8080 --target-port=8080

#  Needs helm v3  https://helm.sh/docs/intro/install/ 
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm install nginx-ingress ingress-nginx/ingress-nginx

# Needs jq (JSON parser similar to awk, but for JSON!) 
# Tip, install with pip or conda if you have them! https://stedolan.github.io/jq/
export NGINX_INGRESS_IP=$(kubectl get service nginx-ingress-ingress-nginx-controller -ojson | jq -r '.status.loadBalancer.ingress[].ip')

## NOT TESTED! ##
cat <<EOF > configurations/ingress-resource.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ingress-resource
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
spec:
  rules:
  - host: "${NGINX_INGRESS_IP}.nip.io"
    http:
      paths:
      - pathType: Prefix
        path: "/hello"
        backend:
          service:
            name: hello-app
            port:
              number: 8080
EOF

kubectl apply -f configurations/ingress-resource.yaml

echo "Verify ingress resource has been created ..."
kubectl get ingress ingress-resource




