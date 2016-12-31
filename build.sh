#!/usr/bin/env bash

sbt one-jar

docker build -t "uk.co.elder-api:latest" .

docker-compose -f "docker-compose.yml" up