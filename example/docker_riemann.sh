#!/usr/bin/env bash

docker container stop riemann
docker container rm riemann
docker container run -d --hostname conduit.riemann.dev --name riemann -p 5555:5555 -p 5556:5556 -v $(pwd)/riemann.config:/etc/riemann.config riemannio/riemann
