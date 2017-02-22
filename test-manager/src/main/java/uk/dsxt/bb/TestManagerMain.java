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

package uk.dsxt.bb;

import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.test_manager.TestManager;
import uk.dsxt.bb.test_manager.TestManagerProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author phd
 */
@Log4j2
public class TestManagerMain {
    private final static String userNameOnRemoteInstance = "ec2-user";
    private final static String pathToBlockchainResources = "hyperledger";

    public static void main(String[] args) throws Exception {
        Path logPath = Paths.get(pathToBlockchainResources, "log");
        if (logPath.toFile().exists()) {
            logPath.toFile().delete();
        }
        logPath.toFile().mkdir();

        TestManagerProperties testManagerProperties = TestManagerProperties
                .builder()
                .pemKeyPath(args[0])
                .amountOfTransactionsPerTarget(Integer.parseInt(args[1]))
                .amountOfThreadsPerTarget(Integer.parseInt(args[2]))
                .minMessageLength(Integer.parseInt(args[3]))
                .maxMessageLength(Integer.parseInt(args[4]))
                .blockchainType(args[5])
                .fileToLogBlocks(args[6])
                .requestPeriod(Integer.parseInt(args[7]))
                .chaincodeFile(args[8])
                .blockchainInstancesAmount(Integer.parseInt(args[9]))
                .loadGeneratorInstancesAmount(Integer.parseInt(args[10]))
                .delayBeetweenRequests(Integer.parseInt(args[11]))
                .pathToBlockchainResources(pathToBlockchainResources)
                .userNameOnRemoteInstances(userNameOnRemoteInstance)
                .deployLogPath(logPath.toAbsolutePath().toFile().toString())
                .masterIpAddress(args[12])
                .masterPort(8080)
                .build();

        List<String> allHosts = Files.readAllLines(Paths.get(pathToBlockchainResources, "instances"));
        TestManager testManager = new TestManager(allHosts, testManagerProperties);
        testManager.start();
    }
}
