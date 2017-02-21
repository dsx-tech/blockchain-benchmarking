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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.remote.instance.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;

/**
 * @author phd
 */
@Log4j2
public class TestManagerMain {
    private final static String userNameOnRemoteInstance = "ec2-user";
    private final static String prefix = "hyperledger";

    public static void main(String[] args) throws Exception {
        String pemKeyPath = args[0];
        int amountOfTransactions = Integer.parseInt(args[1]);
        int amountOfThreadsPerTarget = Integer.parseInt(args[2]);
        int minMessageLength = Integer.parseInt(args[3]);
        int maxMessageLength = Integer.parseInt(args[4]);
        String blockchainType = args[5];
        String fileToLogBlocks = args[6];
        int requestPeriod = Integer.parseInt(args[7]);
        String chaincodeFile = args[8];
        final int blockchainInstancesAmount = Integer.parseInt(args[9]);
        final int loadGeneratorInstancesAmount = Integer.parseInt(args[10]);
        final int delay = Integer.parseInt(args[11]);

        Path logPath = Paths.get(prefix, "log");
        log.debug("pem key path: {}", pemKeyPath);
        if (!Files.exists(Paths.get(prefix, "instances"))) {
            log.error("File \"instances\" doesn't exists");
            return;
        }
        List<String> allHosts = Files.readAllLines(Paths.get(prefix, "instances"));

        List<RemoteInstance> blockchainInstances = allHosts.subList(0, blockchainInstancesAmount)
                .stream()
                .map(host -> new RemoteInstance(userNameOnRemoteInstance, host, 22, pemKeyPath, logPath))
                .collect(Collectors.toList());

        List<LoadGeneratorInstance> loadGeneratorInstances = new ArrayList<>();
        for (int i = 0; i < loadGeneratorInstancesAmount; ++i) {
            loadGeneratorInstances.add(new LoadGeneratorInstance(userNameOnRemoteInstance,
                    allHosts.get(i + blockchainInstancesAmount), 22, pemKeyPath, logPath,
                    amountOfTransactions, amountOfThreadsPerTarget, minMessageLength, maxMessageLength, delay));
        }
        runBlockchain(blockchainInstances, chaincodeFile);
        Thread.sleep(10000);
        List<LoggerInstance> loggerInstances = new ArrayList<>();
        blockchainInstances.forEach(i -> loggerInstances.add(new LoggerInstance(
                userNameOnRemoteInstance, i.getHost(), 22, pemKeyPath, logPath,
                blockchainType, i.getHost(), fileToLogBlocks, requestPeriod)));
        runLoggers(loggerInstances);
        Thread.sleep(10000);
        runLoadGenerators(loadGeneratorInstances, blockchainInstances);
    }

    private static void runBlockchain(List<RemoteInstance> blockchainInstances, String chaincodeFile) throws Exception {
        RemoteInstancesManager<RemoteInstance> blockchainInstancesManager = new RemoteInstancesManager<>();
        blockchainInstancesManager.setRootInstance(blockchainInstances.get(0));
        blockchainInstancesManager.addCommonInstances(blockchainInstances.subList(1, blockchainInstances.size()));

        //String chaincodeFile = "chaincode_example02";
//        String chaincodeFile = "e-voting";

        blockchainInstancesManager.uploadFilesForAll(Arrays.asList(
                Paths.get(prefix, chaincodeFile),
                Paths.get(prefix, "initEnv.sh")
        ));

        blockchainInstancesManager.executeCommandsForAll(singletonList("bash initEnv.sh"));

        List<Path> rootFiles = new ArrayList<>();
        rootFiles.add(Paths.get(prefix, "root", "docker-compose.yml"));
        rootFiles.add(Paths.get(prefix, "root", "startRoot.sh"));
        blockchainInstancesManager.uploadFilesForRoot(rootFiles);
        blockchainInstancesManager.executeCommandsForRoot(singletonList(
                "bash startRoot.sh && touch startRoot.complete")
        );

        List<Path> commonFiles = new ArrayList<>();
        commonFiles.add(Paths.get(prefix, "common", "docker-compose.yml"));
        commonFiles.add(Paths.get(prefix, "common", "startCommon.sh"));
        blockchainInstancesManager.uploadFilesForCommon(commonFiles);
        blockchainInstancesManager.executeCommandsForCommon(Collections.singletonList(
                "bash startCommon.sh && touch startCommon.complete")
        );

        sleep(20000);
        blockchainInstancesManager.executeCommandsForAll(singletonList("CORE_CHAINCODE_ID_NAME=mycc " +
                "CORE_PEER_ADDRESS=0.0.0.0:7051 " +
                "nohup ./" + chaincodeFile + " >/dev/null 2>chaincode.log &")
        );
        log.info("Blockchain instances started");
        sleep(3000);
        HttpResponse<JsonNode> response = Unirest.post(
                "http://" + blockchainInstancesManager.getRootInstance().getHost() + ":7050/chaincode").body("{\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"method\": \"deploy\",\n" +
                "  \"params\": {\n" +
                "    \"type\": 1,\n" +
                "    \"chaincodeID\":{\n" +
                "        \"name\": \"mycc\"\n" +
                "    },\n" +
                "    \"CtorMsg\": {\n" +
                "        \"args\":[\"init\"]\n" +
                "    }\n" +
                "  },\n" +
                "  \"id\": 1\n" +
                "}").asJson();
        log.info("chaincode deployed: {}", response.getBody());
        blockchainInstancesManager.stop();
    }

    private static void runLoggers(List<LoggerInstance> loggers) {
        log.debug("runLoggers start");
        LoggerInstancesManager loggerInstancesManager = new LoggerInstancesManager();
        loggerInstancesManager.setRootInstance(loggers.get(0));
        loggerInstancesManager.addCommonInstances(loggers.subList(1, loggers.size()));
        loggerInstancesManager.uploadFilesForAll(singletonList(Paths.get(prefix, "blockchain-logger.jar")));
        loggerInstancesManager.executeCommandsForAll(Arrays.asList(
                "pkill -f 'java -jar'",
                "sudo yum -y install java-1.8.0",
                "nohup java8 -jar blockchain-logger.jar ${LOG_PARAMS} >/dev/null 2>blockchain-logger-stdout.log &"
        ));

        log.debug("runLoggers stop");
        loggerInstancesManager.stop();
    }

    private static void runLoadGenerators(List<LoadGeneratorInstance> loadGeneratorInstances,
                                          List<RemoteInstance> blockchainInstances) throws Exception {
        log.debug("runLoadGenerators start");
        if (loadGeneratorInstances.isEmpty()) {
            log.error("loadGenerators is absent");
            return;
        }
        List<String> blockchainHosts = blockchainInstances
                .stream()
                .map(RemoteInstance::getHost)
                .collect(Collectors.toList());

        while (!blockchainHosts.isEmpty()) {
            for (LoadGeneratorInstance loadGeneratorInstance : loadGeneratorInstances) {
                if (!blockchainHosts.isEmpty()) {
                    loadGeneratorInstance.getLoadTargets().add(blockchainHosts.remove(0));
                }
            }
        }

        LoadGeneratorInstancesManager loadGeneratorsManager = new LoadGeneratorInstancesManager();
        loadGeneratorsManager.setRootInstance(loadGeneratorInstances.get(0));
        loadGeneratorsManager.addCommonInstances(loadGeneratorInstances.subList(1, loadGeneratorInstances.size()));

        loadGeneratorsManager.executeCommandsForAll(Arrays.asList(
                "pkill -f 'java -jar'",
                "sudo yum -y install java-1.8.0")
        );
        loadGeneratorsManager.uploadFilesForAll(singletonList(Paths.get(prefix, "load-generator.jar")));

        loadGeneratorsManager.executeCommandsForAll(singletonList(
                "nohup java8 -jar load-generator.jar ${LOAD_PARAMS} >/dev/null 2>load-generator-stdout.log &"));

        log.debug("runLoadGenerators end");
        loadGeneratorsManager.stop();
    }
}
