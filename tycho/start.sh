#!/bin/sh

docker build . --tag europe-west6-docker.pkg.dev/movetodata-dev2/movetodata-cr/tycho
docker push europe-west6-docker.pkg.dev/movetodata-dev2/movetodata-cr/tycho

# docker-compose -f docker-compose.yml up
