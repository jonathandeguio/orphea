FROM --platform=linux/amd64 node:16.16.0-alpine as build-stage

WORKDIR /app
# ENV NODE_OPTIONS=--max_old_space_size=32000

COPY ./ .
RUN npm set timeout=100000 \
    && npm set progress=false \
    # && yarn install --production \
    && yarn --silent install 

ENV NODE_ENV=production
RUN yarn build --verbose

# RUN npm install --legacy-peer-deps -prefer-offline --no-audit --progress=false --silent
# RUN npm install typescript @types/node ts-node --legacy-peer-deps -prefer-offline --no-audit --progress=false --silent
# RUN npm run build

FROM --platform=linux/amd64 nginx:latest as production-stage
RUN mkdir /app
COPY --from=build-stage /app/build /app
COPY nginx.conf /etc/nginx/nginx.conf
