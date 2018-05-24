#!/usr/bin/env bash
export GETH_COMMON_PROPERTIES="--datadir=/home/ubuntu/eth-root/datadir --rpc --rpcport 8101 --rpccorsdomain \"*\" --rpcaddr 0.0.0.0 --rpcapi admin,db,eth,debug,miner,net,shh,txpool,personal,web3 --port 30303"
export GETH="geth ${GETH_COMMON_PROPERTIES}";
echo "$GETH";

echo "0000" > /home/ubuntu/root_init_files/0000
mkdir -p root_init_result

allocated_accounts=""
> /home/ubuntu/root_init_result/accounts;
> /home/ubuntu/root_init_result/credentials;
NEWLINE=$"\n"

for i in `seq 0 5`;
        do
                acc=$(${GETH} --password /home/ubuntu/root_init_files/0000 account new)
                acc=${acc:10:40}
                echo ${acc} >> /home/ubuntu/root_init_result/accounts
                echo -e "0x${acc} 0000" >> /home/ubuntu/root_init_result/credentials;
                acc="\"${acc}\": { \"balance\": \"10000000000000000000000\" }"
                if [ "$i" -ne "5" ]
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
        \"chainId\": 15161718,${NEWLINE}
        \"homesteadBlock\": 0,${NEWLINE}
        \"eip155Block\": 0,${NEWLINE}
        \"eip158Block\": 0${NEWLINE}
    },${NEWLINE}
    \"nonce\": \"0x0000000000000042\",${NEWLINE}
    \"mixhash\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",${NEWLINE}
    \"difficulty\": \"0x200\",${NEWLINE}
    \"coinbase\": \"0x0000000000000000000000000000000000000000\",${NEWLINE}
    \"timestamp\": \"0x00\",${NEWLINE}
    \"parentHash\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",${NEWLINE}
    \"gasLimit\": \"0xffffffff\",${NEWLINE}
    \"alloc\": {${NEWLINE}
        ${allocated_accounts}
    }${NEWLINE}
}${NEWLINE}"
echo -e ${genesis} > /home/ubuntu/eth-root/genesis.json;
echo -e ${genesis} > /home/ubuntu/root_init_result/genesis.json;
cp -r /home/ubuntu/eth-root/datadir/keystore/ /home/ubuntu/root_init_result/;

cd /home/ubuntu;
$GETH init /home/ubuntu/eth-root/genesis.json;

mkdir /home/ubuntu/.ethash;
$GETH makedag 0 /home/ubuntu/.ethash >makedag1 2>makedag2;
eth="$GETH"
cmd="$eth js <(echo 'console.log(admin.nodeInfo.enode); exit();') "
echo $cmd
raw_node=$(bash -c "$cmd" 2>/dev/null | grep enode | perl -pe "s/\[\:\:\]//g" | perl -pe "s/^/\"/; s/\s*$/\"/;" | tee)
raw_node=${raw_node:1:137}
raw_node="${raw_node}${ROOT_NODE}:30303"
echo $raw_node
echo $raw_node > /home/ubuntu/root_init_result/enode
nohup $GETH --mine --minerthreads=1 --etherbase=$(shuf -n 1 /home/ubuntu/root_init_result/accounts) >/dev/null 2>ethereum.log &