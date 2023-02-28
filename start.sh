#!/usr/bin/env bash

help()
{
  echo "Run docker services script for mini-games environment"
  echo "Available options:"
  echo "  -d - run only database service"
  echo "  -h - display this help page"
}

while getopts "dh" opt      # get options for -a and -b ( ':' - option has an argument )
do
    case $opt in
        d) docker compose --file docker-compose.yml --env-file .env build mini-games-dev-postgres
           docker compose --file docker-compose.yml --env-file .env up mini-games-dev-postgres
           exit;;
        h) help
           exit;;
        *) help
           exit;;
    esac
done

docker compose --file docker-compose.yml --env-file .env build
docker compose --file docker-compose.yml --env-file .env up
