#!/usr/bin/env bash

VERSION=0.0.1
BUILD_VERSION=Ignite-$VERSION
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
IGNITE_PACKAGE=$SCRIPT_DIR/ignite_package
BUILD_PACKAGE=$SCRIPT_DIR/$BUILD_VERSION


"$SCRIPT_DIR"/gradlew clean build --no-daemon -x test

# create build package directory
if [ -d "$BUILD_PACKAGE" ]; then
  rm -rf "$BUILD_PACKAGE"
fi

mkdir "$BUILD_PACKAGE"
cd "$BUILD_PACKAGE" || exit
mkdir -p bin etc/certs logs

cp "$IGNITE_PACKAGE"/bin/start.sh "$BUILD_PACKAGE"/bin
cp "$SCRIPT_DIR"/build/libs/Ignite*.jar "$BUILD_PACKAGE"/bin

echo "# This is the main config for Ignite Envoy Agent
PORT=7845
ENVOY_ID=77e14d4a-4366-4692-b70b-26c2ccde0219
BASE_URL=https://dev.bosler.io/api
TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyYWtlc2giLCJpc3MiOiJodHRwOi8vZGV2LmJvc2xlci5pby9hcGkvcGFzc3BvcnQvdG9rZW4vQ3JlYXRlTG9uZ0xpdmVkIiwiZXhwIjoxNjY2MjI0MDAwfQ.gSo0G0XKhcUR9pWm1z6YJj1KbLNz_y2HmGwOCHLbjLY
PROXY=false
HTTP_PROXY=None
HTTPS_PROXY=None
" > "$BUILD_PACKAGE"/etc/ignite.conf

# download java
if [ ! -d $SCRIPT_DIR/java ]; then

  cd "$SCRIPT_DIR" || exit
  curl -O -s https://cdn.azul.com/zulu/bin/zulu11.52.13-ca-jdk11.0.13-macosx_aarch64.tar.gz

  tar xf zulu11.52.13-ca-jdk11.0.13-macosx_aarch64.tar.gz
  mv zulu11.52.13-ca-jdk11.0.13-macosx_aarch64 java

  rm -rf zulu11.52.13-ca-jdk11.0.13-macosx_aarch64.tar.gz

  # get certificate and inject into java cacerts
  echo | openssl s_client -servername NAME -connect dev.bosler.io:443 |  sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > $BUILD_PACKAGE/etc/certs/certificate.crt
  keytool -import -trustcacerts -keystore $SCRIPT_DIR/java/lib/security/cacerts --storepass changeit -noprompt -alias bosler -file $BUILD_PACKAGE/etc/certs/certificate.crt

fi

cp -r "$SCRIPT_DIR"/java "$BUILD_PACKAGE"

cd "$SCRIPT_DIR" || exit

tar cfz $BUILD_VERSION.tar.gz $BUILD_VERSION

# clean up

if [ -d "$BUILD_PACKAGE" ]; then
  rm -rf "$BUILD_PACKAGE"
fi
