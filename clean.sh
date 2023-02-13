#!/usr/bin/env bash

docker compose --file docker-compose.yml --env-file .env down
docker compose --file docker-compose.yml --env-file .env rm

