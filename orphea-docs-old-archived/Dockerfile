FROM --platform=linux/amd64 nginx:latest as production-stage
RUN mkdir /app
COPY ./docs /app
COPY nginx.conf /etc/nginx/nginx.conf