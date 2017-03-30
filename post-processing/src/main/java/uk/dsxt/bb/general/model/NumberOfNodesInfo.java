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

package uk.dsxt.bb.general.model;

import lombok.Data;
import uk.dsxt.bb.general.model.enums.IntensityType;
import uk.dsxt.bb.general.model.enums.TransactionSizeType;

@Data
public class NumberOfNodesInfo {

    private int numberOfNodes;
    private IntensityType intensityType;
    private TransactionSizeType transactionSizeType;
    private int numberOfUnverifiedTransactions;
    private long mediumDistributionTime;
    private long mediumVerificationTime;

    public NumberOfNodesInfo(int numberOfNodes, IntensityType intensityType,
                             TransactionSizeType transactionSizeType,
                             int numberOfUnverifiedTransactions,
                             long mediumDistributionTime, long mediumVerificationTime) {
        this.numberOfNodes = numberOfNodes;
        this.intensityType = intensityType;
        this.transactionSizeType = transactionSizeType;
        this.numberOfUnverifiedTransactions = numberOfUnverifiedTransactions;
        this.mediumDistributionTime = mediumDistributionTime;
        this.mediumVerificationTime = mediumVerificationTime;
    }
}
