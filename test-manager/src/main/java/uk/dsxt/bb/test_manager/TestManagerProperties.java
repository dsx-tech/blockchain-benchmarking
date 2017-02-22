/*
 ******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2016 DSX Technologies Limited.                               *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************
 */
package uk.dsxt.bb.test_manager;

import lombok.Builder;
import lombok.Getter;

/**
 * @author phd
 */
@Getter
@Builder
public class TestManagerProperties {
    final private String pemKeyPath;
    final private String pathToBlockchainResources;
    final private int amountOfTransactionsPerTarget;
    final private int amountOfThreadsPerTarget;
    final private int delayBeetweenRequests;
    final private int minMessageLength;
    final private int maxMessageLength;
    final private String blockchainType;
    final private String fileToLogBlocks;
    final private int requestPeriod;
    final private String chaincodeFile;
    final private int blockchainInstancesAmount;
    final private int loadGeneratorInstancesAmount;
    final private String deployLogPath;
    final private String userNameOnRemoteInstances;
    final private String masterIpAddress;
    final private int masterPort;
}
