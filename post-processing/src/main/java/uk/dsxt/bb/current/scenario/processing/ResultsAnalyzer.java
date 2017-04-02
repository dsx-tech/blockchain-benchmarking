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

package uk.dsxt.bb.current.scenario.processing;

import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.current.scenario.model.*;

import java.util.*;

@Log4j2
public class ResultsAnalyzer {

    //note: time is counted from the start of the test in milliseconds
    public static final long TIME_INTERVAL = 1000;
    private BlockchainInfo blockchainInfo;

    public ResultsAnalyzer(BlockchainInfo blockchainInfo) {
        this.blockchainInfo = blockchainInfo;
    }

    public BlockchainInfo analyze() {
        // change time to counting from zero
        updateTimeFormat();
        // calculate creation time and times of distribution
        updateBlockInfos();
        blockchainInfo.setTimeInfos(calculateTimeInfos());
        return blockchainInfo;
    }

    private long recalculateMediumValue(int numberOfElements, long prevMediumValue, long newValue) {
        return ((prevMediumValue * numberOfElements) + newValue) / (numberOfElements + 1);
    }

    /**
     * switches all time from unix timestamp to timestamp counting from the beginning of the test
     */
    private void updateTimeFormat() {
        long startTime = getStartTime();
        for (TransactionInfo transactionInfo : blockchainInfo.getTransactions().values()) {
            transactionInfo.setTime(transactionInfo.getTime() - startTime);
        }
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            for (BlockInfo.DistributionTime time : block.getDistributionTimes()) {
                time.setTime(time.getTime() - startTime);
            }
        }
    }

    private Map<Long, TimeInfo> calculateTimeInfos() {
        NavigableMap<Long, TimeInfo> timeInfos = new TreeMap<>();
        // get max/min time
        long startTime = 0;
        long endTime = getEndTime();
        //add all intervals to list
        for (long i = startTime; i < endTime; i += TIME_INTERVAL) {
            timeInfos.put(i, new TimeInfo(i));
        }
        //fill intensity and transactionSize fields from transactionInfo data
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            long time = transaction.getTime();
            TimeInfo timeInfo = timeInfos.get(timeInfos.floorKey(time));
            timeInfo.setIntensity(timeInfo.getIntensity() + 1);
            timeInfo.setTransactionSize((int) recalculateMediumValue(
                    timeInfo.getNumberOfTransactions(),
                    timeInfo.getTransactionSize(),
                    transaction.getTransactionSize()));
            timeInfo.setNumberOfTransactions(timeInfo.getNumberOfTransactions() + 1);
        }
        //fill latency, throughput, numberOfTransactionsInBlock and blockSize fields from blockInfo data
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            long time = block.getCreationTime();
            TimeInfo timeInfo = timeInfos.get(timeInfos.floorKey(time));
            int numberOfBlocks = timeInfo.getNumberOfBlocks();
            timeInfo.setLatency((int) recalculateMediumValue(
                    numberOfBlocks,
                    timeInfo.getLatency(),
                    block.getDistributionTime95()));
            timeInfo.setNumberTransactionsInBlock((int) recalculateMediumValue(
                    numberOfBlocks,
                    timeInfo.getNumberTransactionsInBlock(),
                    block.getTransactions().size()));
            timeInfo.setBlockSize((int) recalculateMediumValue(
                    numberOfBlocks,
                    timeInfo.getBlockSize(),
                    block.getSize()));
            timeInfo.setThroughput(timeInfo.getThroughput() + block.getTransactions().size());
            timeInfo.setNumberOfBlocks(numberOfBlocks + 1);
        }
        return timeInfos;
    }

    //calculate maxTime and verificationTime
    private void updateBlockInfos() {
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            block.calculateCreationTime();
            block.calculateMaxTime();
        }
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            int size = 0;
            for (TransactionInfo transaction : block.getTransactions()) {
                size += transaction.getTransactionSize();
            }
            block.setSize(size);
        }
    }

    private long getStartTime() {
        long minTime = blockchainInfo.getTransactions().entrySet().iterator().next().getValue().getTime();
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            if (transaction.getTime() < minTime) {
                minTime = transaction.getTime();
            }
        }
        return minTime;
    }

    private long getEndTime() {
        long maxTime = 0;
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            if (transaction.getTime() > maxTime) {
                maxTime = transaction.getTime();
            }
        }
        return maxTime;
    }
}
