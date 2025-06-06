## DNSMASQ

https://hub.docker.com/r/strm/dnsmasq

```
% cat dnsmasq.conf 
#log all dns queries
log-queries
#dont use hosts nameservers
no-resolv
#use google as default nameservers
server=1.1.1.1
server=8.8.8.8
#explicitly define host-ip mappings:
#address=/router-example/10.1.1.1
#address=/server-example/10.1.1.2
#address=/server2-example/10.1.1.3

% cat docker-compose.yml 
version: '2'
services:
  dns:
    restart: always
    image: strm/dnsmasq
    volumes:
      - ./dnsmasq.conf:/etc/dnsmasq.conf
    ports:
      - "53:53/udp"
    cap_add:
      - NET_ADMIN
```

From the command line in daemon mode: <br>
`docker run -d -p 53:53/udp -v $PWD/dnsmasq.conf:/etc/dnsmasq.conf --cap-add=NET_ADMIN strm/dnsmasq`


