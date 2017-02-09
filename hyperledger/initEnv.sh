#!/usr/bin/env bash
goPackageName=go1.7.3.linux-amd64.tar.gz;
sudo yum -y install vim;
curl -O https://storage.googleapis.com/golang/go1.7.3.linux-amd64.tar.gz;
sudo tar -C /usr/local -xzf ${goPackageName};
export PATH=$PATH:/usr/local/go/bin;
curl -L https://github.com/docker/compose/releases/download/1.8.1/docker-compose-`uname -s`-`uname -m` > docker-compose;
sudo chown root docker-compose;
sudo mv docker-compose /usr/local/bin;
sudo chmod +x /usr/local/bin/docker-compose;
docker pull hyperledger/fabric-peer:latest;
docker pull hyperledger/fabric-membersrvc:latest;
sudo chmod u+x chaincode_example02
