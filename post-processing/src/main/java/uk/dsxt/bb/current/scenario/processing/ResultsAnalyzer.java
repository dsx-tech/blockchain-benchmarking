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
    public static final long TIME_INTERVAL = 500;
    private static final int NUMBER_OF_VERIFICATION = 6;
    private BlockchainInfo blockchainInfo;

    public ResultsAnalyzer(BlockchainInfo blockchainInfo) {
        this.blockchainInfo = blockchainInfo;
    }

    public BlockchainInfo analyze() {
        // change time to counting from zero
        updateTimeFormat();
        blockchainInfo.setTimeToIntensities(calculateIntensity());
        // calculate creation time and times of distribution
        updateBlockInfos();
        blockchainInfo.setTimeToUnverifiedTransactions(countUnverifiedTransactions());
        blockchainInfo.setTimeToMediumTimes(calculateDistributionTimes());
        return blockchainInfo;
    }

    private NavigableMap<Long, MediumTimeInfo> calculateDistributionTimes() {
        NavigableMap<Long, MediumTimeInfo> timeToDistributions = new TreeMap<>();
        if (blockchainInfo.getBlocks().isEmpty()) {
            log.error("No blocks found");
            return timeToDistributions;
        }
        //add all possible time intervals
        long startTime = 0;
        long endTime = getEndTime();
        for (long i = startTime; i < endTime; i += TIME_INTERVAL) {
            timeToDistributions.put(i, new MediumTimeInfo());
        }
        //for each block find correct time map and recalculate medium distribution times
        for (BlockInfo block : blockchainInfo.getBlocks().values()) {
            MediumTimeInfo distr = timeToDistributions.get
                    (timeToDistributions.floorKey(block.getCreationTime()));
            int numberOfBlocks = distr.getNumberOfBlocks();
            distr.setMediumDstrbTime95
                    (recalculateMediumValue(numberOfBlocks,
                            distr.getMediumDstrbTime95(),
                            block.getDistributionTime95()));
            distr.setMediumDstrbTime100
                    (recalculateMediumValue(numberOfBlocks,
                            distr.getMediumDstrbTime100(),
                            block.getDistributionTime100()));
            distr.setMediumVerificationTime
                    (recalculateMediumValue(numberOfBlocks,
                            distr.getMediumVerificationTime(),
                            block.getVerificationTime()));
            distr.setNumberOfBlocks(numberOfBlocks + 1);
        }
        return timeToDistributions;
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

    //transaction is verified when block with it is verified
    private NavigableMap<Long, Integer> countUnverifiedTransactions() {
        NavigableMap<Long, Integer> numUnverifiedTrans = new TreeMap<>();
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
                log.error("Couldn't find block by id: " + transaction.getBlockId());
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
            block.calculateCreationTime();
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
        return nextBlock.getDistributionTime100() + nextBlock.getCreationTime();
    }

    private NavigableMap<Long, Integer> calculateIntensity() {
        NavigableMap<Long, Integer> intensities = new TreeMap<>();
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
            long timeInMap = intensities.floorKey(time);
            intensities.replace(timeInMap, intensities.get(timeInMap) + 1);
        }
        return intensities;
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
