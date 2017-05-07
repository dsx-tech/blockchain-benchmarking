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

package uk.dsxt.bb.scenario.proccessing;

import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.properties.proccessing.model.PropertiesFileInfo;
import uk.dsxt.bb.scenario.proccessing.model.*;

import java.util.*;

@Log4j2
public class ResultsAnalyzer {

    //note: time is counted from the start of the test in milliseconds
    private long timeInterval;
    private static final int NUMBER_OF_TIME_SEGMENTS = 100;
    private BlockchainInfo blockchainInfo;
    private PropertiesFileInfo properties;

    public ResultsAnalyzer(BlockchainInfo blockchainInfo, PropertiesFileInfo propertiesFileInfo) {
        this.properties = propertiesFileInfo;
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
        blockchainInfo.setScenarioInfo(new ScenarioInfo(blockchainInfo, properties));
        return blockchainInfo;
    }

    private double calcMedium
            (int numberOfElements, double prevMediumValue, double newValue) {
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
        calculateIntensity(timeSegments);
        //fillTimeSegmentInfo latency, throughput, numberOfTransactionsInBlock and blockSize fields from blockInfo general
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            double time = block.getCreationTime() + block.getLatency();
            if (time < 0) {
                log.error("Time out of range: " + time + " in block " + block.getBlockId());
                continue;
            }
            long floorTime = timeSegments.floorKey((long) time);
            TimeSegmentInfo timeSegmentInfo = timeSegments.get(floorTime);
            fillTimeSegmentInfo(timeSegmentInfo, block);
        }
        return timeSegments;
    }

    private void fillTimeSegmentInfo(TimeSegmentInfo time, BlockInfo block) {
        int numberOfBlocks = time.getNumberOfBlocks();
        //recalculate medium latency
        time.setLatency(calcMedium(numberOfBlocks, time.getLatency(), block.getLatency()));
        //recalculate medium number of transactions in block
        time.setNumberTransactionsInBlock(
                (int) calcMedium(numberOfBlocks,
                        time.getNumberTransactionsInBlock(), block.getTransactions().size()));
        //recalculate medium block size
        time.setBlockSize(calcMedium(numberOfBlocks, time.getBlockSize(), block.getSize()));
        //recalculate throughput
        time.setThroughput(time.getThroughput() + block.getTransactions().size());
        //recalculate medium transaction size
        time.setTransactionSize(calculateMediumTransactionSize(time, block));
        time.setNumberOfBlocks(numberOfBlocks + 1);
    }

    private double calculateMediumTransactionSize(TimeSegmentInfo time, BlockInfo block) {
        return (time.getNumberOfTransactions() * time.getTransactionSize() + block.getSize())
                / (time.getNumberOfTransactions() + block.getTransactions().size());
    }

    /**
     * recalculate intensity field in timeSegment
     *
     * @param timeSegments
     */
    private void calculateIntensity(NavigableMap<Long, TimeSegmentInfo> timeSegments) {
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            long time = transaction.getTime();
            if (time < 0) {
                log.error("Time out of range: " + time +
                        " in transaction " + transaction.getTransactionId());
                continue;
            }
            //find correct time segment and increase intensity
            long floorTime = timeSegments.floorKey(time);
            TimeSegmentInfo timeSegment = timeSegments.get(floorTime);
            timeSegment.setIntensity(timeSegment.getIntensity() + 1);
        }
    }

    /**
     * calculate block creation time and latency
     * currently calculating blockSize as sum of transaction sizes in it
     */
    private void updateBlockInfos() {
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            block.calculateCreationTime();
            block.calculateLatency();
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
        long minTime = blockchainInfo.getTransactions()
                .entrySet().iterator().next().getValue().getTime();
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
