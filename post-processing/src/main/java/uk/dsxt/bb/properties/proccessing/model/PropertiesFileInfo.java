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
package uk.dsxt.bb.properties.proccessing.model;

import lombok.Data;

@Data
public class PropertiesFileInfo {

    private String pathToScenarioDir;
    private int numOfThreads;
    private int minMessageLength;
    private int maxMessageLength;
    private int numOfNodes;
    private long transactionDelay;
    private BlockchainType blockchainType;
    private int loadGeneratorsAmount;

    public PropertiesFileInfo(String pathToScenarioDir, int numOfThreads,
                              int minMessageLength, int maxMessageLength,
                              int numOfNodes, long transactionDelay,
                              BlockchainType blockchainType, int loadGeneratorsAmount) {
        this.pathToScenarioDir = pathToScenarioDir;
        this.numOfThreads = numOfThreads;
        this.minMessageLength = minMessageLength;
        this.maxMessageLength = maxMessageLength;
        this.numOfNodes = numOfNodes;
        this.transactionDelay = transactionDelay;
        this.blockchainType = blockchainType;
        this.loadGeneratorsAmount = loadGeneratorsAmount;
    }

    public boolean equalsExceptIntensity(PropertiesFileInfo file) {
        return file.getLoadGeneratorsAmount() == loadGeneratorsAmount
                && file.getMaxMessageLength() == maxMessageLength
                && file.getMinMessageLength() == minMessageLength
                && file.getNumOfThreads() == numOfThreads
                && file.getNumOfNodes() == numOfNodes;
    }

    public boolean equalsExceptSize(PropertiesFileInfo file) {
        return file.getLoadGeneratorsAmount() == loadGeneratorsAmount
                && file.getTransactionDelay() == transactionDelay
                && file.getNumOfThreads() == numOfThreads
                && file.getNumOfNodes() == numOfNodes;
    }

    public boolean equalsFully(PropertiesFileInfo file) {
        return file.getLoadGeneratorsAmount() == loadGeneratorsAmount
                && file.getMaxMessageLength() == maxMessageLength
                && file.getMinMessageLength() == minMessageLength
                && file.getTransactionDelay() == transactionDelay
                && file.getNumOfThreads() == numOfThreads
                && file.getNumOfNodes() == numOfNodes;
    }
}
