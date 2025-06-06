ARG DEBIAN_FRONTEND=noninteractive
FROM --platform=linux/amd64 ubuntu as build-stage

# Cleanup
RUN  apt-get clean && \
  rm -rf /var/lib/ap


# COPY dependency files
RUN mkdir dependencies
COPY dependencies/packages.txt dependencies/
COPY dependencies/requirements.txt dependencies/

ENV TZ=Europe/Paris
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Install Python
RUN apt-get -qq update && \
  apt-get -qq upgrade -y && \
  apt-get install -qq -y $(cat dependencies/packages.txt) && \
  rm -rf /var/lib/apt/lists/*

ENV NODE_VERSION=16.13.0
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
ENV NVM_DIR=/root/.nvm
RUN . "$NVM_DIR/nvm.sh" && nvm install ${NODE_VERSION}
RUN . "$NVM_DIR/nvm.sh" && nvm use v${NODE_VERSION}
RUN . "$NVM_DIR/nvm.sh" && nvm alias default v${NODE_VERSION}
ENV PATH="/root/.nvm/versions/node/v${NODE_VERSION}/bin/:${PATH}"

# TODO put below in julia or key in vault
WORKDIR /opt/
ARG CACHEBUST=1
RUN git clone https://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@github.com/Bosler-io/boson

WORKDIR /opt/boson/funnel/

COPY ./ /

#  pip3 install --upgrade pip --break-system-packages && \

RUN \
  pip3 install -q -r /dependencies/requirements.txt --break-system-packages && \
  pip3 install /opt/boson/funnel/ --break-system-packages

COPY package*.json /

# DISABLE SOME PYCODESTYLE ERRORS
RUN mkdir /root/.config/
COPY python-lsp-server/setup.cfg /root/.config/pycodestyle
COPY ./sqlfluff/.sqlfluff /root/.config/.sqlfluff

RUN pip3 install /sqlfluff/ --break-system-packages

RUN npm install -g typescript
RUN npm install -g ts-node

RUN npm install

RUN npm run build

# RUN npm run start:main






