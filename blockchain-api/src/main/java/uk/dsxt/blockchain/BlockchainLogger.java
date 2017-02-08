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

package uk.dsxt.blockchain;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.concurrent.TimeUnit;

@Log4j2
public class BlockchainLogger {

    private BlockchainManager blockchainManager;
    private long counterForHeight = 0;
    private FileWriter fw = new FileWriter("block.csv", true);

    BlockchainLogger(BlockchainManager blockchainManager) throws IOException {
        this.blockchainManager = blockchainManager;
    }

    private void log() throws IOException {

        long timeMillis = System.currentTimeMillis();
        long startTime = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        long blockNumber = blockchainManager.getChain().getLastBlockNumber();
        if (blockNumber - 1 > counterForHeight) {
            counterForHeight++;
            long time = blockchainManager.getBlock(blockNumber-1).getTime();
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

    public void logInLoop() throws IOException {
        try {
            counterForHeight = blockchainManager.getChain().getLastBlockNumber();
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
            }
        } catch (Exception e) {
            log.error("Exception while logging to csv");
        }
        finally {
            fw.flush();
            fw.close();
        }
    }
}
