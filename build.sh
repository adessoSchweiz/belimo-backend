#!/usr/bin/env bash

mvn clean package
docker build -t robertbrem/belimo-backend:0.0.1 .