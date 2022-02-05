#!/usr/bin/env bash

docker-compose --env-file ../.env down
docker-compose --env-file ../.env rm

