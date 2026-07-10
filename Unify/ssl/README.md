Update SSL certificate on demo:

change 443 port from 240 to 235 

Run this as a ngnix:

```
docker run -it --rm --name nginx -v ${PWD}/nginx.conf:/etc/nginx/nginx.conf -v ${PWD}:/letsencrypt -v ${PWD}/certs:/etc/letsencrypt -p 80:80 -p 443:443 nginx
```


start certbo, build if needed:
```
docker build . -t certbot

docker run -it --rm --name certbot -v ${PWD}:/letsencrypt -v ${PWD}/certs:/etc/letsencrypt certbot bash
```

https://github.com/marcel-dempers/docker-development-youtube-series/tree/master/security/letsencrypt/introduction

Create new:

```
certbot certonly --webroot -w /letsencrypt \
-d snap.movetodata.io \
-d aurora.movetodata.io \
-d orion.movetodata.io \
-d nexus.movetodata.io \
-d bora.movetodata.io \
-d buran.movetodata.io \
-d elara.movetodata.io \
-d pulsar.movetodata.io \
-d saros.movetodata.io \
-d nova.movetodata.io \
-d solara.movetodata.io \
-d rado.movetodata.io \
-d aldo.movetodata.io

```

inside docker:

certbot renew



Then update kubernetes and reload ingress

```
cd certs/live/
cd demo.movetodata.io/

cat privkey.pem|base64| sed 's/^/    /'

cat fullchain5.pem | base64 | sed 's/^/    /'
```

update templates/tls-auth-secret.yaml with above base64

then helm upgrade

change 443 port back to235 
