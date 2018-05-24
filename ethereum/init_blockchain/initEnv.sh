#!/usr/bin/env bash

sudo apt-get update;
sudo apt-get -y install default-jre

sudo apt-get install software-properties-common
sudo add-apt-repository -y ppa:ethereum/ethereum
sudo apt-get update
sudo apt-get install -y ethereum

cd ~;
mkdir eth-root;
cd ~/eth-root;
mkdir datadir;

cd ~;
touch initEnv.complete
