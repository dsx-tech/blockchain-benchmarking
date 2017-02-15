/*
 * *****************************************************************************
 *  * Blockchain benchmarking framework                                          *
 *  * Copyright (C) 2016 DSX Technologies Limited.                               *
 *  * *
 *  * This program is free software: you can redistribute it and/or modify       *
 *  * it under the terms of the GNU General Public License as published by       *
 *  * the Free Software Foundation, either version 3 of the License, or          *
 *  * (at your option) any later version.                                        *
 *  * *
 *  * This program is distributed in the hope that it will be useful,            *
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 *  * See the GNU General Public License for more details.                       *
 *  * *
 *  * You should have received a copy of the GNU General Public License          *
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *  * *
 *  * Removal or modification of this copyright notice is prohibited.            *
 *  * *
 *  *****************************************************************************
 */

package uk.dsxt.logger;

import lombok.extern.log4j.Log4j2;
import uk.dsxt.blockchain.BlockchainManager;
import uk.dsxt.datamodel.blockchain.BlockchainChainInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

@Log4j2
public class BlockchainLogger {

    private BlockchainManager blockchainManager;
    private long counterForHeight;
    private long requestFrequency;
    private long checkTime;
    private String url;
    private FileWriter fw;

    public BlockchainLogger(String blockchainType, String url, String csv, int requestFrequency) throws IOException {
        this.requestFrequency = requestFrequency;
        this.url = url;
        this.blockchainManager = new BlockchainManager(blockchainType, url);
        Paths.get(csv).toAbsolutePath().getParent().toFile().mkdirs();
        this.fw = new FileWriter(csv, true);
    }

    private void log() throws IOException {

        long timeMillis = System.currentTimeMillis();
        long startTime = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        long lastBlockNumber = blockchainManager.getChain().getLastBlockNumber();
        long time = 0;
        if (lastBlockNumber - 1 > counterForHeight) {
            time = blockchainManager.getBlock(counterForHeight).getTime();
        }
        while (lastBlockNumber - 1 > counterForHeight) {
            counterForHeight++;

            StringJoiner stringJoiner = new StringJoiner(",");
            stringJoiner.add(String.valueOf(counterForHeight));
            stringJoiner.add(String.valueOf(startTime));
            stringJoiner.add(String.valueOf(time));
            fw.write(stringJoiner.toString() + '\n');

            log.info(counterForHeight + " Start time: " + startTime);
            log.info(counterForHeight + " Block committed: " + time);
        }
        fw.flush();
    }

    public void logInLoop() throws IOException, InterruptedException {
        try {
            if (blockchainManager != null) {
                BlockchainChainInfo info = blockchainManager.getChain();
                if (info != null) {
                    counterForHeight = info.getLastBlockNumber();
                    StringJoiner stringJoiner = new StringJoiner(",");
                    stringJoiner.add("id");
                    stringJoiner.add("Start time");
                    stringJoiner.add("Block committed");

                    fw.write(stringJoiner.toString() + '\n');
                    fw.flush();
                    while (true) {
                        log();
                        TimeUnit.MILLISECONDS.sleep(requestFrequency);
                    }
                }
            }
        } finally {
            fw.flush();
            fw.close();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length < 1) {
            log.info("Using default parameters.");
            BlockchainLogger logger = new BlockchainLogger("fabric", "grpc://54.93.73.69:7051",
                    "block.csv",1000);
            logger.logInLoop();
        } else if (args.length == 4) {
            String pattern = "-?\\d+";
            if (args[3].matches(pattern)) {
                BlockchainLogger logger = new BlockchainLogger(args[0], args[1], args[2], Integer.parseInt(args[3]));
                System.out.println("Started logging to " + args[2]);
                logger.logInLoop();
            } else {
                log.error("4th argument is not a integer. Please, enter correct request frequency parameter.");
            }
        } else {
            System.out.println("Wrong number of arguments, need 4 arguments: blockchain type - string, node url - string," +
                    " csv file name - string, request frequency - int, please, try again.");
        }
    }
}