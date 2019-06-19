#!/bin/sh

DB_NAME=users
TABLE_NAME=users

if [[ ! $(docker ps -q -f name=user-postgres) ]]; then
    if [[ $(docker ps -aq -f status=exited -f name=user-postgres) ]]; then
        # cleanup
        docker rm user-postgres
    fi
    docker run -d --name user-postgres -p 5432:5432 postgres:9.6.13
    sleep 5
    docker exec user-postgres psql --user=postgres -p 5432 -c "CREATE DATABASE $DB_NAME;" > /dev/null
else
    echo "it's already running"
fi