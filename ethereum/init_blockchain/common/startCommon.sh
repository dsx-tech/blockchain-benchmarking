#!/usr/bin/env bash
export GETH_COMMON_PROPERTIES="--datadir=/home/ubuntu/eth-root/datadir --targetgaslimit 100 --gasprice 1000 --networkid 497 --rpc --rpcport 8101 --rpccorsdomain \"*\" --rpcaddr 0.0.0.0 --rpcapi admin,db,eth,debug,miner,net,shh,txpool,personal,web3 --port 30303";
export GETH="/home/ubuntu/gopath/go-ethereum/build/bin/geth ${GETH_COMMON_PROPERTIES}";
echo $GETH;

$GETH init ~/common_init_files/root_init_result/genesis.json;
cp ~/common_init_files/root_init_result/keystore/* ~/eth-root/datadir/keystore

mkdir /home/ubuntu/.ethash;
$GETH makedag 0 /home/ubuntu/.ethash >makedag1 2>makedag2;

nohup $GETH --mine --minerthreads=1 --etherbase=$(shuf -n 1 ~/common_init_files/root_init_result/accounts) --bootnodes $(cat ~/common_init_files/root_init_result//enode) >/dev/null 2>ethereum.log &