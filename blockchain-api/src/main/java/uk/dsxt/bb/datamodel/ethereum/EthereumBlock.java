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

package uk.dsxt.bb.datamodel.ethereum;

import lombok.Value;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;

@Value
public class EthereumBlock implements BlockchainBlock {
    String hash;
    String difficulty;
    String extraData;
    String gasLimit;
    String gasUsed;
    String logsBloom;
    String miner;
    String mixHash;
    String nonce;
    String number;
    String parentHash;
    String receiptsRoot;
    String sha3Uncles;
    String size;
    String stateRoot;
    String timestamp;
    String totalDifficulty;
    EthereumTransaction[] transactions;
    String transactionsRoot;
    String[] uncles;

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public String getPreviousBlockHash() {
        return parentHash;
    }

    @Override
    public EthereumTransaction[] getTransactions() {
        return transactions;
    }

    @Override
    public long getTime() {
        return Long.parseLong(timestamp);
    }
}
