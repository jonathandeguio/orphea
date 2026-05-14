#!/bin/sh

docker build . --tag europe-west6-docker.pkg.dev/orphea-dev2/orphea-cr/tycho
docker push europe-west6-docker.pkg.dev/orphea-dev2/orphea-cr/tycho

# docker-compose -f docker-compose.yml up
