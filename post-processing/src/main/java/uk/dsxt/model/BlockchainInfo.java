/******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2017 DSX Technologies Limited.                               *
 * *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 * *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 * *
 * Removal or modification of this copyright notice is prohibited.            *
 * *
 ******************************************************************************/
package uk.dsxt.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class BlockchainInfo {

    private Map<Long, BlockInfo> blocks;
    private Map<Long, TransactionInfo> transactions;
    private List<NodeInfo> nodes;
    private Map<Long, Integer> timeToIntensities;
    private Map<Long, Integer> timeToUnverifiedTransactions;
    private Map<Long, Integer> timeToNumNodes;

    public BlockchainInfo(Map<Long, BlockInfo> blocks, Map<Long, TransactionInfo> transactions, List<NodeInfo> nodes) {
        this.blocks = blocks;
        this.transactions = transactions;
        this.nodes = nodes;
        this.timeToIntensities = new HashMap<>();
        timeToUnverifiedTransactions = new HashMap<>();
        timeToNumNodes = new HashMap<>();
    }

//    public BlockInfo getBlockById(long id) {
//        for (BlockInfo block : blocks) {
//            if (block.getBlockId() == id) {
//                return block;
//            }
//        }
//        return null;
//    }
//
//    public TransactionInfo getTransactionById(long id) {
//        for (TransactionInfo transactionInfo : transactions) {
//            if (transactionInfo.getTransactionId() == id) {
//                return transactionInfo;
//            }
//        }
//        return null;
//    }

    public BlockInfo getChildBlockById(long id) {
        for (BlockInfo block : blocks.values()) {
            if (block.getParentBlockId() == id) {
                return block;
            }
        }
        return null;
    }
}
