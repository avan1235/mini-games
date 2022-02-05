#!/usr/bin/env bash

docker compose --file ./dev-env/docker-compose.yml --env-file ./.env up
