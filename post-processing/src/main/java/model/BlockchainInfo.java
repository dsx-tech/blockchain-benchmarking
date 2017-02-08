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
package model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BlockchainInfo {

    private List<BlockInfo> blocks;
    private List<TransactionInfo> transactions;
    private Map<Long, Integer> timeToIntensities;
    private Map<Long, Integer> timeToUnverifiedTransactions;

    public BlockchainInfo(List<BlockInfo> blocks, List<TransactionInfo> transactions) {
        this.blocks = blocks;
        this.transactions = transactions;
    }

    public BlockInfo getBlockById(long id) {
        for (BlockInfo block : blocks) {
            if (block.getBlockId() == id) {
                return block;
            }
        }
        return null;
    }

    public TransactionInfo getTransactionById(long id) {
        for (TransactionInfo transactionInfo : transactions) {
            if (transactionInfo.getTransactionId() == id) {
                return transactionInfo;
            }
        }
        return null;
    }

    public BlockInfo getChildBlockById(long id) {
        for (BlockInfo block : blocks) {
            if (block.getParentBlockId() == id) {
                return block;
            }
        }
        return null;
    }
}
