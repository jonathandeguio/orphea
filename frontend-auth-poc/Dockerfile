FROM --platform=linux/amd64 node:16.16.0-alpine as build-stage
WORKDIR /app
# ENV NODE_OPTIONS=--max_old_space_size=32000
ENV NODE_ENV=production
COPY ./ .
RUN npm set timeout=100000 \
    && npm set progress=false \
    && yarn install --production \
    && yarn build

#RUN npm install --legacy-peer-deps --production -prefer-offline --no-audit --progress=false
# RUN yarn run build

FROM --platform=linux/amd64 nginx:latest as production-stage
RUN mkdir /app
COPY --from=build-stage /app/build /app
COPY nginx.conf /etc/nginx/nginx.conf
