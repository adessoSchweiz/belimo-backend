#!/usr/bin/env bash

docker stop belimo-backend
docker rm belimo-backend

docker run -d \
  --name belimo-backend \
  --net belimo \
  -p 8282:8080 \
  robertbrem/belimo-backend:0.0.1

docker logs -f belimo-backend