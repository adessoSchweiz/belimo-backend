#!/usr/bin/env bash

docker stop mysql
docker rm mysql

docker run -d \
  --name mysql \
  --net belimo \
  -e MYSQL_ROOT_PASSWORD=my-secret-pw \
  -e MYSQL_DATABASE=josef \
  mysql:5