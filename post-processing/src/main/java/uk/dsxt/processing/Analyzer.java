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
package uk.dsxt.processing;

import lombok.extern.log4j.Log4j2;
import uk.dsxt.model.*;

import java.util.*;

@Log4j2
public class Analyzer {

    private BlockchainInfo blockchainInfo;

    //note: time is counted from the start of the test in milliseconds
    private static final long TIME_INTERVAL = 5000;
    private static final int NUMBER_OF_VERIFICATION = 6;

    public Analyzer(BlockchainInfo blockchainInfo) {
        this.blockchainInfo = blockchainInfo;
    }

    public BlockchainInfo analyze() {
        blockchainInfo.setTimeToIntensities(calculateIntensity());
        updateTransactionInfos();
        updateBlockInfos();
        updateNodeInfos();
        blockchainInfo.setTimeToNumNodes(calculateNumberOfNodes());
        blockchainInfo.setTimeToUnverifiedTransactions(calculateUnverifiedTransactions());
        blockchainInfo.setTimeInfos(calculateTimeInfos());
        return blockchainInfo;
    }


    private static final int NUMBER_OF_SIZES = 3;

    private NavigableMap<Long, NavigableMap<Integer, TimeInfo>> calculateTimeInfos() {
        NavigableMap<Long, NavigableMap<Integer, TimeInfo>> timeInfos = new TreeMap<>();
        if (blockchainInfo.getBlocks().isEmpty()) {
            log.error("No blocks found");
            return timeInfos;
        }
        int minBlockSize = blockchainInfo.getBlocks().values().iterator().next().getSize();
        int maxBlockSize = 0;
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            if (minBlockSize > block.getSize()) {
                minBlockSize = block.getSize();
            }
            if (maxBlockSize < block.getSize()) {
                maxBlockSize = block.getSize();
            }
        }
        //add all possible time intervals
        long startTime = 0;
        long endTime = getEndTime();
        for (long i = startTime; i < endTime; i += TIME_INTERVAL) {
            timeInfos.put(i, new TreeMap<>());
        }
        //add all size spans to every map
        int step = (maxBlockSize - minBlockSize) / NUMBER_OF_SIZES;
        for (Map.Entry<Long, NavigableMap<Integer, TimeInfo>> timeInfo : timeInfos.entrySet()) {
            for (int i = 0; i < NUMBER_OF_SIZES; i++) {
                int startSize =minBlockSize + i * step;
                timeInfo.getValue().put(startSize, new TimeInfo
                        (new TimeInfo.TimeAndSize(timeInfo.getKey(), new TimeInfo.SizeSpan
                                (startSize, startSize + step))));
            }
        }
        //for each block find correct time and size map and recalculate medium distribution times
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            NavigableMap<Integer, TimeInfo> timeInfoInside = timeInfos.get(timeInfos.floorKey(block.getCreationTime()));
            TimeInfo t = timeInfoInside.get(timeInfoInside.floorKey(block.getSize()));
            t.setMediumDstrbTime95(((t.getMediumDstrbTime95() * t.getNumberOfBlocks() + block.getDistributionTime95())
                    / (t.getNumberOfBlocks() + 1)));
            t.setMediumDstrbTime100(((t.getMediumDstrbTime100() * t.getNumberOfBlocks() + block.getDistributionTime100())
                    / (t.getNumberOfBlocks() + 1)));
            t.setNumberOfBlocks(t.getNumberOfBlocks() + 1);
        }
        return timeInfos;

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
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            long creationTime = transaction.getTime();
            BlockInfo block = blockchainInfo.getBlocks().get(transaction.getBlockId());
            if (block == null) {
                log.error("couldn't find block by id: " + transaction.getBlockId());
                //ignore all problems
                continue;
            }
            long verificationTime = block.getVerificationTime();
            if (verificationTime == -1) {
                verificationTime = endTime;
            }
            for (Long time : numUnverifiedTrans.keySet()) {
                if (creationTime <= time && time <= verificationTime) {
                    numUnverifiedTrans.replace(time, numUnverifiedTrans.get(time) + 1);
                }
            }
        }
        return numUnverifiedTrans;
    }


    //calculate maxTime and verificationTime
    private void updateBlockInfos() {
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            block.calculateMaxTime();
        }
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            block.setVerificationTime(calculateVerificationTime(block));
        }

        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            int size = 0;
            for (TransactionInfo transaction : block.getTransactions()) {
                size += transaction.getTransactionSize();
            }
            block.setSize(size);
        }
    }

    //block is verified when 6 blocks after it have been created

    /**
     * @return -1 if block wasn't verified
     **/
    private long calculateVerificationTime(BlockInfo blockInfo) {
        BlockInfo nextBlock = blockInfo;
        for (int i = 0; i < NUMBER_OF_VERIFICATION; i++) {
            nextBlock = blockchainInfo.getChildBlockById(nextBlock.getBlockId());
            if (nextBlock == null) {
                return -1;
            }
        }
        return nextBlock.getDistributionTime100();
    }

    //adding blockId to transactionInfo
    private void updateTransactionInfos() {
        for (BlockInfo blockInfo : blockchainInfo.getBlocks().values()) {
            for (TransactionInfo transaction : blockInfo.getTransactions()) {
                transaction.setBlockId(blockInfo.getBlockId());
            }
        }
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
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            long time = transaction.getTime();
            while (time > 0 && !intensities.containsKey(time)) {
                time--;
            }
            if (time < 0) {
                log.error("incorrect time: " + transaction.getTime());//ignoring all incorrect lines
                continue;
            }
            intensities.replace(time, intensities.get(time) + 1);
        }
        return intensities;
    }

    private Map<Long, Integer> calculateNumberOfNodes() {
        Map<Long, Integer> numOfNodes = new HashMap<>();
        long startTime = 0;
        long endTime = getEndTime();
        //add all intervals to list
        for (long i = startTime; i < endTime; i += TIME_INTERVAL) {
            numOfNodes.put(i, 0);
        }
        for (Long time : numOfNodes.keySet()) {
            for (NodeInfo node : blockchainInfo.getNodes()) {
                for (NodeInfo.TimeSpan workTime : node.getWorkTimes()) {
                    if (workTime.getStartTime() < time && time < workTime.getEndTime()) {
                        numOfNodes.replace(time, numOfNodes.get(time) + 1);
                        break;
                    }
                }
            }
        }
        return numOfNodes;
    }

    private void updateNodeInfos() {
        for (NodeInfo node : blockchainInfo.getNodes()) {
            List<Long> startTimes = node.getStartTimes();
            List<Long> stopTimes = node.getStopTimes();
            startTimes.sort(Long::compareTo);
            stopTimes.sort(Long::compareTo);
            if (startTimes.size() != stopTimes.size()) {
                log.error("Start and stop times of node " + node.getNodeId() + " doesn't correspond");
                continue;
            }
            for (int i = 0; i < startTimes.size(); i++) {
                node.addWorkTime(new NodeInfo.TimeSpan(startTimes.get(i), stopTimes.get(i)));
            }
            //todo check here that time spans don't overlap
        }
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
