/*
 ******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2016 DSX Technologies Limited.                               *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************
 */

package uk.dsxt.bb.ethereum;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.blockchain.Message;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.datamodel.ethereum.EthereumBlock;
import uk.dsxt.bb.datamodel.ethereum.EthereumInfo;
import uk.dsxt.bb.datamodel.ethereum.EthereumPeer;
import uk.dsxt.bb.datamodel.ethereum.EthereumTransaction;
import uk.dsxt.bb.utils.JSONRPCHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
@AllArgsConstructor
public class EthereumManager implements Manager {

    private String url;

    private String unlockAccount(String address, String passphrase, Long duration) {
        return JSONRPCHelper.post(url, EthereumMethods.UNLOCK_ACCOUNT.getMethod(), address, passphrase, duration);
    }

    @Override
    public String sendMessage(byte[] body) {
        return Strings.EMPTY;
    }

    public String sendMessage(String from, String to, String message) {
        return sendTransactionWithMessage(from, to, message, "0x1");
    }

    public String sendTransactionWithMessage(String from, String to, String message, String amount) {
        return JSONRPCHelper.postToSendMessageEthereum(url, from, to, message, amount);
    }

    @Override
    public String sendTransaction(String from, String to, long amount) {
        return JSONRPCHelper.postToSendTransactionEthereum(url, from, to, Long.toString(amount));
    }

    public String mineBlocks() {
        return JSONRPCHelper.post(url, EthereumMethods.START_MINING.getMethod());
    }

    public String stopMiningBlocks() {
        return JSONRPCHelper.post(url, EthereumMethods.STOP_MINING.getMethod());
    }

    @Override
    public List<Message> getNewMessages() {
        List<Message> transactions = new ArrayList<>();
        List<String> pendingTransactions = null;

        pendingTransactions = getPendingTransaction();

        if (pendingTransactions != null) {
            pendingTransactions.forEach(pendingTransaction -> {
                EthereumTransaction transaction = JSONRPCHelper.post(url, EthereumMethods.GET_TRANSACTION.getMethod(),
                        EthereumTransaction.class, pendingTransaction);
                Message message = new Message(transaction.getTransactionIndex(), transaction.getValue(), false);
                transactions.add(message);
            });
        }

        return transactions;
    }

    public List<String> getPendingTransaction() {
        EthereumBlock block = JSONRPCHelper.post(url, EthereumMethods.GET_BLOCK_BY_NUMBER.getMethod(), EthereumBlock.class,
                "pending", true);
        ArrayList<String> transactionsHash = new ArrayList<>();
        EthereumTransaction[] transactions = block.getTransactions();
        Arrays.stream(transactions).forEach(tr -> transactionsHash.add(tr.getHash()));
        return transactionsHash;
    }

    public EthereumTransaction[] getTransactionsFromBlock(long id) {
        return getBlockById(id).getTransactions();
    }

    @Override
    public EthereumBlock getBlockById(long id) {
        return JSONRPCHelper.post(url, EthereumMethods.GET_BLOCK_BY_NUMBER.getMethod(), EthereumBlock.class,
                "0x" + Long.toHexString(id), true);
    }

    @Override
    public BlockchainBlock getBlockByHash(String hash) {
        return null;
    }

    @Override
    public EthereumPeer[] getPeers() {
        return JSONRPCHelper.post(url, EthereumMethods.GET_PEERS.getMethod(), EthereumPeer[].class);
    }

    public EthereumTransaction[] getTransactionsFromTxPool() {
        return JSONRPCHelper.post(url, EthereumMethods.GET_TRANSACTIONS_FROM_POOL.getMethod(), EthereumTransaction[].class);
    }

    public String getLastBlockNumber() {
        return JSONRPCHelper.post(url, EthereumMethods.GET_BLOCK_BY_NUMBER.getMethod());
    }

    @Override
    public EthereumInfo getChain() {
        String numberOfPeers = null;
        String numberOfBlock = null;

        int amountOfPeers = 0;
        int amountOfBlocks = 0;

        numberOfPeers = JSONRPCHelper.post(url, EthereumMethods.GET_AMOUNT_OF_PEERS.getMethod());
        if (numberOfPeers != null) {
            numberOfPeers = numberOfPeers.replaceAll("^.|.$", "");
        }
        numberOfBlock = JSONRPCHelper.post(url, EthereumMethods.GET_LAST_BLOCK_NUMBER.getMethod());
        if (numberOfBlock != null) {
            numberOfBlock = numberOfBlock.replaceAll("^.|.$", "");
        }

        if (numberOfPeers != null) {
            amountOfPeers = Integer.decode(numberOfPeers);
        }

        if (numberOfBlock != null) {
            amountOfBlocks = Integer.decode(numberOfBlock);
        }

        return new EthereumInfo(amountOfPeers, amountOfBlocks);
    }

    @Override
    public void authorize(String address, String password) {
        // Unlock with 0 duration unlocks account until geth exists
        // https://github.com/ethereum/go-ethereum/wiki/Management-APIs#personal_unlockaccount
        unlockAccount(address, password, 0L);
    }

    private enum EthereumMethods {
        UNLOCK_ACCOUNT("personal_unlockAccount"),
        SEND_TRANSACTION("eth_sendTransaction"),
        START_MINING("miner_start"),
        STOP_MINING("miner_stop"),
        GET_BLOCK_BY_NUMBER("eth_getBlockByNumber"),
        GET_PEERS("admin_peers"),
        GET_TRANSACTIONS_FROM_POOL("txpool_content"),
        GET_LAST_BLOCK_NUMBER("eth_blockNumber"),
        GET_AMOUNT_OF_PEERS("net_peerCount"),
        GET_TRANSACTION("eth_getTransactionByHash");

        private final String method;

        EthereumMethods(String method) {
            this.method = method;
        }

        public String getMethod() {
            return method;
        }
    }
}
