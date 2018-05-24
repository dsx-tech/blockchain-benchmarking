#!/usr/bin/env bash
export GETH_COMMON_PROPERTIES="--datadir=/home/ubuntu/eth-root/datadir --rpc --rpcport 8101 --rpccorsdomain \"*\" --rpcaddr 0.0.0.0 --rpcapi admin,db,eth,debug,miner,net,shh,txpool,personal,web3 --port 30303";
export GETH="geth ${GETH_COMMON_PROPERTIES}";
echo $GETH;

$GETH init /home/ubuntu/common_init_files/root_init_result/genesis.json;
cp /home/ubuntu/common_init_files/root_init_result/keystore/* /home/ubuntu/eth-root/datadir/keystore;

mkdir /home/ubuntu/.ethash;
$GETH makedag 0 /home/ubuntu/.ethash >makedag1 2>makedag2;

nohup $GETH --mine --minerthreads=1 --etherbase=$(shuf -n 1 ~/common_init_files/root_init_result/accounts) --bootnodes="$(cat ~/common_init_files/root_init_result/enode)" >/dev/null 2>ethereum.log &