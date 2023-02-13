#!/usr/bin/env bash

docker compose --file docker-compose.yml --env-file .env build
docker compose --file docker-compose.yml --env-file .env up
