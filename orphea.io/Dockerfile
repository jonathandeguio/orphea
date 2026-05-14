FROM python:3.9-slim


WORKDIR /usr/src/app

COPY ./ ./

# update apt-get

RUN apt-get update

# Running the main
CMD ["docker","compose","up","-d"]