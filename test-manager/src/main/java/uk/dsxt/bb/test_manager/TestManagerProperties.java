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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author phd
 */
@Getter
@Builder
public class TestManagerProperties {
    final private String pemKeyPath;
    final private String instancesPath;
    final private Path blockchainInitResourcesPath;
    final private Path testManagerModulesPath;
    final private int amountOfTransactionsPerTarget;
    final private int amountOfThreadsPerTarget;
    final private int delayBeetweenRequests;
    final private int minMessageLength;
    final private int maxMessageLength;
    final private String blockchainType;
    final private String fileToLogBlocks;
    final private int requestBlocksPeriod;
    final private String chaincodeFile;
    final private int blockchainInstancesAmount;
    final private int loadGeneratorInstancesAmount;
    final private String resultLogsPath;
    final private String userNameOnRemoteInstances;
    final private String masterIpAddress;
    final private int masterPort;
    final private int testTimeout;
    final private String modulesInitResourcesPath;
    final private int afterBlockchainInitTimeout;
    final private Path loadGeneratorConfigPath;
    final private String blockchainConfigPath;

    public static TestManagerProperties fromProperties(Properties properties) {
        return TestManagerProperties.builder()
                .resultLogsPath(properties.getProperty("log.path"))
                .pemKeyPath(properties.getProperty("pem.key.path"))
                .instancesPath(properties.getProperty("instances.path"))
                .blockchainInitResourcesPath(Paths.get(properties.getProperty("blockchain.init.resources")))
                .testManagerModulesPath(Paths.get(properties.getProperty("test_manager.modules.path")))
                .amountOfTransactionsPerTarget(convertToInt(properties.getProperty("amount.transactions.per.target")))
                .amountOfThreadsPerTarget(convertToInt(properties.getProperty("amount.threads.per.target")))
                .minMessageLength(convertToInt(properties.getProperty("message.length.min")))
                .maxMessageLength(convertToInt(properties.getProperty("message.length.max")))
                .blockchainType(properties.getProperty("blockchain.type"))
                .fileToLogBlocks(properties.getProperty("file.log.blocks"))
                .requestBlocksPeriod(convertToInt(properties.getProperty("request.blocks.period")))
                .blockchainInstancesAmount(convertToInt(properties.getProperty("blockchain.instances.amount")))
                .loadGeneratorInstancesAmount(convertToInt(properties.getProperty("load_generator.instances.amount")))
                .loadGeneratorConfigPath(Paths.get(properties.getProperty("load_generator.load_config")))
                .delayBeetweenRequests(convertToInt(properties.getProperty("message.delay")))
                .userNameOnRemoteInstances(properties.getProperty("remote.instance.user.name"))
                .masterIpAddress(properties.getProperty("test_manager.ip"))
                .masterPort(convertToInt(properties.getProperty("test_manager.port")))
                .testTimeout(convertToInt(properties.getProperty("test_manager.test_timeout")))
                .modulesInitResourcesPath(properties.getProperty("modules.init.resources"))
                .afterBlockchainInitTimeout(convertToInt(properties.getProperty("test_manager.after_blockchain_init.timeout")))
                .blockchainConfigPath(properties.getProperty("blockchain.config.path"))
                .build();
    }

    private static int convertToInt(String potentialInt) {
        return Integer.valueOf(potentialInt);
    }
}
