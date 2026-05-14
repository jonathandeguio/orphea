# This is the package for ignite agent installation.

## it needs below certificate in java store

keytool -import -trustcacerts -keystore download_package/java/lib/security/cacerts --storepass changeit -noprompt -alias orphea -file download_package/etc/certs/orphea_public_cert.cer

# Note : it will also need java folder with java 11