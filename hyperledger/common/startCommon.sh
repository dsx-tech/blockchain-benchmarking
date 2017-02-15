#!/usr/bin/env bash
#fabric
docker pull hyperledger/fabric-peer:latest;
docker pull hyperledger/fabric-membersrvc:latest;
sudo chmod u+x e-voting;
export HOST_IP=${NODE}; export ROOT=${ROOT_NODE}; export PEER_ID=${PEER_ID}; docker-compose up -d;