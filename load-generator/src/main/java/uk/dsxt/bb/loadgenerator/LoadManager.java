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

import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.blockchain.BlockchainManager;
import uk.dsxt.bb.loadgenerator.data.Credential;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
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
    private final int delay;
    private List<String> targets;
    private List<Credential> credentials;
    private int amountOfTransactions;
    private int amountOfThreadsPerTarget;
    private ExecutorService executorService;
    private List<Logger> loggers;

    LoadManager(List<String> targets, List<Credential> credentials, int amountOfTransactions, int amountOfThreadsPerTarget, int minLength, int maxLength, int delay) {
        this.targets = targets;
        this.credentials = credentials;
        this.amountOfTransactions = amountOfTransactions;
        this.amountOfThreadsPerTarget = amountOfThreadsPerTarget;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.delay = delay;
        this.loggers = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(amountOfThreadsPerTarget * targets.size());
    }

    private static String generateMessage(Random random, int minLength, int maxLength) {
        StringBuilder tmp = new StringBuilder();
        int length = minLength + random.nextInt(maxLength - minLength);
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
        for (int targetIndex = 0; targetIndex < targets.size(); ++targetIndex) {
            String t = targets.get(targetIndex);
            Logger logger = new Logger(Paths.get("load_logs", t + "_load.log"));
            loggers.add(logger);
            BlockchainManager manager = new BlockchainManager("ethereum", "http://" + t + ":8101");
            int credentialFromIndex = targetIndex % credentials.size();
            Credential credentialFrom = credentials.get(credentialFromIndex);
            manager.authorize(credentialFrom.getAccount(), credentialFrom.getPassword());
            for (int i = 0; i < amountOfThreadsPerTarget; ++i) {
                executorService.submit(() -> {
                    List<String> logs = new ArrayList<>(BATCH_SIZE);
                    Random random = new Random();
                    for (int j = 0; j < amountOfTransactions; ++j) {
                        try {
                            Thread.sleep(delay);
//                            String message = "0x" + Integer.toHexString(random.nextInt(1000) + 1000);
                            String message = generateMessage(random, 25, 100);
                            long startTime = System.currentTimeMillis();

                            int credentialToIndex = getRandomIntExcept(random, credentials.size(), credentialFromIndex);
                            Credential credentialTo = credentials.get(credentialToIndex);
                            String id = manager.sendMessage(credentialFrom.getAccount(), credentialTo.getAccount(), message);

                            StringJoiner stringJoiner = new StringJoiner(",");
                            stringJoiner.add(id);
                            stringJoiner.add(Long.toString(startTime));
                            stringJoiner.add(Integer.toString(message.getBytes().length));
                            stringJoiner.add(id != null && !id.isEmpty() ? "OK" : "FAIL");

                            logs.add(stringJoiner.toString());
                            if (j == amountOfTransactions - 1 || j % BATCH_SIZE == 0) {
                                logger.addLogs(logs);
                                logs = new ArrayList<>(BATCH_SIZE);
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
