#!/usr/bin/env bash

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

kubectl create secret generic rabbitmq-zipkin-credentials \
    --from-literal=RABBIT_USER=rabbit-user-prod \
    --from-literal=RABBIT_PASSWORD=rabbit-pwd-prod \
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

kubectl create secret tls tls-certificate --key kubernetes/cert/tls.key --cert kubernetes/cert/tls.crt

docker-compose up -d mongodb mysql rabbitmq
docker tag edjaz/auth-server               edjaz/auth-server:v1
docker tag edjaz/product-composite-service edjaz/product-composite-service:v1 
docker tag edjaz/product-service           edjaz/product-service:v1
docker tag edjaz/recommendation-service    edjaz/recommendation-service:v1
docker tag edjaz/review-service            edjaz/review-service:v1

kubectl apply -k kubernetes/services/overlays/prod

kubectl wait --timeout=600s --for=condition=ready pod --all
