#!/usr/bin/env bash

for NODE in $(cat instances); do
    echo ${NODE}
    scp -i ~/Development/Projects/AWS/Keys/ecs.pem common/docker-compose.yml ec2-user@${NODE}:/home/ec2-user
    ssh -o "StrictHostKeyChecking no" -i ~/Development/Projects/AWS/Keys/ecs.pem ec2-user@${NODE} "docker-compose stop;"
done