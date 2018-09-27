#!/usr/bin/env bash

docker stop mysql
docker rm mysql

docker run -d \
  --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=my-secret-pw \
  -e MYSQL_DATABASE=josef \
  mysql:5