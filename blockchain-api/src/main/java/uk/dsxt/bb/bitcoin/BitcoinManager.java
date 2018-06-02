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

package uk.dsxt.bb.bitcoin;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.blockchain.Message;
import uk.dsxt.bb.datamodel.bitcoin.BitcoinBlock;
import uk.dsxt.bb.datamodel.bitcoin.BitcoinChain;
import uk.dsxt.bb.datamodel.bitcoin.BitcoinPeer;
import uk.dsxt.bb.datamodel.bitcoin.BitcoinTransaction;
import uk.dsxt.bb.datamodel.bitcoin.BitcoinTransactionInList;
import uk.dsxt.bb.datamodel.bitcoin.BitcoinUnspentTransaction;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.utils.JSONRPCHelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Log4j2
public class BitcoinManager implements Manager {

    private String url;

    private enum BitcoinMethods {
        SENDTOADDRESS,
        LISTUNSPENT,
        LISTTRANSACTIONS,
        GETBLOCKHASH,
        GETBLOCK,
        GETPEERINFO,
        GETINFO,
        GETNEWADDRESS
    }

    private static final String BITCOIN_WALLET_ADDRESS = "mpkMbdQsiCCQ6x5YfufPsm5ByeJ73ccQ1V";

    @Override
    public String sendTransaction(String to, String from, long amount) {
        return sendMessage(to, Long.toString(amount).getBytes());
    }

    @Override
    public String sendMessage(byte[] body) {
        return sendMessage(BITCOIN_WALLET_ADDRESS, body);
    }

    @Override
    public String sendMessage(String from, String to, String message) {
        return null;
    }

    private String sendMessage(String address, byte[] body) {
        return JSONRPCHelper.post(url, BitcoinMethods.SENDTOADDRESS.name().toLowerCase(),
                address, BitcoinManager.toBigDecimal(body));
    }

    @Override
    public List<Message> getNewMessages() {
        List<BitcoinTransaction> transactions = new ArrayList<>();
        List<BitcoinTransactionInList> transactionsInList = Arrays.stream(getLastTransactions())
                .collect(Collectors.toList());
        if (transactionsInList != null) {
            transactions.addAll(transactionsInList);
        }
        return transactionsToResult(transactions);
    }

    private BitcoinUnspentTransaction[] getUnspentTransactions() {
        return JSONRPCHelper.post(url, BitcoinMethods.LISTUNSPENT.name().toLowerCase(), BitcoinUnspentTransaction[].class);
    }

    private BitcoinTransactionInList[] getLastTransactions() {
        return JSONRPCHelper.post(url, BitcoinMethods.LISTTRANSACTIONS.name().toLowerCase(),
                BitcoinTransactionInList[].class);
    }

    public List<Message> getMessages(String account, int count, int from) throws IOException  {
        List<BitcoinTransaction> transactions = new ArrayList<>();
        List<BitcoinTransactionInList> transactionsInList = Arrays.stream(getTransactions(account, count, from))
                .collect(Collectors.toList());
        if (transactionsInList != null) {
            transactions.addAll(transactionsInList);
        }

        return transactionsToResult(transactions);
    }

    public List<Message> getUnspentTransactionsList() {
        List<BitcoinTransaction> transactions = new ArrayList<>();
        List<BitcoinUnspentTransaction> transactionList = Arrays.stream(getUnspentTransactions())
                .collect(Collectors.toList());
        if (transactionList != null) {
            transactions.addAll(transactionList);
        }
        return transactionsToResult(transactions);
    }

    private List<Message> transactionsToResult(List<BitcoinTransaction> transactions) {
        List<Message> result = new ArrayList<>();

        transactions.forEach(
                transaction -> result.add(new Message(transaction.getTxId(),
                        (String.join(" ", "send to", transaction.getAddress(),
                                transaction.getAmount().toPlainString(), "address", "btc")),
                        transaction.isSpendable())
                ));
        return result;
    }

    private BitcoinTransactionInList[] getTransactions(String account, int count, int from) {
        return JSONRPCHelper.post(url, BitcoinMethods.LISTTRANSACTIONS.name().toLowerCase(),
                BitcoinTransactionInList[].class, account, count, from);
    }

    @Override
    public BitcoinBlock getBlockById(long blockId) {
        String blockHash = JSONRPCHelper.post(url, BitcoinMethods.GETBLOCKHASH.name().toLowerCase(), blockId);
        if (blockHash != null) {
            blockHash = blockHash.replaceAll("^.|.$", "");
        }
        return JSONRPCHelper.post(url, BitcoinMethods.GETBLOCK.name().toLowerCase(), BitcoinBlock.class, blockHash);
    }

    @Override
    public BlockchainBlock getBlockByHash(String hash) {
        return null;
    }

    @Override
    public BitcoinPeer[] getPeers() {
        return JSONRPCHelper.post(url, BitcoinMethods.GETPEERINFO.name().toLowerCase(),
                BitcoinPeer[].class);
    }

    @Override
    public BitcoinChain getChain() {
        return JSONRPCHelper.post(url, BitcoinMethods.GETINFO.name().toLowerCase(), BitcoinChain.class);
    }

    @Override
    public void authorize(String user, String password) {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        });
    }

    public String getNewAddress() {
        String postResult = JSONRPCHelper.post(url, BitcoinMethods.GETNEWADDRESS.name().toLowerCase());
        if (postResult != null) {
            return postResult.replaceAll("^.|.$", "");
        }
        return Strings.EMPTY;
    }

    private static BigDecimal toBigDecimal(byte[] bytes) {
        return new BigDecimal(ByteBuffer.wrap(bytes).toString());
    }
}
