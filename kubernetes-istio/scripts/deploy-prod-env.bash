#!/usr/bin/env bash

# Print commands to the terminal before execution and stop the script if any error occurs
set -ex


function waitForPods() {

    set +x
    local expectedPodCount=$1
    local labelSelection=$2
    local sleepSec=10

    n=0
    echo "Do we have $expectedPodCount pods with the label '$labelSelection' yet?"
    actualPodCount=$(kubectl get pod -l $labelSelection -o json | jq ".items | length")
    until [[ $actualPodCount == $expectedPodCount ]]
    do
        n=$((n + 1))
        if [[ $n == 40 ]]
        then
            echo " Give up"
            exit 1
        else
            echo -n "${actualPodCount}!=${expectedPodCount}, sleep $sleepSec..."
            sleep $sleepSec
            echo -n ", retry #$n, "
            actualPodCount=$(kubectl get pod -l $labelSelection -o json | jq ".items | length")
        fi
    done
    echo "OK! ($actualPodCount=$expectedPodCount)"

    set -x
}

if kubectl -n istio-system get secret istio-ingressgateway-certs > /dev/null ; then
    echo "Secret istio-ingressgateway-certs found, skip creating it..."
else
    echo "Secret istio-ingressgateway-certs not found, creating it..."
    kubectl create -n istio-system secret tls istio-ingressgateway-certs \
        --key kubernetes-istio/cert/tls.key \
        --cert kubernetes-istio/cert/tls.crt
fi

kubectl create configmap config-repo-auth-server       --from-file=docker-compose/config-repo/application.yml --from-file=docker-compose/config-repo/auth-server.yml --save-config
kubectl create configmap config-repo-gateway           --from-file=docker-compose/config-repo/application.yml --from-file=docker-compose/config-repo/gateway.yml --save-config
kubectl create configmap config-repo-product-composite --from-file=docker-compose/config-repo/application.yml --from-file=docker-compose/config-repo/product-composite.yml --save-config
kubectl create configmap config-repo-product           --from-file=docker-compose/config-repo/application.yml --from-file=docker-compose/config-repo/product.yml --save-config
kubectl create configmap config-repo-recommendation    --from-file=docker-compose/config-repo/application.yml --from-file=docker-compose/config-repo/recommendation.yml --save-config
kubectl create configmap config-repo-review            --from-file=docker-compose/config-repo/application.yml --from-file=docker-compose/config-repo/review.yml --save-config

kubectl create secret generic rabbitmq-credentials \
    --from-literal=SPRING_RABBITMQ_USERNAME=rabbit-user-prod \
    --from-literal=SPRING_RABBITMQ_PASSWORD=rabbit-pwd-prod \
    --save-config

kubectl create secret generic mongodb-credentials \
    --from-literal=SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin \
    --from-literal=SPRING_DATA_MONGODB_USERNAME=mongodb-user-prod \
    --from-literal=SPRING_DATA_MONGODB_PASSWORD=mongodb-pwd-prod \
    --save-config

kubectl create secret generic mysql-credentials \
    --from-literal=SPRING_DATASOURCE_USERNAME=mysql-user-prod \
    --from-literal=SPRING_DATASOURCE_PASSWORD=mysql-pwd-prod \
    --save-config

# Roles
kubectl apply -f kubernetes-istio/services/base/roles/roles.yml

kubectl create secret tls tls-certificate --key kubernetes-istio/cert/tls.key --cert kubernetes-istio/cert/tls.crt

eval $(minikube docker-env)
cd docker-compose

docker-compose up -d mongodb mysql rabbitmq


# Deploy v1 services
docker tag edjaz/auth-server               edjaz/auth-server:v1
docker tag edjaz/product-composite/service edjaz/product-composite/service:v1
docker tag edjaz/product/service           edjaz/product/service:v1
docker tag edjaz/recommendation/service    edjaz/recommendation/service:v1
docker tag edjaz/review/service            edjaz/review/service:v1

cd ..

kubectl apply -k kubernetes-istio/services/base/services
kubectl apply -k kubernetes-istio/services/overlays/prod/v1
kubectl apply -k kubernetes-istio/services/overlays/prod/istio

kubectl wait --timeout=600s --for=condition=available deployment --all

kubectl get deployment auth-server-v1 product-v1 product-composite-v1 recommendation-v1 review-v1 -o yaml | istioctl kube-inject -f - | kubectl apply -f -

waitForPods 5 'version=v1'

cd docker-compose

# Deploy v2 services
docker tag edjaz/auth-server               edjaz/auth-server:v2
docker tag edjaz/product-composite/service edjaz/product-composite/service:v2
docker tag edjaz/product/service           edjaz/product/service:v2
docker tag edjaz/recommendation/service    edjaz/recommendation/service:v2
docker tag edjaz/review/service            edjaz/review/service:v2

cd ..

kubectl apply -k kubernetes-istio/services/overlays/prod/v2

kubectl wait --timeout=600s --for=condition=available deployment --all

kubectl get deployment auth-server-v2 product-v2 product-composite-v2 recommendation-v2 review-v2 -o yaml | istioctl kube-inject -f - | kubectl apply -f -

waitForPods 5 'version=v2'


# Ensure that alls pos are ready
kubectl wait --timeout=120s --for=condition=Ready pod --all


set +ex