FROM --platform=linux/amd64 node:20-alpine as build-stage

WORKDIR /app
# ENV NODE_OPTIONS=--max_old_space_size=32000

COPY ./ .
RUN npm install --legacy-peer-deps --silent

ENV NODE_ENV=production
ENV NODE_OPTIONS="--max-old-space-size=8192"
RUN npm run build

# RUN npm install --legacy-peer-deps -prefer-offline --no-audit --progress=false --silent
# RUN npm install typescript @types/node ts-node --legacy-peer-deps -prefer-offline --no-audit --progress=false --silent
# RUN npm run build

FROM --platform=linux/amd64 nginx:latest as production-stage
RUN mkdir -p /app/learn
COPY --from=build-stage /app/build /app/learn
RUN touch /app/index.html
COPY nginx.conf /etc/nginx/nginx.conf
