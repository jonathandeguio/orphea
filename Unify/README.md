# :crystal_ball: Unify is a infrastructure as code project

[![Whitelist Workflow - Dev2](https://github.com/Orphea-io/Unify/actions/workflows/dev2.workflow.yml/badge.svg)](https://github.com/Orphea-io/Unify/actions/workflows/dev2.workflow.yml)

## :gift: Pre-requisite

docker desktop

if you want to run kubernetes then minikube or full kubernetes

## :red_circle: Get Started

Clone it first

```
git clone git@github.com:Orphea-io/Unify.git
```

This repository contains a `docker-compose` orchestrated application with Java, React, Spark services running behind an nginx reverse proxy. There is also Kubernetes support under folder Kuber.

## :zap: Running

&emsp; Well you can run under Docker or Kubernetes

### &emsp; Docker

```bash
cd Unify/Docker
./start.sh
```

### &emsp; Kubernetes

```bash
cd Unify/Kuber
./start.sh
```

or start.bat on Windows

Behind the scenes, it is pulling all required repos and doing a docker compose up / build. In Kubernetes, it is creating / starting all deployments.

Build can take some time for the first time. :hourglass:

## :electric_plug: Access

:tada: Once it is all up, you can access it on

### Main URL

Docker : http://localhost ( john : 1234 )

Kuber : http://<minikube_ip> ( john : 1234 )

### Swagger URL

Docker : http://localhost:8080/swagger-ui.html

Kuber : http://<minikube_ip>/swagger-ui.html

To find minikube ip run below command

```
minikube ip
```

## :link: Access URLs under Docker

| Access                | via Proxy                 | Direct                         |
| --------------------- | :------------------------ | :----------------------------- |
| Frontend              | http://localhost          | http://localhost:3000          |
| Kitab(Catalog)        | http://localhost/kitab    | http://localhost:8080/kitab    |
| Passport(Auth)        | http://localhost/passport | http://localhost:8080/passport |
| Fractal(Code Repo)    | http://localhost/fractal  | http://localhost:8080/fractal  |
| Zoro(Data Management) | http://localhost/zoro     | http://localhost:8080/zoro     |

## :link: Access URLs for Kubernetes

Access URLs will be same as Docker, just replace localhost with minikube ip.

## Architecture Diagram.

<img src="reverse proxy.png"/>
