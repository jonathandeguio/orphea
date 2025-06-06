#!/bin/sh

docker build . --tag europe-west6-docker.pkg.dev/bosler-dev2/bosler-cr/tycho
docker push europe-west6-docker.pkg.dev/bosler-dev2/bosler-cr/tycho

# docker-compose -f docker-compose.yml up
