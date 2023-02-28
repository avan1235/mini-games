#!/usr/bin/env bash

help()
{
  echo "Run docker services script for mini-games environment."
  echo "By default only database service is started."
  echo "Available options:"
  echo "  -a - run all docker services including server app"
  echo "  -h - display this help page"
}

while getopts "ah" opt
do
    case $opt in
        a) docker compose --file docker-compose.yml --env-file .env build
           docker compose --file docker-compose.yml --env-file .env up
           exit;;
        h) help
           exit;;
        *) help
           exit;;
    esac
done

docker compose --file docker-compose.yml --env-file .env build mini-games-dev-postgres
docker compose --file docker-compose.yml --env-file .env up mini-games-dev-postgres
