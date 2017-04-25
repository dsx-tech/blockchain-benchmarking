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

package uk.dsxt.bb.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.blockchain.BlockchainManager;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.bb.remote.instance.WorkFinishedTO;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@Log4j2
public class BlockchainLogger {

    private BlockchainManager blockchainManager;
    private long requestFrequency;
    private FileWriter fw;
    private String ip;
    private String masterHost;

    public BlockchainLogger(String blockchainType, String url, String csv, int requestFrequency, String ip, String masterHost) throws IOException {
        this.requestFrequency = requestFrequency;
        this.blockchainManager = new BlockchainManager(blockchainType, url);
        Paths.get(csv).toAbsolutePath().getParent().toFile().mkdirs();
        this.fw = new FileWriter(csv, true);
        this.ip = ip;
        this.masterHost = masterHost;
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                String defaultBlockchainType = "fabric";
                String defaultURL = "grpc://54.93.73.69:7051";
                String defaultCSV = "block.csv";
                int requestFrequency = 1000;

                log.info("No arguments found. Starting Blockchain Logger using default parameters.");
                log.debug("Default arguments. Blockchain type: {}, URL: {}, CSV: {}, Frequency: {}", defaultBlockchainType, defaultURL, defaultCSV, requestFrequency);
                BlockchainLogger logger = new BlockchainLogger(defaultBlockchainType, defaultURL, defaultCSV, requestFrequency, "", "");
                logger.logInLoop();
            } else if (args.length == 6) {
                String pattern = "-?\\d+";
                if (args[3].matches(pattern)) {
                    BlockchainLogger logger = new BlockchainLogger(args[0], args[1], args[2], Integer.parseInt(args[3]), args[4], args[5]);
                    log.info("Starting Blockchain Logger. Destination CSV: {}", args[2]);
                    logger.logInLoop();
                } else {
                    log.error("4th argument is not a integer. Please, enter correct request frequency parameter.");
                }
            } else {
                log.warn("Wrong number of arguments, need 4 arguments: blockchain type - string, node url - string," +
                        " csv file name - string, request frequency - int, please, try again.");
            }
        } catch (Exception e) {
            log.error("Couldn't start Blockchain Logger.", e);
        }
    }

    private void log(long lastLoggedBlockId, long currentBlockId, long time) throws IOException {
        for (long i = lastLoggedBlockId + 1; i <= currentBlockId; ++i) {
            StringJoiner stringJoiner = new StringJoiner(",");
            stringJoiner.add(String.valueOf(i));
            stringJoiner.add(String.valueOf(time));
            fw.write(stringJoiner.toString() + '\n');
        }
        fw.flush();
    }

    private boolean isBlocksInRangeHasTransactions(long start, long end) {
        return LongStream.rangeClosed(start + 1, end)
                .parallel()
                .anyMatch(i -> {
                    try {
                        return blockchainManager.getBlock(i).getTransactions().length > 0;
                    } catch (IOException e) {
                        log.error(e);
                        return false;
                    }
                });
    }
    public void logInLoop() {
        try {
            if (blockchainManager != null) {
                BlockchainChainInfo info = blockchainManager.getChain();
                if (info != null) {
                    StringJoiner stringJoiner = new StringJoiner(",");
                    stringJoiner.add("id");
                    stringJoiner.add("Appeared time");
                    fw.write(stringJoiner.toString() + '\n');
                    fw.flush();

                    long currentBlockId;
                    long previousBlockId = -1;
                    long lastNonEmptyBlockId = -1;
                    long lastNonEmptyBlockTime = System.currentTimeMillis();

                    while (true) {
                        long startLoopTime = System.currentTimeMillis();
                        currentBlockId = blockchainManager.getChain().getLastBlockNumber();
                        long currentBlockTime = System.currentTimeMillis();
                        BlockchainBlock currentBlock = blockchainManager.getBlock(currentBlockId);


                        if (currentBlockId > previousBlockId) {
                            log(previousBlockId, currentBlockId, currentBlockTime);
                        }

                        if ((startLoopTime - lastNonEmptyBlockTime > 10 * 60 * 1000) && !isBlocksInRangeHasTransactions(previousBlockId, currentBlockId)) {
                            ObjectMapper mapper = new ObjectMapper();
                            WorkFinishedTO remoteInstanceStateTO = new WorkFinishedTO(ip);

                            Unirest.post("http://" + masterHost + "/logger/state")
                                    .body(mapper.writeValueAsString(remoteInstanceStateTO)).asJson();
                            break;
                        }

                        if (currentBlock.getTransactions().length != 0) {
                            lastNonEmptyBlockId = currentBlockId;
                            lastNonEmptyBlockTime = currentBlockTime;
                        }

                        previousBlockId = currentBlockId;

                        long endLoopTime = System.currentTimeMillis();
                        long loopTime = endLoopTime - startLoopTime;
                        if (requestFrequency > loopTime) {
                            TimeUnit.MILLISECONDS.sleep(requestFrequency - loopTime);
                        } else {
                            log.warn("Request for new block was longer than request.blocks.period, it is can impact on results.");
                        }
                    }
                }
            }
        } catch (InterruptedException ie) {
            log.error("BlockchainLogger loop was interrupted while sleeping", ie);
        } catch (IOException ioe) {
            log.error("Couldn't write blocks information to file", ioe);
        } catch (UnirestException e) {
            log.error(e);
        } finally {
            try {
                fw.flush();
            } catch (IOException e) {
                log.error("Couldn't flush FileWriter");
            }
            try {
                fw.close();
            } catch (IOException e) {
                log.error("Couldn't close FileWriter");
            }
        }
    }
}