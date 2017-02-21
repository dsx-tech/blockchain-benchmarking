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
import uk.dsxt.bb.datamodel.ethereum.EthereumBlock;
import uk.dsxt.bb.datamodel.ethereum.EthereumInfo;
import uk.dsxt.bb.datamodel.ethereum.EthereumPeer;
import uk.dsxt.bb.datamodel.ethereum.EthereumTransaction;
import uk.dsxt.bb.utils.InternalLogicException;
import uk.dsxt.bb.utils.JSONRPCHelper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
@AllArgsConstructor
public class EthereumManager implements Manager {

    private String url;

    public String unlockAccount(String address, String passphrase, Long duration)
            throws MalformedURLException, InternalLogicException {
        return JSONRPCHelper.post(url, EthereumMethods.UNLOCK_ACCOUNT.getMethod(), address, passphrase, duration);
    }

    @Override
    public String sendMessage(byte[] body) {

        return Strings.EMPTY;
    }

    public String sendTransaction(String from, String to, byte[] amount) {
        try {
            return JSONRPCHelper.postToSendMessageEthereum(url, EthereumMethods.SEND_TRANSACTION.getMethod(), from, to,
                    new String(amount));
        } catch (IOException e) {
            log.error("Cannot send message", e);
        }
        return Strings.EMPTY;
    }

    public String mineBlocks() throws MalformedURLException, InternalLogicException {
        return JSONRPCHelper.post(url, EthereumMethods.START_MINING.getMethod());
    }

    public String stopMiningBlocks() throws MalformedURLException, InternalLogicException {
        return JSONRPCHelper.post(url, EthereumMethods.STOP_MINING.getMethod());
    }

    @Override
    public List<Message> getNewMessages() {
        List<Message> transactions = new ArrayList<>();
        List<String> pendingTransactions = null;

        try {
            pendingTransactions = getPendingTransaction();
        } catch (IOException | InternalLogicException e) {
            log.error("Cannot get pending transactions", e);
        }

        if (pendingTransactions != null) {
            pendingTransactions.forEach(pendingTransaction -> {
                try {
                    EthereumTransaction transaction = JSONRPCHelper.post(url, EthereumMethods.GET_TRANSACTION.getMethod(),
                            EthereumTransaction.class, pendingTransaction);
                    Message message = new Message(transaction.getTransactionIndex(), transaction.getValue(), false);
                    transactions.add(message);
                } catch (IOException e) {
                    log.error("Cannot get transaction", e);
                }
            });
        }

        return transactions;
    }

    public List<String> getPendingTransaction() throws IOException, InternalLogicException {
        EthereumBlock block = JSONRPCHelper.post(url, EthereumMethods.GET_BLOCK_BY_NUMBER.getMethod(), EthereumBlock.class,
                "pending", true);
        ArrayList<String> transactionsHash = new ArrayList<>();
        EthereumTransaction[] transactions = block.getTransactions();
        Arrays.stream(transactions).forEach(tr -> transactionsHash.add(tr.getHash()));
        return transactionsHash;
    }

    public EthereumTransaction[] getTransactionsFromBlock(long id) throws IOException {
        return getBlock(id).getTransactions();
    }

    @Override
    public EthereumBlock getBlock(long id) throws IOException {
        return JSONRPCHelper.post(url, EthereumMethods.GET_BLOCK_BY_NUMBER.getMethod(), EthereumBlock.class,
                Long.toString(id), true);
    }

    @Override
    public EthereumPeer[] getPeers() throws IOException {
        return JSONRPCHelper.post(url, EthereumMethods.GET_PEERS.getMethod(), EthereumPeer[].class);
    }

    public EthereumTransaction[] getTransactionsFromTxPool() throws IOException {
        return JSONRPCHelper.post(url, EthereumMethods.GET_TRANSACTIONS_FROM_POOL.getMethod(), EthereumTransaction[].class);
    }

    public String getLastBlockNumber() throws MalformedURLException, InternalLogicException {
        return JSONRPCHelper.post(url, EthereumMethods.GET_BLOCK_BY_NUMBER.getMethod());
    }

    @Override
    public EthereumInfo getChain() throws IOException {
        String numberOfPeers = null;
        String numberOfBlock = null;

        int amountOfPeers = 0;
        int amountOfBlocks = 0;

        try {
            numberOfPeers = JSONRPCHelper.post(url, EthereumMethods.GET_AMOUNT_OF_PEERS.getMethod());
            if (numberOfPeers != null) {
                numberOfPeers = numberOfPeers.replaceAll("^.|.$", "");
            }
            numberOfBlock = JSONRPCHelper.post(url, EthereumMethods.GET_LAST_BLOCK_NUMBER.getMethod());
            if (numberOfBlock != null) {
                numberOfBlock = numberOfBlock.replaceAll("^.|.$", "");
            }
        } catch (InternalLogicException e) {
            log.error("Cannot get chain", e);
        }

        if (numberOfPeers != null) {
            amountOfPeers = Integer.decode(numberOfPeers);
        }

        if (numberOfBlock != null) {
            amountOfBlocks = Integer.decode(numberOfBlock);
        }

        return new EthereumInfo(amountOfPeers, amountOfBlocks);
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
