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

import uk.dsxt.bb.general.model.GeneralInfo;
import uk.dsxt.bb.general.model.IntensityInfo;
import uk.dsxt.bb.general.model.enums.IntensityDispersionType;
import uk.dsxt.bb.general.model.enums.NumberOfNodesType;
import uk.dsxt.bb.general.model.enums.TransactionSizeType;
import uk.dsxt.bb.model.BlockchainInfo;
import uk.dsxt.bb.model.TransactionInfo;
import uk.dsxt.bb.processing.ResultsAnalyzer;

import java.util.*;

import static java.lang.Math.abs;

public class ResultCombiner {

    private static final long TIME_DIAPASON_FOR_DISPERSION = 2500;
    private static final long NUMBER_OF_SEGMENTS = TIME_DIAPASON_FOR_DISPERSION / ResultsAnalyzer.TIME_INTERVAL;

    private static final int HIGH_DISPERSION = 500;

    private static final int BIG_TRANSACTION_SIZE = 1000;
    private static final int SMALL_TRANSACTION_SIZE = 10;

    private static final int BIG_NUMBER_OF_NODES = 50;
    private static final int SMALL_NUMDER_OF_NODES = 5;

    public static GeneralInfo combine(BlockchainInfo blockchainInfo, GeneralInfo generalInfo) {
        for (Map.Entry<Long, Integer> element : blockchainInfo.getTimeToIntensities().entrySet()) {
            long time = element.getKey();
            int intensity = element.getValue();
            //calculate all params corresponding to this intensity
            IntensityDispersionType type = getIntensityDispersion(blockchainInfo.getTimeToIntensities(), time);
            TransactionSizeType size = getTransactionSizeType(blockchainInfo.getTransactions().values(), time);
            NumberOfNodesType numberOfNodesType = getNumberOfNodesType(blockchainInfo.getNumberOfNodes());
            int numberOfUnverified = blockchainInfo.getTimeToUnverifiedTransactions().get(time);
            long mediumDistributionTime = blockchainInfo.getTimeToDistributionTimes().get(time).getMediumDstrbTime95();
            long mediumVerifTime = -1;
            IntensityInfo intensityInfoInfo = generalInfo.getIntensityInfo(intensity, size, numberOfNodesType, type);

            if (intensityInfoInfo == null) {
                //add new intensityInfo
                generalInfo.addIntensity
                        (new IntensityInfo(intensity, type, numberOfNodesType,
                                size, numberOfUnverified, mediumDistributionTime, mediumVerifTime));
            } else {
                //recalculate old intensityInfo
                int newUnverif = (intensityInfoInfo.getNumberOfUnverifiedTransactions() + numberOfUnverified) / 2;
                long newDistrTime = (intensityInfoInfo.getMediumDistributionTime() + mediumDistributionTime) / 2;
                long newVerTime = (intensityInfoInfo.getMediumVerificationTime() + mediumVerifTime) / 2;
                intensityInfoInfo.setNumberOfUnverifiedTransactions(newUnverif);
                intensityInfoInfo.setMediumDistributionTime(newDistrTime);
                intensityInfoInfo.setMediumVerificationTime(newVerTime);
            }
        }
        return generalInfo;
    }


    private static NumberOfNodesType getNumberOfNodesType(int numberOfNodes) {
        if (numberOfNodes > BIG_NUMBER_OF_NODES) {
            return NumberOfNodesType.MANY;
        }
        if (numberOfNodes < SMALL_NUMDER_OF_NODES) {
            return NumberOfNodesType.FEW;
        }
        return NumberOfNodesType.SOME;
    }


    /**
     * @param timeToIntensities
     * @param time
     * @return HIGH dispersion, if more than 20% of differences are higher than HIGH_DISPERSION
     */

    private static IntensityDispersionType getIntensityDispersion
    (NavigableMap<Long, Integer> timeToIntensities, Long time) {
        List<Integer> intensitiesInDiapason = new ArrayList<>();
        for (long i = -NUMBER_OF_SEGMENTS; i <= NUMBER_OF_SEGMENTS; i++) {
            if (timeToIntensities.containsKey(time + i * ResultsAnalyzer.TIME_INTERVAL)) {
                intensitiesInDiapason.add(timeToIntensities.get(time + i * ResultsAnalyzer.TIME_INTERVAL));
            }
        }
        int numberOfHighDifferences = 0;
        for (int i = 1; i < intensitiesInDiapason.size(); i++) {
            if (abs(intensitiesInDiapason.get(i - 1) - intensitiesInDiapason.get(i)) > HIGH_DISPERSION) {
                numberOfHighDifferences++;
            }
        }
        if (numberOfHighDifferences >= intensitiesInDiapason.size() * 0.2) {
            return IntensityDispersionType.HIGH;
        }
        return IntensityDispersionType.LOW;
    }

    /**
     * @param transactions
     * @param time
     * @return medium transaction size in time diapason
     * (may be change to 95-percentile medium)
     */
    private static TransactionSizeType getTransactionSizeType
    (Collection<TransactionInfo> transactions, Long time) {
        List<Integer> sizesInDiapason = new ArrayList<>();
        for (TransactionInfo transaction : transactions) {
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
