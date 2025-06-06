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
-d snap.bosler.io \
-d aurora.bosler.io \
-d orion.bosler.io \
-d nexus.bosler.io \
-d bora.bosler.io \
-d buran.bosler.io \
-d elara.bosler.io \
-d pulsar.bosler.io \
-d saros.bosler.io \
-d nova.bosler.io \
-d solara.bosler.io \
-d rado.bosler.io \
-d aldo.bosler.io

```

inside docker:

certbot renew



Then update kubernetes and reload ingress

```
cd certs/live/
cd demo.bosler.io/

cat privkey.pem|base64| sed 's/^/    /'

cat fullchain5.pem | base64 | sed 's/^/    /'
```

update templates/tls-auth-secret.yaml with above base64

then helm upgrade

change 443 port back to235 
