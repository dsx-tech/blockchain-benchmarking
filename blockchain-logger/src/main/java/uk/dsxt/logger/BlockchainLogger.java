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
import java.util.concurrent.TimeUnit;

@Log4j2
public class BlockchainLogger {

    private BlockchainManager blockchainManager;
    private long counterForHeight;
    private long requestFrequency;
    private String url;
    private FileWriter fw;

    public BlockchainLogger(String blockchainType, String url, String csv, int requestFrequency) throws IOException {
        this.requestFrequency = requestFrequency;
        this.url = url;
        this.blockchainManager = new BlockchainManager(blockchainType, url);
        this.fw = new FileWriter(csv, true);
    }

    private void log() throws IOException {

        long timeMillis = System.currentTimeMillis();
        long startTime = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        long blockNumber = blockchainManager.getChain().getLastBlockNumber();
        if (blockNumber - 1 > counterForHeight) {
            counterForHeight++;
            long time = blockchainManager.getBlock(blockNumber - 1).getTime();
            String str = String.valueOf(counterForHeight) +
                    "," +
                    startTime +
                    "," +
                    time +
                    "\n";
            fw.write(str);
            fw.flush();

            log.info(counterForHeight + " Start time: " + startTime);
            log.info(counterForHeight + " Block committed: " + time);
        }
    }

    public void logInLoop() throws IOException, InterruptedException {
        try {
            if (blockchainManager != null) {
                BlockchainChainInfo info = blockchainManager.getChain();
                if (info != null) {
                    counterForHeight = info.getLastBlockNumber();
                    String str = "id" +
                            "," +
                            "Start time" +
                            "," +
                            "Block commited" +
                            "\n";
                    fw.write(str);
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
            BlockchainLogger logger = new BlockchainLogger("fabric", "grpc://172.17.0.3:7051",
                    "block.csv",1000);
            logger.logInLoop();
        } else if (args.length == 4) {
            BlockchainLogger logger = new BlockchainLogger(args[0], args[1], args[2], Integer.parseInt(args[3]));
            System.out.println("Started logging to " + args[2]);
            logger.logInLoop();
        } else {
            System.out.println("Wrong number of arguments, need 4 arguments: blockchain type - string, node url - string," +
                    " csv file name - string, request frequency - int, please, try again.");
        }
    }
}