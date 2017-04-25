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
    private long timeInterval;
    private static final int NUMBER_OF_TIME_SEGMENTS = 100;
    private BlockchainInfo blockchainInfo;

    public ResultsAnalyzer(BlockchainInfo blockchainInfo) {
        this.blockchainInfo = blockchainInfo;
    }

    public BlockchainInfo analyze() {
        // change time to counting from zero
        updateTimeFormat();
        //calculate timeInterval
        timeInterval = (getEndTime() - getStartTime()) / NUMBER_OF_TIME_SEGMENTS;
        // calculate creation time and times of distribution
        updateBlockInfos();
        blockchainInfo.setTimeSegments(calculateTimeSegments());
        blockchainInfo.setScenarioInfo(calculateScenarioInfo());
        return blockchainInfo;
    }

    private ScenarioInfo calculateScenarioInfo() {
        int numberOfNodes = blockchainInfo.getNumberOfNodes();
        int maxThroughput = 0;

        //todo throughput95
        //int throughput95;
        int averageThroughput = 0;
        int averageIntensity = 0;
        int averageTransactionSize = 0;
        int averageBlockSize = 0;
        long averageLatency = 0;
        for (TimeSegmentInfo timeSegmentInfo : blockchainInfo.getTimeSegments().values()) {
            if (timeSegmentInfo.getThroughput() > maxThroughput) {
                maxThroughput = timeSegmentInfo.getThroughput();
            }
            averageTransactionSize += timeSegmentInfo.getTransactionSize();
            averageThroughput += timeSegmentInfo.getThroughput();
            averageIntensity += timeSegmentInfo.getIntensity();
            averageBlockSize += timeSegmentInfo.getBlockSize();
            averageLatency += timeSegmentInfo.getLatency();
        }
        int numberOfTimeSegments = blockchainInfo.getTimeSegments().values().size();
        averageThroughput /= numberOfTimeSegments;
        averageIntensity /= numberOfTimeSegments;
        averageBlockSize /= numberOfTimeSegments;
        averageLatency /= numberOfTimeSegments;
        averageTransactionSize /= numberOfTimeSegments;
        return new ScenarioInfo(numberOfNodes, maxThroughput,
                averageThroughput, averageIntensity,
                averageTransactionSize, averageBlockSize, averageLatency);
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

    private Map<Long, TimeSegmentInfo> calculateTimeSegments() {
        NavigableMap<Long, TimeSegmentInfo> timeSegments = new TreeMap<>();
        // get max/min time
        long startTime = 0;
        long endTime = getEndTime();
        //add all intervals to list
        for (long i = startTime; i < endTime; i += timeInterval) {
            timeSegments.put(i, new TimeSegmentInfo(i));
        }
        //fill intensity and transactionSize fields from transactionInfo data
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            long time = transaction.getTime();
            if (time < 0) {
                log.error("Time out of range: " + time);
                continue;
            }
            //find correct time segment and increase intensity
            long floorTime = timeSegments.floorKey(time);
            TimeSegmentInfo timeSegment = timeSegments.get(floorTime);
            timeSegment.setIntensity(timeSegment.getIntensity() + 1);
            //recalculate medium transaction size
            timeSegment.setTransactionSize((int) recalculateMediumValue(
                    timeSegment.getNumberOfTransactions(),
                    timeSegment.getTransactionSize(),
                    transaction.getTransactionSize()));
            timeSegment.setNumberOfTransactions(timeSegment.getNumberOfTransactions() + 1);
        }
        //fill latency, throughput, numberOfTransactionsInBlock and blockSize fields from blockInfo data
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            long time = block.getCreationTime();
            if (time < 0) {
                log.error("Time out of range: " + time + " in block " + block.getBlockId());
                continue;
            }
            long floorTime = timeSegments.floorKey(time);
            TimeSegmentInfo timeSegmentInfo = timeSegments.get(floorTime);
            int numberOfBlocks = timeSegmentInfo.getNumberOfBlocks();
            //recalculate medium latency
            timeSegmentInfo.setLatency((int) recalculateMediumValue(
                    numberOfBlocks,
                    timeSegmentInfo.getLatency(),
                    block.getDistributionTime100()));
            //recalculate medium number of transactions in block
            timeSegmentInfo.setNumberTransactionsInBlock((int) recalculateMediumValue(
                    numberOfBlocks,
                    timeSegmentInfo.getNumberTransactionsInBlock(),
                    block.getTransactions().size()));
            //recalculate medium block size
            timeSegmentInfo.setBlockSize((int) recalculateMediumValue(
                    numberOfBlocks,
                    timeSegmentInfo.getBlockSize(),
                    block.getSize()));
            //calculating throughput as number of transactions in just created blocks
            timeSegmentInfo.setThroughput(timeSegmentInfo.getThroughput() + block.getTransactions().size());
            timeSegmentInfo.setNumberOfBlocks(numberOfBlocks + 1);
            //calculating throughput as number of transactions in distributed blocks
            timeSegmentInfo = timeSegments.get(timeSegments.floorKey(block.getCreationTime() + block.getDistributionTime100()));
            timeSegmentInfo.setDistributionThroughput
                    (timeSegmentInfo.getThroughput() + block.getTransactions().size());
        }
        return timeSegments;
    }

    //calculate maxTime and verificationTime
    //currently calculating blockSize as sum of transaction sizes in it
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
//        for(BlockInfo block : blockchainInfo.getBlocks().values()) {
//            for(BlockInfo.DistributionTime time : block.getDistributionTimes()) {
//                if(time.getTime() < minTime) {
//                    minTime = time.getTime();
//                }
//            }
//        }
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
