@echo off

git --version


IF EXIST repos (
    cd repos
) ELSE (
    mkdir repos
    git pull
    cd ..
)

IF EXIST snap-ui (
    git clone git@github.com:Bosler-io/snap-ui.git
) ELSE (
    cd snap-ui
    git pull
    cd ..
)

IF EXIST snap (
    git clone git@github.com:Bosler-io/snap.git
) ELSE (
    cd snap
    git pull
    cd ..
)

cd ..

git pull

docker-compose up --build