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
import uk.dsxt.bb.datamodel.bitcoin.*;
import uk.dsxt.bb.utils.InternalLogicException;
import uk.dsxt.bb.utils.JSONRPCHelper;

import java.io.IOException;
import java.math.BigDecimal;
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
    public String sendMessage(byte[] body) {
        try {
            return sendMessage(BITCOIN_WALLET_ADDRESS, body);
        } catch (IOException e) {
            log.error("Error while sending message", e);
        }
        return null;
    }

    private String sendMessage(String address, byte[] body) throws IOException {
        try {
            return JSONRPCHelper.post(url, BitcoinMethods.SENDTOADDRESS.name().toLowerCase(),
                    address, BitcoinManager.toBigDecimal(body));
        } catch (InternalLogicException e) {
            log.error("Cannot spend transaction", e);
        }
        return Strings.EMPTY;
    }

    @Override
    public List<Message> getNewMessages() {
        List<BitcoinTransaction> transactions = new ArrayList<>();
        try {
            List<BitcoinTransactionInList> transactionsInList = Arrays.stream(getLastTransactions())
                    .collect(Collectors.toList());
            if (transactionsInList != null) {
                transactionsInList.forEach(transactions::add);
            }
        } catch (IOException e) {
            log.error("Error while getting new transactions", e);
        }
        return transactionsToResult(transactions);
    }

    private BitcoinUnspentTransaction[] getUnspentTransactions() throws IOException {
        return JSONRPCHelper.post(url, BitcoinMethods.LISTUNSPENT.name().toLowerCase(), BitcoinUnspentTransaction[].class);
    }

    private BitcoinTransactionInList[] getLastTransactions() throws IOException {
        return JSONRPCHelper.post(url, BitcoinMethods.LISTTRANSACTIONS.name().toLowerCase(),
                BitcoinTransactionInList[].class);
    }

    public List<Message> getMessages(String account, int count, int from) throws IOException  {
        List<BitcoinTransaction> transactions = new ArrayList<>();
        List<BitcoinTransactionInList> transactionsInList = Arrays.stream(getTransactions(account, count, from))
                .collect(Collectors.toList());
        if (transactionsInList != null) {
            transactionsInList.forEach(transactions::add);
        }

        return transactionsToResult(transactions);
    }

    public List<Message> getUnspentTransactionsList() throws IOException {
        List<BitcoinTransaction> transactions = new ArrayList<>();
        List<BitcoinUnspentTransaction> transactionList = Arrays.stream(getUnspentTransactions())
                .collect(Collectors.toList());
        if (transactionList != null) {
            transactionList.forEach(transactions::add);
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

    private BitcoinTransactionInList[] getTransactions(String account, int count, int from) throws IOException {
        return JSONRPCHelper.post(url, BitcoinMethods.LISTTRANSACTIONS.name().toLowerCase(),
                BitcoinTransactionInList[].class, account, count, from);
    }

    @Override
    public BitcoinBlock getBlock(long blockId) throws IOException {
        String blockHash = null;
        try {
            blockHash = JSONRPCHelper.post(url, BitcoinMethods.GETBLOCKHASH.name().toLowerCase(), blockId);
            if (blockHash != null) {
                blockHash = blockHash.replaceAll("^.|.$", "");
            }
        } catch (InternalLogicException e) {
            log.error("Cannot get block", e);
        }
        return JSONRPCHelper.post(url, BitcoinMethods.GETBLOCK.name().toLowerCase(), BitcoinBlock.class, blockHash);
    }

    @Override
    public BitcoinPeer[] getPeers() throws IOException {
        return JSONRPCHelper.post(url, BitcoinMethods.GETPEERINFO.name().toLowerCase(),
                BitcoinPeer[].class);
    }

    @Override
    public BitcoinChain getChain() throws IOException {
        return JSONRPCHelper.post(url, BitcoinMethods.GETINFO.name().toLowerCase(), BitcoinChain.class);
    }

    public String getNewAddress() throws IOException {
        try {
            String postResult = JSONRPCHelper.post(url, BitcoinMethods.GETNEWADDRESS.name().toLowerCase());
            if (postResult != null) {
                return postResult.replaceAll("^.|.$", "");
            }
        } catch (InternalLogicException e) {
            log.error("Cannot get new bitcoin wallet address", e);
        }
        return Strings.EMPTY;
    }

    private static BigDecimal toBigDecimal(byte[] bytes) {
        return new BigDecimal(ByteBuffer.wrap(bytes).toString());
    }
}
