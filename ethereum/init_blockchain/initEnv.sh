#!/usr/bin/env bash

sudo apt-get update;
#sudo apt-get -y upgrade;
sudo apt-get -y install default-jre
wget https://storage.googleapis.com/golang/go1.7.4.linux-amd64.tar.gz;
sudo tar -xvf go1.7.4.linux-amd64.tar.gz;
sudo mv go /usr/local;
export GOROOT=/usr/local/go;
export GOPATH=$HOME/gopath;
export PATH=$GOPATH/bin:$GOROOT/bin:$PATH;


mkdir gopath;
cd gopath;
git clone -b release/1.6 https://github.com/ethereum/go-ethereum;
sudo apt-get install -y build-essential golang;


cd go-ethereum;
make all;

cd ~;
mkdir eth-root;
cd ~/eth-root;
mkdir datadir;

cd ~;
touch initEnv.complete
