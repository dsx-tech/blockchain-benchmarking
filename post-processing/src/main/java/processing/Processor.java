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
package processing;

import model.BlockInfo;
import model.TransactionInfo;

import java.util.*;

public class Processor {

    private List<TransactionInfo> transactions;
    private List<BlockInfo> blocks;
    private Map<Long, Integer> intensities;
    private Map<Long, Integer> unverifiedTransactions;

    //note: time is counted from the start of the test in milliseconds
    private static final long TIME_INTERVAL = 5000;
    private static final int NUMBER_OF_VERIFICATION = 6;

    public Processor(List<TransactionInfo> transactions, List<BlockInfo> blocks) {
        if(transactions == null || blocks == null) {
            throw new NullPointerException();
        }
        this.transactions = transactions;
        this.blocks = blocks;
    }

    public List<TransactionInfo> getTransactions() {
        return transactions;
    }

    public List<BlockInfo> getBlocks() {
        return blocks;
    }

    public Map<Long, Integer> getIntensities() {
        return intensities;
    }

    public Map<Long, Integer> getUnverifiedTransactions() {
        return unverifiedTransactions;
    }

    public void process() {
        intensities = calculateIntensity();
        updateTransactionInfos();
        updateBlockInfos();
        unverifiedTransactions = calculateUnverifiedTransactions();
    }

    //transaction is verified when block with it is verified
    private Map<Long, Integer> calculateUnverifiedTransactions() {
        Map<Long, Integer> numUnverifiedTrans = new HashMap<>();
        long startTime = 0;
        long endTime = getEndTime();
        //add all intervals to list
        for (long i = startTime; i < endTime; i += TIME_INTERVAL) {
            numUnverifiedTrans.put(i, 0);
        }
        //foreach transaction find intervals where it has already been created but hasn't been verified yet
        for (TransactionInfo transaction : transactions) {
            long creationTime = transaction.getTime();
            BlockInfo block = getBlockById(transaction.getBlockId());
            if (block == null) {
                //ignore all problems
                continue;
            }
            long verificationTime = block.getVerificationTime();
            if (verificationTime == -1) {
                verificationTime = endTime;
            }
            for (Long time : numUnverifiedTrans.keySet()) {
                if (creationTime <= time && time <= verificationTime) {
                    numUnverifiedTrans.replace(time, unverifiedTransactions.get(time) + 1);
                }
            }
        }
        return numUnverifiedTrans;
    }

    private BlockInfo getBlockById(int id) {
        for (BlockInfo block : blocks) {
            if (block.getBlockId() == id) {
                return block;
            }
        }
        return null;
    }

    //calculate maxTime and verificationTime
    private void updateBlockInfos() {
        for (BlockInfo block : blocks) {
            block.calculateMaxTime();
        }
        for (BlockInfo block : blocks) {
            block.setVerificationTime(calculateVerificationTime(block));
        }
    }

    //block is verified when 6 blocks after it have been created

    /**
     * @return -1 if block wasn't verified
     **/
    private long calculateVerificationTime(BlockInfo blockInfo) {
        BlockInfo nextBlock = blockInfo;
        for (int i = 0; i < NUMBER_OF_VERIFICATION; i++) {
            nextBlock = getChildById(nextBlock.getBlockId());
            if (nextBlock == null) {
                return -1;
            }
        }
        return nextBlock.getMaxNodeTime();
    }

    private BlockInfo getChildById(int id) {
        for (BlockInfo block : blocks) {
            if (block.getParentBlockId() == id) {
                return block;
            }
        }
        return null;
    }

    //adding blockId to transactionInfo
    private void updateTransactionInfos() {
        for (BlockInfo blockInfo : blocks) {
            for (Integer transactionId : blockInfo.getTransactionIds()) {
                TransactionInfo transaction = findTransactionById(transactionId);
                if (transaction != null) {
                    transaction.setBlockId(blockInfo.getBlockId());
                }
            }
        }
    }

    private TransactionInfo findTransactionById(int id) {
        for (TransactionInfo transactionInfo : transactions) {
            if (transactionInfo.getTransactionId() == id) {
                return transactionInfo;
            }
        }
        return null;
    }

    private Map<Long, Integer> calculateIntensity() {
        Map<Long, Integer> intensities = new HashMap<>();
        // get max/min time
        long startTime = 0;
        long endTime = getEndTime();
        //add all intervals to list
        for (long i = startTime; i < endTime; i += TIME_INTERVAL) {
            intensities.put(i, 0);
        }
        //for each transaction add ++ to corresponding intensity
        for (TransactionInfo transaction : transactions) {
            long time = transaction.getTime();
            while (time > 0 && !intensities.containsKey(time)) {
                time--;
            }
            if (time < 0) {
                //ignoring all incorrect lines
                continue;
            }
            intensities.replace(time, intensities.get(time) + 1);
        }
        return intensities;
    }

    private long getEndTime() {
        long maxTime = 0;
        for (TransactionInfo transaction : transactions) {
            if (transaction.getTime() > maxTime) {
                maxTime = transaction.getTime();
            }
        }
        return maxTime;
    }
}
