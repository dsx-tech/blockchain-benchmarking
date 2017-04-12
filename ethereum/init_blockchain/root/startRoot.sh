#!/usr/bin/env bash
export GETH_COMMON_PROPERTIES="--datadir=/home/ubuntu/eth-root/datadir --targetgaslimit 100 --gasprice 1000 --networkid 497 --rpc --rpcport 8101 --rpccorsdomain \"*\" --rpcaddr 0.0.0.0 --rpcapi admin,db,eth,debug,miner,net,shh,txpool,personal,web3 --port 30303 --ipcapi admin,db,eth,debug,miner,net,shh,txpool,personal,web3"
export GETH="/home/ubuntu/gopath/go-ethereum/build/bin/geth ${GETH_COMMON_PROPERTIES}"
echo $GETH

echo "0000" > ~/root_init_files/0000
mkdir root_init_result

accounts=()
allocated_accounts=""
> ~/root_init_result/accounts;
> ~/root_init_result/credentials;
NEWLINE=$"\n"

for i in `seq 0 1`;
        do
                acc=$(${GETH} --password ~/root_init_files/0000 account new)
                acc=${acc:10:40}
                accounts[$i]=${acc}
                echo ${acc} >> ~/root_init_result/accounts
                echo -e "0x${acc} 0000" >> ~/root_init_result/credentials;
                acc="\"${acc}\": { \"balance\": \"1000000000\" }"
                if [ "$i" -ne "1" ]
                then
                	acc="${acc},${NEWLINE}"
                else
                	acc="${acc}${NEWLINE}"
                fi
                echo ${acc}
                allocated_accounts="${allocated_accounts}${acc}"
        done

genesis="{${NEWLINE}
    \"config\": {${NEWLINE}
        \"chainId\": 497,${NEWLINE}
        \"homesteadBlock\": 10,${NEWLINE}
        \"eip155Block\": 0,${NEWLINE}
        \"eip158Block\": 0${NEWLINE}
    },${NEWLINE}
    \"difficulty\": \"0x0400\",${NEWLINE}
    \"gasLimit\": \"0x4c4b40\",${NEWLINE}
    \"alloc\": {${NEWLINE}
        ${allocated_accounts}
    }${NEWLINE}
}${NEWLINE}"
echo -e ${genesis} > ~/eth-root/genesis.json;
echo -e ${genesis} > ~/root_init_result/genesis.json;
cp -r ~/eth-root/datadir/keystore/ ~/root_init_result/;

cd ~;
$GETH init /home/ubuntu/eth-root/genesis.json;

mkdir /home/ubuntu/.ethash;
$GETH makedag 0 /home/ubuntu/.ethash >makedag1 2>makedag2;
eth="$GETH"
cmd="$eth js <(echo 'console.log(admin.nodeInfo.enode); exit();') "
echo $cmd
raw_node=$(bash -c "$cmd" 2>/dev/null |grep enode | perl -pe "s/\[\:\:\]//g" | perl -pe "s/^/\"/; s/\s*$/\"/;" | tee)
raw_node=${raw_node:1:137}
raw_node="${raw_node}${ROOT_NODE}:30303"
echo $raw_node
echo $raw_node > ~/root_init_result/enode
nohup $GETH --mine --etherbase=$(shuf -n 1 ~/root_init_result/accounts) >/dev/null 2>ethereum.log &