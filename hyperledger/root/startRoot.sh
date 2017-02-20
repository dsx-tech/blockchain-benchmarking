#!/usr/bin/env bash
#fabric
docker pull hyperledger/fabric-peer:latest;
docker pull hyperledger/fabric-membersrvc:latest;
sudo chmod u+x e-voting;

export HOST_IP=${ROOT_NODE}; docker-compose up -d;