#!/bin/bash
docker compose down -v
rm -rf ./data/mysql
docker compose up --build
