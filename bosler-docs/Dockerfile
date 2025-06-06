FROM --platform=linux/amd64 node:18.17.0-alpine as build-stage

WORKDIR /app
# ENV NODE_OPTIONS=--max_old_space_size=32000

COPY ./ .
# Set the yarn version and install dependencies
RUN yarn set version 3.5.0 && \
    yarn install --silent

ENV NODE_ENV=production
ENV NODE_OPTIONS="--max-old-space-size=8192"
RUN yarn build 

# RUN npm install --legacy-peer-deps -prefer-offline --no-audit --progress=false --silent
# RUN npm install typescript @types/node ts-node --legacy-peer-deps -prefer-offline --no-audit --progress=false --silent
# RUN npm run build

FROM --platform=linux/amd64 nginx:latest as production-stage
RUN mkdir -p /app/learn
COPY --from=build-stage /app/build /app/learn
RUN touch /app/index.html
COPY nginx.conf /etc/nginx/nginx.conf
