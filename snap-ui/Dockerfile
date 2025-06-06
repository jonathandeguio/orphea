FROM --platform=linux/amd64 node:18.12 as build-stage

WORKDIR /app

COPY ./ .
RUN yarn set version 3.5.0
RUN yarn install --silent
#  --production
# && yarn list --depth=1 > yarn_packages.log

ENV NODE_ENV=production
# RUN yarn build --verbose
RUN yarn build --verbose

FROM --platform=linux/amd64 nginx:latest as production-stage
RUN mkdir /app
COPY --from=build-stage /app/build /app
# COPY --from=build-stage /app/yarn_packages.log /app
COPY nginx.conf /etc/nginx/nginx.conf
