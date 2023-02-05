#!/usr/bin/env bash

docker run --rm -p 5555:5555 -p 5556:5556 -v E:\Conduit\example\riemann.config:/etc/riemann.config riemannio/riemann