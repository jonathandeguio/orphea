# Unify is a infrastructure as code project

Clone it with recursive since it has submodules

```
git clone git@github.com:MoveToData-io/Unify.git
```

This repository contains a `docker-compose` orchestrated application with Java, React, Spark services running behind an nginx reverse proxy.

## Running

```bash
cd Unify
./start.sh
```

or start.bat on Windows

Behind the scenes, it is pulling all required repos and doing a docker compose up / build

Build can take some time for the first time.

Once it is all up, you can access it on http://localhost ( john : 1234 )

Access Swagger interface here : http://localhost:8080/swagger-ui.html

## Access URLs

| Access             | via Proxy                 | Direct                         |
| ------------------ | :------------------------ | :----------------------------- |
| Frontend           | http://localhost          | http://localhost:3000          |
| Kitab(Catalog)     | http://localhost/kitab    | http://localhost:8080/kitab    |
| Passport(Auth)     | http://localhost/passport | http://localhost:8080/passport |
| Fractal(Code Repo) | http://localhost/fractal  | http://localhost:8080/fractal  |
| Julia(Git Backend) | http://localhost/julia    | http://localhost:5001          |

## Architecture Diagram

<img src="reverse proxy.png"/>
