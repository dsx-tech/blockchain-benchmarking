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

package uk.dsxt.bb.general.processing;

import uk.dsxt.bb.current.scenario.model.MediumTimeInfo;
import uk.dsxt.bb.general.model.GeneralInfo;
import uk.dsxt.bb.general.model.IntensityInfo;
import uk.dsxt.bb.general.model.SizeInfo;
import uk.dsxt.bb.general.model.enums.*;
import uk.dsxt.bb.current.scenario.model.BlockchainInfo;
import uk.dsxt.bb.current.scenario.model.TransactionInfo;
import uk.dsxt.bb.current.scenario.processing.ResultsAnalyzer;

import java.util.*;

import static java.lang.Math.abs;

public class ResultCombiner {

    private static final long TIME_DIAPASON_FOR_DISPERSION = 2500;
    private static final long NUMBER_OF_SEGMENTS = TIME_DIAPASON_FOR_DISPERSION / ResultsAnalyzer.TIME_INTERVAL;

    private static final int HIGH_INTENSITY_DISPERSION = 50;
    private static final int HIGH_SIZE_DISPERSION = 10;

    private static final int BIG_TRANSACTION_SIZE = 1000;
    private static final int SMALL_TRANSACTION_SIZE = 50;

    private static final int BIG_NUMBER_OF_NODES = 50;
    private static final int SMALL_NUMBER_OF_NODES = 5;

    private static final int STRONG_INTENSITY = 100;
    private static final int WEAK_INTENSITY = 5;

    private BlockchainInfo blockchainInfo;
    private GeneralInfo generalInfo;

    public ResultCombiner(BlockchainInfo blockchainInfo, GeneralInfo generalInfo) {
        this.blockchainInfo = blockchainInfo;
        this.generalInfo = generalInfo;
    }

    public GeneralInfo combine() {
        for (Map.Entry<Long, Integer> element : blockchainInfo.getTimeToIntensities().entrySet()) {
            combineIntensity(element.getKey(), element.getValue());
        }
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            combineSize(transaction.getTime(), transaction.getTransactionSize());
        }
        return generalInfo;
    }

    private void combineSize(long time, int size) {
        //calculate all params corresponding to this size
        SizeDispersionType sizeDispersionType = getSizeDispersionType(time);
        IntensityType intensityType = getIntensityType(time);
        NumberOfNodesType numberOfNodesType = getNumberOfNodesType(blockchainInfo.getNumberOfNodes());
        int numberOfUnverified = blockchainInfo.getTimeToUnverifiedTransactions().
                get(blockchainInfo.getTimeToUnverifiedTransactions().floorKey(time));
        MediumTimeInfo mediumTimeInfo = blockchainInfo.getTimeToMediumTimes().
                get(blockchainInfo.getTimeToMediumTimes().floorKey(time));
        long mediumDistributionTime = mediumTimeInfo.getMediumDstrbTime95();
        long mediumVerifTime = mediumTimeInfo.getMediumVerificationTime();

        SizeInfo sizeInfo = generalInfo.getSizeInfo(size, sizeDispersionType, numberOfNodesType, intensityType);

        if (sizeInfo == null) {
            //add new intensityInfo
            generalInfo.addSizeInfo(new SizeInfo(size, sizeDispersionType, numberOfNodesType, intensityType,
                    numberOfUnverified, mediumDistributionTime, mediumVerifTime));
        } else {
            //recalculate
            long newDistrTime = (sizeInfo.getMediumDistributionTime() + mediumDistributionTime) / 2;
            int newUnverif = (sizeInfo.getNumberOfUnverifiedTransactions() + numberOfUnverified) / 2;
            long newVerTime = (sizeInfo.getMediumVerificationTime() + mediumVerifTime) / 2;
            sizeInfo.setNumberOfUnverifiedTransactions(newUnverif);
            sizeInfo.setMediumDistributionTime(newDistrTime);
            sizeInfo.setMediumVerificationTime(newVerTime);
        }
    }

    private IntensityType getIntensityType(long time) {
        int intensity = blockchainInfo.getTimeToIntensities().
                get(blockchainInfo.getTimeToIntensities().floorKey(time));
        if (intensity > STRONG_INTENSITY) {
            return IntensityType.STRONG;
        }
        if (intensity < WEAK_INTENSITY) {
            return IntensityType.WEAK;
        }
        return IntensityType.MEDIUM;
    }

    private SizeDispersionType getSizeDispersionType(long time) {
        List<TransactionInfo> transactionsInDiapason = new ArrayList<>();
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            if (abs(transaction.getTime() - time) < TIME_DIAPASON_FOR_DISPERSION) {
                transactionsInDiapason.add(transaction);
            }
        }
        transactionsInDiapason.sort(Comparator.comparingLong(TransactionInfo::getTime));
        int numberOfHighDifferences = 0;
        for (int i = 1; i < transactionsInDiapason.size(); i++) {
            if (abs(transactionsInDiapason.get(i - 1).getTransactionSize()
                    - transactionsInDiapason.get(i).getTransactionSize()) > HIGH_SIZE_DISPERSION) {
                numberOfHighDifferences++;
            }
        }
        if (numberOfHighDifferences >= transactionsInDiapason.size() * 0.2) {
            return SizeDispersionType.HIGH;
        }
        return SizeDispersionType.LOW;
    }

    private void combineIntensity(long time, int intensity) {
        //calculate all params corresponding to this intensity
        IntensityDispersionType type = getIntensityDispersion(time);
        TransactionSizeType size = getTransactionSizeType(time);
        NumberOfNodesType numberOfNodesType = getNumberOfNodesType(blockchainInfo.getNumberOfNodes());
        int numberOfUnverified = blockchainInfo.getTimeToUnverifiedTransactions().get(time);
        long mediumDistributionTime = blockchainInfo.getTimeToMediumTimes().get(time).getMediumDstrbTime95();
        long mediumVerifTime = blockchainInfo.getTimeToMediumTimes().get(time).getMediumVerificationTime();
        IntensityInfo intensityInfo = generalInfo.getIntensityInfo(intensity, size, numberOfNodesType, type);

        if (intensityInfo == null) {
            //add new intensityInfo
            generalInfo.addIntensity
                    (new IntensityInfo(intensity, type, numberOfNodesType,
                            size, numberOfUnverified, mediumDistributionTime, mediumVerifTime));
        } else {
            //recalculate old intensityInfo
            int newUnverif = (intensityInfo.getNumberOfUnverifiedTransactions() + numberOfUnverified) / 2;
            long newDistrTime = (intensityInfo.getMediumDistributionTime() + mediumDistributionTime) / 2;
            long newVerTime = (intensityInfo.getMediumVerificationTime() + mediumVerifTime) / 2;
            intensityInfo.setNumberOfUnverifiedTransactions(newUnverif);
            intensityInfo.setMediumDistributionTime(newDistrTime);
            intensityInfo.setMediumVerificationTime(newVerTime);
        }
    }


    private NumberOfNodesType getNumberOfNodesType(int numberOfNodes) {
        if (numberOfNodes > BIG_NUMBER_OF_NODES) {
            return NumberOfNodesType.MANY;
        }
        if (numberOfNodes < SMALL_NUMBER_OF_NODES) {
            return NumberOfNodesType.FEW;
        }
        return NumberOfNodesType.SOME;
    }


    /**
     * @param time
     * @return HIGH dispersion, if more than 20% of differences are higher than HIGH_INTENSITY_DISPERSION
     */

    private IntensityDispersionType getIntensityDispersion(Long time) {
        List<Integer> intensitiesInDiapason = new ArrayList<>();
        for (long i = -NUMBER_OF_SEGMENTS; i <= NUMBER_OF_SEGMENTS; i++) {
            if (blockchainInfo.getTimeToIntensities().containsKey(time + i * ResultsAnalyzer.TIME_INTERVAL)) {
                intensitiesInDiapason.add(blockchainInfo.getTimeToIntensities().
                        get(time + i * ResultsAnalyzer.TIME_INTERVAL));
            }
        }
        int numberOfHighDifferences = 0;
        for (int i = 1; i < intensitiesInDiapason.size(); i++) {
            if (abs(intensitiesInDiapason.get(i - 1) - intensitiesInDiapason.get(i)) > HIGH_INTENSITY_DISPERSION) {
                numberOfHighDifferences++;
            }
        }
        if (numberOfHighDifferences >= intensitiesInDiapason.size() * 0.2) {
            return IntensityDispersionType.HIGH;
        }
        return IntensityDispersionType.LOW;
    }

    /**
     * @param time
     * @return medium transaction size in time diapason
     * (may be change to 95-percentile medium)
     */
    private TransactionSizeType getTransactionSizeType(Long time) {
        List<Integer> sizesInDiapason = new ArrayList<>();
        for (TransactionInfo transaction : blockchainInfo.getTransactions().values()) {
            if (abs(transaction.getTime() - time) < TIME_DIAPASON_FOR_DISPERSION) {
                sizesInDiapason.add(transaction.getTransactionSize());
            }
        }
        int sum = 0;
        for (Integer size : sizesInDiapason) {
            sum += size;
        }
        double mediumSize = sum / sizesInDiapason.size();
        if (mediumSize > BIG_TRANSACTION_SIZE) {
            return TransactionSizeType.BIG;
        }
        if (mediumSize < SMALL_TRANSACTION_SIZE) {
            return TransactionSizeType.SMALL;
        }
        return TransactionSizeType.MIDDLE;
    }
}
