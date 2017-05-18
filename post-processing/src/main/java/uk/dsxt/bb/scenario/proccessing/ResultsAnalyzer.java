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
    private static final long FIXED_TIME_INTERVAL = 360000; // 6 minutes
    private static final int NUMBER_OF_TIME_SEGMENTS = 25;
    private BlockchainInfo blockchainInfo;
    private PropertiesFileInfo properties;

    public ResultsAnalyzer(BlockchainInfo blockchainInfo, PropertiesFileInfo propertiesFileInfo) {
        this.properties = propertiesFileInfo;
        this.blockchainInfo = blockchainInfo;
    }

    public BlockchainInfo analyze() {
        if (blockchainInfo.getTransactions().isEmpty() || blockchainInfo.getBlocks().isEmpty()
                || blockchainInfo.getResources().isEmpty()) {
            return null;
        }
        // change time to counting from zero
        updateTimeFormat();
        //calculate timeInterval
        timeInterval = FIXED_TIME_INTERVAL;
//                (getEndTime() - getStartTime()) / NUMBER_OF_TIME_SEGMENTS;
        // calculate creation time and times of distribution
        updateBlockInfos();
        calculateTransactionLatencies();
        blockchainInfo.setTimeSegments(calculateTimeSegments());
        blockchainInfo.setScenarioInfo(new ScenarioInfo(blockchainInfo, properties));
        blockchainInfo.setResources(cutTestTimeSegment());
        convertAllTimesToSeconds();
        return blockchainInfo;
    }

    private void convertAllTimesToSeconds() {
        for (TimeSegmentInfo time :
                blockchainInfo.getTimeSegments().values()) {
            time.setLatency(convertTimeToSeconds((long) time.getLatency()));
            time.setTime((long) convertTimeToSeconds(time.getTime()));
        }
        for (TransactionInfo transactionInfo : blockchainInfo.getTransactions().values()) {
            if (transactionInfo.getLatency() == Double.POSITIVE_INFINITY) {
                continue;
            }
            transactionInfo.setLatency(convertTimeToSeconds((long) transactionInfo.getLatency()));
        }

    }

    private double convertTimeToSeconds(long time) {
        return time / 1000;
    }

    private double calcMedium
            (int numberOfElements, double prevMediumValue, double newValue) {
        return ((prevMediumValue * numberOfElements) + newValue) / (numberOfElements + 1);
    }

    private void calculateTransactionLatencies() {
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            transaction.calculateLatency(blockchainInfo);
        }
    }

    private List<ResourceInfo> cutTestTimeSegment() {
        final long endTime = getEndTime();
        List<ResourceInfo> list = new ArrayList<>();
        for (ResourceInfo resourceInfo : blockchainInfo.getResources()) {
            if ((resourceInfo.getTime() >= 0
                    && resourceInfo.getTime() <= endTime)) {
                resourceInfo.setMem(convertBytesToMb(resourceInfo.getMem()));
                resourceInfo.setDownloaded(convertBytesToMb(resourceInfo.getDownloaded()));
                resourceInfo.setUploaded(convertBytesToMb(resourceInfo.getUploaded()));
                resourceInfo.setTime((long) convertTimeToSeconds(resourceInfo.getTime()));
                list.add(resourceInfo);
            }
        }
        return list;
    }

    private double convertBytesToMb(double bytes) {
        return bytes / (1024 * 1024);
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
        for (ResourceInfo resource : blockchainInfo.getResources()) {
            resource.setTime(resource.getTime() - startTime);
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
        //fillTimeSegmentInfo blockLatency, throughput, numberOfTransactionsInBlock and blockSize fields from blockInfo general
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            double time = block.getCreationTime() + block.getLatency();
            if ((long) time < startTime) {
                log.error("Time out of range: " + time + " in block " + block.getBlockId());
                continue;
            }
            long floorTime = timeSegments.floorKey((long) time);
            TimeSegmentInfo timeSegmentInfo = timeSegments.get(floorTime);
            fillTimeSegmentInfo(timeSegmentInfo, block);
        }
        //calculate intensity and throughput
        for (TimeSegmentInfo time : timeSegments.values()) {
            time.setIntensity((1000 * time.getNumberOfTransactionsGenerated()) / timeInterval);
            time.setThroughput((1000 * time.getNumberOfTransactionsIntegrated()) / timeInterval);
            time.setBlockGenerationFrequency((60 * 1000 * time.getNumberOfBlocks()) / timeInterval);
            if (time.getNumberOfTransactionsIntegrated() == 0) {
                time.setTransactionSize(0f);
                time.setLatency(-1);
                continue;
            }
            time.setTransactionSize(time.getTransactionSize() / time.getNumberOfTransactionsIntegrated());
            time.setLatency(time.getLatency() / time.getNumberOfTransactionsIntegrated());
        }
        fillQueue(timeSegments);
        return timeSegments;
    }

    private void fillTimeSegmentInfo(TimeSegmentInfo time, BlockInfo block) {
        int numberOfBlocks = time.getNumberOfBlocks();
        //recalculate medium blockLatency
        time.setBlockLatency(calcMedium(numberOfBlocks, time.getBlockLatency(), block.getLatency()));
        //recalculate medium number of transactions in block
        time.setNumberTransactionsInBlock(
                (int) calcMedium(numberOfBlocks,
                        time.getNumberTransactionsInBlock(), block.getTransactions().size()));
        //recalculate medium block size
        time.setBlockSize(calcMedium(numberOfBlocks, time.getBlockSize(), block.getSize()));
        //recalculate throughput
        time.setNumberOfTransactionsIntegrated(time.getNumberOfTransactionsIntegrated()
                + block.getTransactions().size());
        //recalculate medium transaction size
        time.setTransactionSize(time.getTransactionSize() + block.getSize());
        time.setLatency(time.getLatency() + calculateSumOfLatencies(block));
        time.setNumberOfBlocks(numberOfBlocks + 1);
    }

    private double calculateSumOfLatencies(BlockInfo block) {
        double sum = 0.0;
        for (TransactionInfo transactionInfo : block.getTransactions()) {
            double latency = transactionInfo.getLatency();
            if (latency > 0) {
                sum += latency;
            }
        }
        return sum;
    }

    private void fillQueue(Map<Long, TimeSegmentInfo> timeSegments) {
        int numberGenerated = 0;
        int numberIntegrated = 0;
        for (TimeSegmentInfo time : timeSegments.values()) {
            numberGenerated += time.getNumberOfTransactionsGenerated();
            numberIntegrated += time.getNumberOfTransactionsIntegrated();
            time.setTransactionQueueLength(numberGenerated - numberIntegrated);
        }
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
            timeSegment.setNumberOfTransactionsGenerated
                    (timeSegment.getNumberOfTransactionsGenerated() + 1);
        }
    }

    /**
     * calculate block creation time and blockLatency
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
