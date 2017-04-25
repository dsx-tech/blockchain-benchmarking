/*
 * *****************************************************************************
 *  * Blockchain benchmarking framework                                          *
 *  * Copyright (C) 2016 DSX Technologies Limited.                               *
 *  * *
 *  * This program is free software: you can redistribute it and/or modify       *
 *  * it under the terms of the GNU General Public License as published by       *
 *  * the Free Software Foundation, either version 3 of the License, or          *
 *  * (at your option) any later version.                                        *
 *  * *
 *  * This program is distributed in the hope that it will be useful,            *
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 *  * See the GNU General Public License for more details.                       *
 *  * *
 *  * You should have received a copy of the GNU General Public License          *
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *  * *
 *  * Removal or modification of this copyright notice is prohibited.            *
 *  * *
 *  *****************************************************************************
 */

package uk.dsxt.bb.datamodel.multichain;

import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;

import java.math.BigDecimal;

public class MultichainBlock implements BlockchainBlock {
    String hash;
    String miner;
    long confirmations;
    long size;
    long height;
    int version;
    String merkleroot;
    String[] tx;
    long time;
    long nonce;
    String bits;
    BigDecimal difficulty;
    String chainwork;
    String previousblockhash;
    String nextblockhash;

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public String getPreviousBlockHash() {
        return previousblockhash;
    }

    @Override
    public MultichainTransaction[] getTransactions() {
        MultichainTransaction[] transactions = new MultichainTransaction[tx.length];
        for (int i = 0; i < tx.length; i++) {
            transactions[i] = new MultichainTransaction(tx[i]);
        }
        return transactions;
    }
}
