#!/usr/bin/env bash

for NODE in $(cat instances); do
    echo ${NODE}
#    scp -i /mnt/d/Development/Diplom/blockchain-benchmarking/hyperledger/ecs.pem common/docker-compose.yml ec2-user@${NODE}:/home/ec2-user
    ssh -o "StrictHostKeyChecking no" -i /mnt/d/Development/Diplom/blockchain-benchmarking/hyperledger/blockchain_benchmarking.pem ec2-user@${NODE} "docker-compose stop;"
    ssh -o "StrictHostKeyChecking no" -i /mnt/d/Development/Diplom/blockchain-benchmarking/hyperledger/blockchain_benchmarking.pem ec2-user@${NODE} "docker rm \$(docker ps -a -q)"
done