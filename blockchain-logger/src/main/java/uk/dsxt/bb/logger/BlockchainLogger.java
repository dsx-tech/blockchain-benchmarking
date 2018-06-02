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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@Log4j2
public class BlockchainLogger {

    private static final ObjectMapper mapper = new ObjectMapper();
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

    private void log(long blockId, long time, String blockHash) throws IOException {
        fw.write(String.format("%d,%d,%s\n", blockId, time, blockHash));
        fw.flush();
    }

    private boolean isBlocksInRangeHasTransactions(long start, long end) {
        return LongStream.rangeClosed(start + 1, end)
                .parallel()
                .anyMatch(i -> {
                    try {
                        BlockchainBlock block = blockchainManager.getBlockById(i);
                        return block != null && block.getTransactions() != null && block.getTransactions().length > 0;
                    } catch (IOException e) {
                        log.error(e);
                        return false;
                    }
                });
    }

    private void logInLoop() {
        try {
            if (blockchainManager != null) {
                BlockchainChainInfo info = blockchainManager.getChain();
                if (info != null) {
                    fw.write("id,Appeared time,Current block hash\n");
                    fw.flush();

                    long lastNonEmptyBlockTime = System.currentTimeMillis();
                    // stores all hashes of blocks, that were viewed previously
                    Set<String> processedBlocksHashes = new HashSet<>();
                    processedBlocksHashes.add("");

                    while (true) {
                        long startLoopTime = System.currentTimeMillis();
                        long blockIdToImport = blockchainManager.getChain().getLastBlockNumber();
                        BlockchainBlock blockToImport = blockchainManager.getBlockById(blockIdToImport);
                        String blockHashToImport = blockToImport == null ? "" : blockToImport.getHash();
                        // Check if last block was already processed, if not, save it and check previous block
                        while (!processedBlocksHashes.contains(blockHashToImport) && blockIdToImport > 0) {
                            log.debug(String.format("New block, blockHash=%s, blockId=%d", blockHashToImport, blockIdToImport));
                            if (blockToImport != null && blockToImport.getTransactions() != null
                                    && blockToImport.getTransactions().length > 0) {
                                lastNonEmptyBlockTime = startLoopTime;
                            }
                            log(blockIdToImport, startLoopTime, blockHashToImport);
                            processedBlocksHashes.add(blockHashToImport);
                            blockToImport = blockchainManager.getBlockById(--blockIdToImport);
                            blockHashToImport = blockToImport == null ? "" : blockToImport.getHash();
                        }

                        if (startLoopTime - lastNonEmptyBlockTime > 10 * 60 * 1000) { // && !isBlocksInRangeHasTransactions(blockIdToImport, ...)) {
                            Unirest.post("http://" + masterHost + "/logger/state")
                                    .body(mapper.writeValueAsString(new WorkFinishedTO(ip))).asJson();
                            break;
                        }

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