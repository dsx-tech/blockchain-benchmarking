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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import lombok.extern.log4j.Log4j2;
import spark.Spark;
import uk.dsxt.bb.blockchain.BlockchainManager;
import uk.dsxt.bb.datamodel.blockchain.BlockchainTransaction;
import uk.dsxt.bb.remote.instance.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static java.net.HttpURLConnection.*;
import static java.util.Collections.singletonList;

/**
 * @author phd
 */
@Log4j2
public class TestManager {

    private List<String> allHosts;
    private ConcurrentHashMap<String, LoggerInstance> loggerInstances = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, LoadGeneratorInstance> loadGeneratorInstances = new ConcurrentHashMap<>();

    private TestManagerProperties properties;
    private RemoteInstancesManager<RemoteInstance> blockchainInstancesManager;
    private LoggerInstancesManager loggerInstancesManager;
    private LoadGeneratorInstancesManager loadGeneratorInstancesManager;
    private Path blocksLog;
    private Path transactionsLog;

    private volatile AtomicBoolean isLoggersLogLoaded = new AtomicBoolean(false);
    private volatile AtomicBoolean isLoadGeneratorsLogsLoaded = new AtomicBoolean(false);
    private volatile AtomicBoolean isTerminated = new AtomicBoolean(false);


    public TestManager(List<String> allHosts, TestManagerProperties properties) {
        this.allHosts = allHosts;
        this.properties = properties;
        validateProperties(properties);
        blocksLog = getEmptyFolder(Paths.get(properties.getPathToBlockchainResources(), "blocks"));
        transactionsLog = getEmptyFolder(Paths.get(properties.getPathToBlockchainResources(), "transactions"));
    }

    private void validateProperties(TestManagerProperties properties) {
        log.debug("pem key path: {}", properties.getPemKeyPath());
        if (!Files.exists(Paths.get(properties.getPathToBlockchainResources(), "instances"))) {
            log.error("File \"instances\" doesn't exists");
            return;
        }
    }

    public void start() {
        try {

            Spark.port(properties.getMasterPort());
            Spark.threadPool(10, 3, 20000);
            initLoggersListener();
            initLoadGeneratorsListener();
            Spark.awaitInitialization();

            blockchainInstancesManager = runBlockchain();
            Thread.sleep(10000);

            List<LoggerInstance> loggerInstances = new ArrayList<>();
            blockchainInstancesManager.getAllInstances().forEach(i -> loggerInstances.add(new LoggerInstance(
                    properties.getUserNameOnRemoteInstances(), i.getHost(), 22, properties.getPemKeyPath(),
                    Paths.get(properties.getDeployLogPath()), properties.getBlockchainType(),
                    i.getHost(), properties.getFileToLogBlocks(), properties.getRequestPeriod())));
            loggerInstances.forEach(instance -> this.loggerInstances.put(instance.getHost(), instance));
            loggerInstancesManager = runLoggers(loggerInstances);
            Thread.sleep(10000);

            List<LoadGeneratorInstance> loadGeneratorInstances = new ArrayList<>();
            for (int i = 0; i < properties.getLoadGeneratorInstancesAmount(); ++i) {
                loadGeneratorInstances.add(new LoadGeneratorInstance(properties.getUserNameOnRemoteInstances(),
                        allHosts.get(i + properties.getBlockchainInstancesAmount()), 22,
                        properties.getPemKeyPath(), Paths.get(properties.getDeployLogPath()),
                        properties.getAmountOfTransactionsPerTarget(), properties.getAmountOfThreadsPerTarget(),
                        properties.getMinMessageLength(), properties.getMaxMessageLength(), properties.getDelayBeetweenRequests()));
            }
            loadGeneratorInstances.forEach(instance -> this.loadGeneratorInstances.put(instance.getHost(), instance));
            loadGeneratorInstancesManager = runLoadGenerators(loadGeneratorInstances, blockchainInstancesManager.getAllInstances());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private RemoteInstancesManager<RemoteInstance> runBlockchain() {
        List<RemoteInstance> blockchainInstances = allHosts.subList(0, properties.getBlockchainInstancesAmount())
                .stream()
                .map(host -> new RemoteInstance(properties.getUserNameOnRemoteInstances(),
                        host, 22,
                        properties.getPemKeyPath(), Paths.get(properties.getDeployLogPath())))
                .collect(Collectors.toList());
        try {
            RemoteInstancesManager<RemoteInstance> blockchainInstancesManager = new RemoteInstancesManager<>(
                    properties.getMasterIpAddress(),
                    properties.getMasterPort());
            blockchainInstancesManager.setRootInstance(blockchainInstances.get(0));
            blockchainInstancesManager.addCommonInstances(blockchainInstances.subList(1, blockchainInstances.size()));

            blockchainInstancesManager.uploadFilesForAll(Arrays.asList(
                    Paths.get(properties.getPathToBlockchainResources(), properties.getChaincodeFile()),
                    Paths.get(properties.getPathToBlockchainResources(), "initEnv.sh")
            ));

            blockchainInstancesManager.executeCommandsForAll(singletonList("bash initEnv.sh"));

            List<Path> rootFiles = new ArrayList<>();
            rootFiles.add(Paths.get(properties.getPathToBlockchainResources(), "root", "docker-compose.yml"));
            rootFiles.add(Paths.get(properties.getPathToBlockchainResources(), "root", "startRoot.sh"));
            blockchainInstancesManager.uploadFilesForRoot(rootFiles);
            blockchainInstancesManager.executeCommandsForRoot(singletonList(
                    "bash startRoot.sh && touch startRoot.complete")
            );

            List<Path> commonFiles = new ArrayList<>();
            commonFiles.add(Paths.get(properties.getPathToBlockchainResources(), "common", "docker-compose.yml"));
            commonFiles.add(Paths.get(properties.getPathToBlockchainResources(), "common", "startCommon.sh"));
            blockchainInstancesManager.uploadFilesForCommon(commonFiles);
            blockchainInstancesManager.executeCommandsForCommon(Collections.singletonList(
                    "bash startCommon.sh && touch startCommon.complete")
            );

            sleep(20000);
            blockchainInstancesManager.executeCommandsForAll(singletonList("CORE_CHAINCODE_ID_NAME=mycc " +
                    "CORE_PEER_ADDRESS=0.0.0.0:7051 " +
                    "nohup ./" + properties.getChaincodeFile() + " >/dev/null 2>chaincode.log &")
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
            return blockchainInstancesManager;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private LoggerInstancesManager runLoggers(List<LoggerInstance> loggers) {
        log.debug("runLoggers start");
        LoggerInstancesManager loggerInstancesManager = new LoggerInstancesManager(
                properties.getMasterIpAddress(),
                properties.getMasterPort());
        loggerInstancesManager.setRootInstance(loggers.get(0));
        loggerInstancesManager.addCommonInstances(loggers.subList(1, loggers.size()));
        loggerInstancesManager.uploadFilesForAll(singletonList(Paths.get(properties.getPathToBlockchainResources(), "blockchain-logger.jar")));
        loggerInstancesManager.executeCommandsForAll(Arrays.asList(
                "pkill -f 'java -jar'",
                "sudo yum -y install java-1.8.0",
                "nohup java8 -jar blockchain-logger.jar ${LOG_PARAMS} >/dev/null 2>blockchain-logger-stdout.log &"
        ));

        log.debug("runLoggers stop");
        return loggerInstancesManager;
    }

    private  LoadGeneratorInstancesManager runLoadGenerators(List<LoadGeneratorInstance> loadGeneratorInstances,
                                                             List<RemoteInstance> blockchainInstances) throws Exception {
        log.debug("runLoadGenerators start");
        if (loadGeneratorInstances.isEmpty()) {
            log.error("loadGenerators is absent");
            throw new RuntimeException("loadGenerators is absent");
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

        LoadGeneratorInstancesManager loadGeneratorsManager = new LoadGeneratorInstancesManager(
                properties.getMasterIpAddress(),
                properties.getMasterPort());
        loadGeneratorsManager.setRootInstance(loadGeneratorInstances.get(0));
        loadGeneratorsManager.addCommonInstances(loadGeneratorInstances.subList(1, loadGeneratorInstances.size()));

        loadGeneratorsManager.executeCommandsForAll(Arrays.asList(
                "pkill -f 'java -jar'",
                "sudo yum -y install java-1.8.0")
        );
        loadGeneratorsManager.uploadFilesForAll(singletonList(Paths.get(properties.getPathToBlockchainResources(), "load-generator.jar")));

        loadGeneratorsManager.executeCommandsForAll(singletonList(
                "nohup java8 -jar load-generator.jar ${LOAD_PARAMS} >/dev/null 2>load-generator-stdout.log &"));

        log.debug("runLoadGenerators end");
        return loadGeneratorsManager;
    }

    private void initLoggersListener() {
        Spark.post("/logger/state", (req, resp) -> {
            try {
                log.info("/logger/state");
                ObjectMapper mapper = new ObjectMapper();
                WorkFinishedTO workFinishedTO = mapper.readValue(req.body(), WorkFinishedTO.class);
                if (loggerInstances.containsKey(workFinishedTO.getIp())) {
                    resp.status(HTTP_OK);
                    log.info("Logger work finished: {}", workFinishedTO.getIp());
                    loggerInstances.get(workFinishedTO.getIp()).setRunning(false);
                    checkInstancesStatus();
                } else {
                    resp.status(HTTP_NOT_FOUND);
                }
            } catch (JsonParseException e) {
                resp.status(HTTP_BAD_REQUEST);
            }
            return "";
        });
    }

    private void initLoadGeneratorsListener() {
        Spark.post("/loadGenerator/state", (req, resp) -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                WorkFinishedTO workFinishedTO = mapper.readValue(req.body(), WorkFinishedTO.class);
                if (loadGeneratorInstances.containsKey(workFinishedTO.getIp())) {
                    resp.status(HTTP_OK);
                    log.info("LoadGenerator work finished {}", workFinishedTO.getIp());
                    loadGeneratorInstances.get(workFinishedTO.getIp()).setRunning(false);
                    checkInstancesStatus();
                } else {
                    resp.status(HTTP_NOT_FOUND);
                }
            } catch (JsonParseException e) {
                resp.status(HTTP_BAD_REQUEST);
            }
            return "";
        });
    }

    private boolean checkLoggersStatus() {
        boolean isAnyRunning = loggerInstances
                .values()
                .stream()
                .anyMatch(RemoteInstance::isRunning);
        if (!isAnyRunning) {
            getLogsFromLoggers();
        }
        return isAnyRunning;
    }

    private boolean checkLoadGeneratorStatus() {
        boolean isAnyRunning = loadGeneratorInstances
                .values()
                .stream()
                .anyMatch(RemoteInstance::isRunning);
        if (!isAnyRunning) {
            getLogsFromLoadGenerators();
        }
        return isAnyRunning;
    }

    private void checkInstancesStatus() {
        if (!checkLoggersStatus() && !checkLoadGeneratorStatus()) {
            stop();
        }
    }

    private void getLogsFromLoggers() {
        if (isLoggersLogLoaded.compareAndSet(false, true)) {
            loggerInstances.values().forEach(logger ->
                    logger.downloadFiles(singletonList(properties.getFileToLogBlocks())
                            , blocksLog.resolve(logger.getHost() + ".csv"))
            );
        }
    }
    private void getLogsFromLoadGenerators() {
        if (isLoadGeneratorsLogsLoaded.compareAndSet(false, true)) {
            loadGeneratorInstances.values().forEach(loadGenerator ->
                    loadGenerator.downloadFiles(
                            loadGenerator.getLoadTargets().stream().map(target -> "load_logs/" + target + "_load.log").collect(Collectors.toList()),
                            transactionsLog)
                    );
        }
    }

    public void stop() {
        if (isTerminated.compareAndSet(false, true)) {

            getTransactionsForBlocks();

            blockchainInstancesManager.stop();
            loggerInstancesManager.stop();
            loadGeneratorInstancesManager.stop();
            Spark.stop();
        }
    }

    private void getTransactionsForBlocks() {
        log.info("Extracting transactions from blockchain...");
        try (FileWriter fw = new FileWriter(
                getEmptyFolder(Paths.get(properties.getPathToBlockchainResources(), "transactionsPerBlock"))
                        .resolve("transactionsPerBlock").toFile())) {
            fw.write("blockID, transactionID\n");
            BlockchainManager blockchainManager = new BlockchainManager(properties.getBlockchainType(),
                    "grpc://" +blockchainInstancesManager.getRootInstance().getHost() + ":7051");
            long lastBlock = blockchainManager.getChain().getLastBlockNumber();
            for (int i = 0; i < lastBlock; ++i) {
                for (BlockchainTransaction blockchainTransaction: blockchainManager.getBlock(i).getTransactions()) {
                    fw.write(i + "," + blockchainTransaction.getTxId() + '\n');
                }
                fw.flush();
            }
        } catch (IOException e) {
            log.error(e);
        }
        log.info("Extracting finished");
    }

    private static Path getEmptyFolder(Path path) {
        if (path.toFile().exists()) {
            path.toFile().delete();
        }
        path.toFile().mkdirs();
        return path;
    }
}