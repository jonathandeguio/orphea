#!/usr/bin/env bash

#
git --version 2>&1 >/dev/null # improvement by tripleee
GIT_IS_AVAILABLE=$?
# ...
if [ $GIT_IS_AVAILABLE -ne 0 ]; then
  echo "Error:  git client is not installed; it is essential."
  exit
fi

# create repo directory
if [ ! -d repos ];  then
  mkdir repos
fi

cd repos

if [ ! -d frontend ]; then
  git clone git@github.com:MoveToData-io/frontend.git  > /dev/null
else
  cd frontend
  git pull  > /dev/null
  cd ..
fi

if [ ! -d boson ]; then
  git clone git@github.com:MoveToData-io/boson.git  > /dev/null
else
  cd boson
  git pull  > /dev/null
  cd ..
fi

cd ..

git pull > /dev/null

docker-compose up --build

# sleep for 10 seconds because db tables time to start
sleep 10
if [ `uname -s` == "Darwin" ]; then
  open -n -a "Google Chrome" --args "--new-tab" "http://localhost"
  open -n -a "Google Chrome" --args "--new-tab" "http://localhost:8080/swagger-ui.html"
else
  start "http://localhost"
  start "http://localhost:8080/swagger-ui.html"
fi


echo ""
echo ""
echo "+--------------------------------------------------------------------------+"
echo "|                           Say hello to MoveToData                            |"
echo "+--------------------------------------------------------------------------+"
echo "|                                                                          |"
echo "|                                                                          |"
echo -e "|   Here is the movetodata IP : localhost                            \t   |"
echo "|                                                                          |"
echo "|   Connect to MoveToData  : http://localhost                                  |"
echo -e "|   Connect to Swagger : http://localhost:8080/swagger-ui.html          \t   |"
echo "|                                                                          |"
echo "+--------------------------------------------------------------------------+"
echo ""