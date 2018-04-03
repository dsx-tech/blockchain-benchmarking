#!/usr/bin/env bash
###############################IN CONTAINER####################################################

# EXPORT VARS
#NODE_DIR=/node_dir
GETH_COMMON="--datadir=$NODE_DIR --networkid 497 --rpc --rpcport 8101 --rpccorsdomain \"*\" --rpcaddr 0.0.0.0 --rpcapi admin,db,eth,debug,miner,net,shh,txpool,personal,web3 --port 30303 --verbosity 3"

# INIT ETH NETWORK
geth $GETH_COMMON init $NODE_DIR/genesis.json
echo 'Init done'
# CREATE AND WRITE NEW ACCOUNT TO FILE
echo 'console.log(personal.newAccount("passphrase"));' > $NODE_DIR/getacc.js
geth $GETH_COMMON js $NODE_DIR/getacc.js > $NODE_DIR/acc

# CREATE AND ADD NEW ENODE TO FILE
echo 'console.log(admin.nodeInfo.enode);' > $NODE_DIR/getenode.js
geth $GETH_COMMON js $NODE_DIR/getenode.js > $NODE_DIR/enode

#RUN NODE
#nohup geth $GETH_COMMON --mine --minerthreads=1 --bootnodes $BOOTNODES >> $NODE_DIR/eth.log &
geth $GETH_COMMON --mine --minerthreads=1 --bootnodes $BOOTNODES 2>> $NODE_DIR/eth.log
#echo 'Run done'