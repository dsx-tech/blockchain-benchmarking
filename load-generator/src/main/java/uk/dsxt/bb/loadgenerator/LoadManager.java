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

package uk.dsxt.bb.loadgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.blockchain.BlockchainManager;
import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.loadgenerator.data.Credential;
import uk.dsxt.bb.loadgenerator.external_monitor.SpyManager;
import uk.dsxt.bb.loadgenerator.load_plan.LoadPlan;
import uk.dsxt.bb.test_manager.TestManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author phd
 */
@Log4j2
class LoadManager {
    private static final int BATCH_SIZE = 100;
    private static final char[] symbols;
    private static final int TRANSACTION_WARNING_LIMIT_MILLIS = 100;

    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch)
            tmp.append(ch);
        for (char ch = 'a'; ch <= 'z'; ++ch)
            tmp.append(ch);
        symbols = tmp.toString().toCharArray();
    }

    private final int minLength;
    private final int maxLength;
    private List<String> targets;
    private List<Credential> credentials;
    private int amountOfTransactions;
    private int amountOfThreadsPerTarget;
    private ExecutorService executorService;
    private List<Logger> loggers;
    private String blockchainType;
    private String blockchainPort;

    LoadManager(List<String> targets, List<Credential> credentials,
                int amountOfTransactions, int amountOfThreadsPerTarget,
                int minLength, int maxLength,
                String blockchainType, String blockchainPort) {
        this.targets = targets;
        this.credentials = credentials;
        this.amountOfTransactions = amountOfTransactions;
        this.amountOfThreadsPerTarget = amountOfThreadsPerTarget;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.loggers = new ArrayList<>();
        this.blockchainType = blockchainType;
        this.blockchainPort = blockchainPort;
        executorService = Executors.newFixedThreadPool(amountOfThreadsPerTarget * targets.size());
    }

    private static String generateMessage(Random random, int minLength, int maxLength) {
        int length = minLength + random.nextInt(maxLength - minLength);
        StringBuilder tmp = new StringBuilder(length);
        for (int i = 0; i < minLength + length; ++i) {
            tmp.append(symbols[random.nextInt(symbols.length)]);
        }
        return tmp.toString();
    }

    private static int getRandomIntExcept(Random random, int bound, int except) {
        int candidate = random.nextInt(bound);
        while (candidate == except) {
            candidate = random.nextInt(bound);
        }
        return candidate;
    }

    void start() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        for (int targetIndex = 0; targetIndex < targets.size(); ++targetIndex) {
            final String target = targets.get(targetIndex);
            final int currentTargetIndex = targetIndex;
            Logger logger = new Logger(Paths.get("load_logs", target + "_load.log"));
            loggers.add(logger);
//            Manager manager = new SpyManager(new BlockchainManager(blockchainType, "http://" + target + ":" + blockchainPort), String.valueOf(currentTargetIndex));
//            Manager manager = new MockManager(String.valueOf(currentTargetIndex));
            Manager manager = new BlockchainManager(blockchainType, "http://" + target + ":" + blockchainPort);
            int credentialFromIndex = -1;
            Credential credentialFrom = null;

            if (!credentials.isEmpty()) {
                credentialFromIndex = targetIndex % credentials.size();
                credentialFrom = credentials.get(credentialFromIndex);
                manager.authorize(credentialFrom.getAccount(), credentialFrom.getPassword());
            }

            for (int i = 0; i < amountOfThreadsPerTarget; ++i) {
                int finalCredentialFromIndex = credentialFromIndex;
                Credential finalCredentialFrom = credentialFrom;
                executorService.submit(() -> {
                    LoadPlan loadPlan;
                    try {
                        loadPlan = objectMapper.readValue(new File(System.getProperty("user.home") + "/" + TestManager.LOAD_CONFIG_PATH), LoadPlan.class);
                    } catch (IOException e) {
                        log.error("Unable to load Load plan", e);
                        return;
                    }
                    List<String> logs = new ArrayList<>(BATCH_SIZE);
                    Random random = new Random();
                    int failed = 0;
                    int previousExecutionTimeMillis = 0;
                    for (int transactionId = 0; transactionId < amountOfTransactions; ++transactionId) {
                        try {
                            int generatedDelay = loadPlan.nextDelay(currentTargetIndex);
                            int sleepMillis = generatedDelay - previousExecutionTimeMillis;
                            if (sleepMillis > 0) {
                                Thread.sleep(sleepMillis);
                            } else if (sleepMillis < -5) {
                                log.info(String.format("Too much time to execute: expected=%d, actual=%d, timestamp=%d",
                                        generatedDelay, previousExecutionTimeMillis, System.currentTimeMillis()));
                            }
                            long startTimeNanos = System.nanoTime();

                            String message = generateMessage(random, minLength, maxLength);
                            String id;
                            if (finalCredentialFrom != null) {
                                int credentialToIndex = getRandomIntExcept(random, credentials.size(), finalCredentialFromIndex);
                                Credential credentialTo = credentials.get(credentialToIndex);
                                id = manager.sendMessage(finalCredentialFrom.getAccount(), credentialTo.getAccount(), message);
                            } else {
                                id = manager.sendMessage(message.getBytes());
                            }

                            logs.add(String.format("%s,%d,%d,%s", id, System.currentTimeMillis(),
                                    message.length(), id != null && !id.isEmpty() ? "OK" : "FAIL"));
                            if (transactionId == amountOfTransactions - 1 || transactionId % BATCH_SIZE == 0) {
                                logger.addLogs(logs);
                                logs = new ArrayList<>(BATCH_SIZE);
                            }

                            if (id == null || id.isEmpty()) {
                                ++failed;
                            } else {
                                failed = 0;
                            }
                            if (failed > 5) {
                                log.warn(String.format("Failed too much times(%d), timestamp=%d", failed, System.currentTimeMillis()));
                                manager.authorize(finalCredentialFrom.getAccount(), finalCredentialFrom.getPassword());
                                failed = 0;
                            }

                            previousExecutionTimeMillis = (int)((System.nanoTime() - startTimeNanos) / 1000000L);
                            if (previousExecutionTimeMillis > TRANSACTION_WARNING_LIMIT_MILLIS) {
                                log.info(String.format("Too much time to execute: %d, timestamp=%d",
                                        previousExecutionTimeMillis, System.currentTimeMillis()));
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    void waitCompletion() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(24, TimeUnit.HOURS);
            for (Logger logger : loggers) {
                logger.shutdown();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
