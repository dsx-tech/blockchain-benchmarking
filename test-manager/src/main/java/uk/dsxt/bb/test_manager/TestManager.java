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
    public static final String LOAD_CONFIG_PATH = "load_config.json";

    private List<String> allHosts;
    private ConcurrentHashMap<String, LoggerInstance> loggerInstances = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, LoadGeneratorInstance> loadGeneratorInstances = new ConcurrentHashMap<>();

    private TestManagerProperties properties;
    private List<String> blockchainHosts;
    private RemoteInstancesManager<RemoteInstance> resourceMonitorsInstancesManager;
    private LoggerInstancesManager loggerInstancesManager;
    private LoadGeneratorInstancesManager loadGeneratorInstancesManager;
    private Path blocksLog;
    private Path transactionsLog;
    private Path resourceMonitorsLog;

    private volatile AtomicBoolean isLoggersLogLoaded = new AtomicBoolean(false);
    private volatile AtomicBoolean isLoadGeneratorsLogsLoaded = new AtomicBoolean(false);
    private volatile AtomicBoolean isResourceMonitorsLogsLoaded = new AtomicBoolean(false);
    private volatile AtomicBoolean isTerminated = new AtomicBoolean(false);


    public TestManager(List<String> allHosts, TestManagerProperties properties) {
        this.allHosts = allHosts;
        this.properties = properties;
        validateProperties(properties);
        blocksLog = getEmptyFolder(Paths.get(properties.getResultLogsPath(), "blocks"));
        transactionsLog = getEmptyFolder(Paths.get(properties.getResultLogsPath(), "transactions"));
        resourceMonitorsLog = getEmptyFolder(Paths.get(properties.getResultLogsPath(), "resource_monitors"));
    }

    private void validateProperties(TestManagerProperties properties) {
        log.debug("pem key path: {}", properties.getPemKeyPath());
        if (!Files.exists(Paths.get(properties.getInstancesPath()))) {
            log.error("File \"instances\" doesn't exists");
            return;
        }
        Path logPath = Paths.get(properties.getResultLogsPath());
        if (logPath.toFile().exists()) {
            logPath.toFile().delete();
        }
        logPath.toFile().mkdir();
    }

    public void start() {
        try {

            Spark.port(properties.getMasterPort());
            Spark.threadPool(10, 3, 20000);
            initLoggersListener();
            initLoadGeneratorsListener();
            Spark.awaitInitialization();
            // TODO: 20.11.2017 property?
//            blockchainInstancesManager = runBlockchain();
//            Thread.sleep(10000);

            resourceMonitorsInstancesManager = runResourceMonitors();

            this.blockchainHosts = allHosts.subList(0, properties.getBlockchainInstancesAmount());
            List<LoggerInstance> loggerInstances = blockchainHosts
                    .stream()
                    .map(h -> new LoggerInstance(
                        properties.getUserNameOnRemoteInstances(),
                        h,
                        22,
                        properties.getPemKeyPath(),
                        Paths.get(properties.getResultLogsPath()),
                        properties.getBlockchainType(),
                        h,
                        Integer.toString(properties.getBlockchainPort()),
                        properties.getFileToLogBlocks(),
                        properties.getRequestBlocksPeriod()
            )).collect(Collectors.toList());

            loggerInstances.forEach(instance -> this.loggerInstances.put(instance.getHost(), instance));

            loggerInstancesManager = runLoggers(loggerInstances);
            Thread.sleep(10000);

            List<LoadGeneratorInstance> loadGeneratorInstances = new ArrayList<>();
            for (int i = 0; i < properties.getLoadGeneratorInstancesAmount(); ++i) {
                loadGeneratorInstances.add(new LoadGeneratorInstance(properties.getUserNameOnRemoteInstances(),
                        allHosts.get(i + properties.getBlockchainInstancesAmount()), 22,
                        properties.getPemKeyPath(), Paths.get(properties.getResultLogsPath()),
                        properties.getBlockchainType(), "",
                        Integer.toString(properties.getBlockchainPort()),
                        properties.getAmountOfTransactionsPerTarget(), properties.getAmountOfThreadsPerTarget(),
                        properties.getMinMessageLength(), properties.getMaxMessageLength(), properties.getDelayBeetweenRequests()));
            }
            loadGeneratorInstances.forEach(instance -> this.loadGeneratorInstances.put(instance.getHost(), instance));
            loadGeneratorInstancesManager = runLoadGenerators(loadGeneratorInstances, blockchainHosts);
            initTimeoutForFinishTest();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void initTimeoutForFinishTest() {
        new Thread(() -> {
            try {
                Thread.sleep(properties.getTestTimeout());
                loggerInstances.values().forEach(v -> v.setRunning(false));
                loadGeneratorInstances.values().forEach(v -> v.setRunning(false));
                checkInstancesStatus();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private RemoteInstancesManager<RemoteInstance> runResourceMonitors() {
        List<RemoteInstance> monitorsInstances = allHosts
                .stream()
                .map(host -> new RemoteInstance(properties.getUserNameOnRemoteInstances(),
                        host, 22,
                        properties.getPemKeyPath(), resourceMonitorsLog))
                .collect(Collectors.toList());

        RemoteInstancesManager<RemoteInstance> monitorsInstancesManager = new RemoteInstancesManager<>(
                properties.getMasterIpAddress(),
                properties.getMasterPort());
        monitorsInstancesManager.setRootInstance(monitorsInstances.get(0));
        monitorsInstancesManager.addCommonInstances(monitorsInstances.subList(1, monitorsInstances.size()));

        monitorsInstancesManager.uploadFilesForAll(Arrays.asList(
                properties.getTestManagerModulesPath().resolve("resource-monitor.jar")
        ));

        monitorsInstancesManager.uploadFolderForRoot(properties.getTestManagerModulesPath().resolve("lib"), "lib");
        monitorsInstancesManager.uploadFolderForCommon(properties.getTestManagerModulesPath().resolve("lib"), "lib");

        monitorsInstancesManager.uploadFilesForAll(Arrays.asList(Paths.get(properties.getModulesInitResourcesPath(), "install_java.sh"),
                Paths.get(properties.getModulesInitResourcesPath(), "startResourceMonitor.sh")));
        monitorsInstancesManager.executeCommandsForAll(singletonList("bash install_java.sh"));
        monitorsInstancesManager.executeCommandsForAll(singletonList("bash startResourceMonitor.sh"));
//        Arrays.asList("sudo apt-get update;", "sudo apt-get -y install default-jre;", "nohup java -jar resource-monitor.jar >/dev/null 2>resource-monitor-stdout.log &")
//                .forEach(c -> monitorsInstancesManager.executeCommandsForAll(singletonList(c)));

        log.info("resource monitors started");
        return monitorsInstancesManager;
    }

    private RemoteInstancesManager<RemoteInstance> runBlockchain() {
        List<RemoteInstance> blockchainInstances = allHosts.subList(0, properties.getBlockchainInstancesAmount())
                .stream()
                .map(host -> new RemoteInstance(properties.getUserNameOnRemoteInstances(),
                        host, 22,
                        properties.getPemKeyPath(), Paths.get(properties.getResultLogsPath())))
                .collect(Collectors.toList());
        try {
            RemoteInstancesManager<RemoteInstance> blockchainInstancesManager = new RemoteInstancesManager<>(
                    properties.getMasterIpAddress(),
                    properties.getMasterPort());
            blockchainInstancesManager.setRootInstance(blockchainInstances.get(0));
            blockchainInstancesManager.addCommonInstances(blockchainInstances.subList(1, blockchainInstances.size()));

            blockchainInstancesManager.uploadFilesForAll(Arrays.asList(
                    properties.getBlockchainInitResourcesPath().resolve("initEnv.sh")
            ));

            blockchainInstancesManager.executeCommandsForAll(singletonList("bash initEnv.sh"));

            blockchainInstancesManager.uploadFolderForRoot(properties.getBlockchainInitResourcesPath().resolve("root"), "root_init_files");
            blockchainInstancesManager.executeCommandsForRoot(singletonList(
                    "bash root_init_files/startRoot.sh && touch startRoot.complete")
            );
            blockchainInstancesManager.getRootInstance().downloadFolder("~/root_init_result",
                    Paths.get("tmp", "root_init_result"));

            blockchainInstancesManager.uploadFolderForCommon(properties.getBlockchainInitResourcesPath().resolve("common"), "common_init_files");
            blockchainInstancesManager.uploadFolderForCommon(Paths.get("tmp", "root_init_result"), "common_init_files/root_init_result");
            blockchainInstancesManager.executeCommandsForCommon(Collections.singletonList(
                    "bash common_init_files/startCommon.sh && touch startCommon.complete")
            );
            sleep(properties.getAfterBlockchainInitTimeout());
            blockchainInstancesManager.executeCommandsForRoot(singletonList("bash root_init_files/on_init_finished.sh"));
            Thread.sleep(5 * 1000);
            log.info("Blockchain instances started");
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
        loggerInstancesManager.uploadFilesForAll(singletonList(properties.getTestManagerModulesPath().resolve("blockchain-logger.jar")));

        loggerInstancesManager.uploadFilesForAll(singletonList(Paths.get(properties.getModulesInitResourcesPath(), "startLogger.sh")));
        loggerInstancesManager.uploadFolderForAll(Paths.get(properties.getModulesInitResourcesPath(), "credentials"), "credentials");
        loggerInstancesManager.executeCommandsForAll(Arrays.asList(
                "bash startLogger.sh"
        ));

        log.debug("runLoggers stop");
        return loggerInstancesManager;
    }

    private  LoadGeneratorInstancesManager runLoadGenerators(List<LoadGeneratorInstance> loadGeneratorInstances,
                                                             List<String> blockchainInstances) throws Exception {
        log.debug("runLoadGenerators start");
        if (loadGeneratorInstances.isEmpty()) {
            log.error("loadGenerators is absent");
            throw new RuntimeException("loadGenerators is absent");
        }
        List<String> blockchainHosts = new ArrayList<>(blockchainInstances);
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

        loadGeneratorsManager.uploadFilesForAll(Arrays.asList(
                properties.getTestManagerModulesPath().resolve("load-generator.jar"),
//                Paths.get("tmp", "root_init_result", "credentials"),
                Paths.get(properties.getModulesInitResourcesPath(), "startLoadGenerator.sh"),
                properties.getLoadGeneratorConfigPath()
        ));

        loadGeneratorsManager.uploadFolderForAll(Paths.get(properties.getModulesInitResourcesPath(), "credentials"), "credentials");

        loadGeneratorsManager.executeCommandsForAll(singletonList(String.format("mv %s %s",
                properties.getLoadGeneratorConfigPath().getFileName().toString(), TestManager.LOAD_CONFIG_PATH)));
        loadGeneratorsManager.executeCommandsForAll(singletonList("bash startLoadGenerator.sh"));

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

            getResourceMonitorsLogs();
            getTransactionsForBlocks();

            List<Runnable> stopActions = Arrays.asList(
//                    () -> blockchainInstancesManager.stop(),
                    () -> loggerInstancesManager.stop(),
                    () -> loadGeneratorInstancesManager.stop(),
                    () -> resourceMonitorsInstancesManager.stop(),
                    Spark::stop
            );

            boolean success = stopActions.stream().allMatch(this::safeExecute);
            log.info(success ? "Test completed" : "You can stop test, but something went wrong");
        }
    }

    private boolean safeExecute(Runnable action) {
        try {
            action.run();
            return true;
        }
        catch (Exception e) {
            log.error(e);
        }
        return false;
    }

    private void getResourceMonitorsLogs() {
        log.info("Getting logs from resource-monitors");
        if (isResourceMonitorsLogsLoaded.compareAndSet(false, true)) {
            resourceMonitorsInstancesManager.getAllInstances().forEach(monitor ->
                    monitor.downloadFiles(
                            singletonList("resource_usage.csv"),
                            resourceMonitorsLog.resolve(monitor.getHost() + "_res_usage.csv"))
            );
        }
    }

    private void getTransactionsForBlocks() {
        log.info("Extracting transactions from blockchain...");
        try (FileWriter fw = new FileWriter(
                getEmptyFolder(Paths.get(properties.getResultLogsPath(), "transactionsPerBlock"))
                        .resolve("transactionsPerBlock").toFile())) {
            fw.write("blockID, transactionID\n");
            BlockchainManager blockchainManager = new BlockchainManager(properties.getBlockchainType(),
                    "grpc://" +blockchainHosts.get(0) + ":" + properties.getBlockchainPort(),
                    "D:/Projects/blockchain-benchmarking/hyperledger/init_modules/credentials/local/1.properties");
            long lastBlock = blockchainManager.getChain().getLastBlockNumber();
            for (int i = 0; i <= lastBlock; ++i) {
                for (BlockchainTransaction blockchainTransaction: blockchainManager.getBlockById(i).getTransactions()) {
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