/******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2017 DSX Technologies Limited.                               *
 * *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 * *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 * *
 * Removal or modification of this copyright notice is prohibited.            *
 * *
 ******************************************************************************/
package uk.dsxt.bb.processing;

import au.com.bytecode.opencsv.CSVReader;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import uk.dsxt.bb.model.BlockInfo;
import uk.dsxt.bb.model.BlockchainInfo;
import uk.dsxt.bb.model.TransactionInfo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Log4j2
public class CSVParser {

    //input files
    private static final String PATH = "post-processing/src/main/resources/logs";
    private static final String BLOCKS_DIR = "/blocks";
    private static final String TRANSACTIONS_DIR = "/transactions";
    private static final String TRANSACTIONS_PER_BLOCK_DIR = "/transactionsPerBlock";

    public static BlockchainInfo parseCSVs() {
        BlockchainInfo blockchainInfo = new BlockchainInfo();
        //blocks
        File blocksDir = new File(PATH + BLOCKS_DIR);
        if (!blocksDir.isDirectory() || blocksDir.listFiles() == null) {
            log.error("Can't find blocks directory");
            return null;
        }
        for (File file : blocksDir.listFiles()) {
            try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
                //call corresponding parser
                String nodeId = FilenameUtils.removeExtension(file.getName());
                blockchainInfo.setBlocks(parseBlockCSV(reader, nodeId, blockchainInfo.getBlocks()));
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }
        }
        //transactions
        File transactionsDir = new File(PATH + TRANSACTIONS_DIR);
        if (!transactionsDir.isDirectory() || transactionsDir.listFiles() == null) {
            log.error("Can't find transactions directory");
            return null;
        }
        for (File file : transactionsDir.listFiles()) {
            try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
                //call corresponding parser
                String nodeId = FilenameUtils.removeExtension(file.getName());
                blockchainInfo.addTransactions(parseLoadCSV(reader, nodeId));
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }
        }
        //transactions per block
        File transactionsPerBlockDir = new File(PATH + TRANSACTIONS_PER_BLOCK_DIR);
        if (!transactionsPerBlockDir.isDirectory() || transactionsPerBlockDir.listFiles() == null) {
            log.error("Can't find transactionsPerBlock directory");
            return null;
        }
        for (File file : transactionsPerBlockDir.listFiles()) {
            try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
                //call corresponding parser
                parseTransactionsPerBlockCSV(reader, blockchainInfo.getBlocks(), blockchainInfo.getTransactions());
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }
        }
        return blockchainInfo;
    }

    private static Map<String, TransactionInfo> parseLoadCSV(CSVReader reader, String nodeId) throws IOException {
        String[] nextLine;
        Map<String, TransactionInfo> transactions = new HashMap<>();
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length < 4) {
                //ignore all unparseable lines
                log.error("Unparseable line: " + Arrays.toString(nextLine));
                continue;
            }
            try {
                String transactionId = nextLine[0];
                long creationTime = Long.parseLong(nextLine[1]);
                int size = Integer.parseInt(nextLine[2]);
                int status = Integer.parseInt(nextLine[3]);
                TransactionInfo transactionInfo = new TransactionInfo(creationTime, transactionId, size, nodeId, status);
                transactions.put(transactionId, transactionInfo);
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                //ignore all unparseable lines
            }
        }
        return transactions;
    }

    private static Map<Long, BlockInfo> parseBlockCSV(CSVReader reader, String nodeId,
                                                      Map<Long, BlockInfo> blocks) throws IOException {
        String[] nextLine;
        //skip header
        reader.readNext();
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length < 2) {
                //ignore all unparseable lines
                log.error("Unparseable line: " + Arrays.toString(nextLine));
                continue;
            }
            try {
                long blockId = Long.parseLong(nextLine[0]);
                long time = Long.parseLong(nextLine[1]) * 1000;
                if (blocks.containsKey(blockId)) {
                    BlockInfo block = blocks.get(blockId);
                    block.addDistributionTime(new BlockInfo.DistributionTime(nodeId, time));
                } else {
                    BlockInfo block = new BlockInfo(blockId, new ArrayList<BlockInfo.DistributionTime>() {{
                        add(new BlockInfo.DistributionTime(nodeId, time));
                    }});
                    blocks.put(blockId, block);
                }
            } catch (NumberFormatException e) {
                //ignore all unparseable lines
                log.error(e.getMessage());
            }
        }
        return blocks;
    }

    private static void parseTransactionsPerBlockCSV(CSVReader reader,
                                                     Map<Long, BlockInfo> blocks,
                                                     Map<String, TransactionInfo> transactions) throws IOException {
        String[] nextLine;
        //skip header
        reader.readNext();
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length < 2) {
                //ignore all unparseable lines
                log.error("Unparseable line: " + Arrays.toString(nextLine));
                continue;
            }
            try {
                long blockId = Integer.parseInt(nextLine[0]);
                String transactionId = nextLine[1];

                if (!blocks.containsKey(blockId)) {
                    log.error("Unknown block found : " + blockId);
                    continue;
                }
                if (!transactions.containsKey(transactionId)) {
                    log.error("Unknown transaction found : " + transactionId);
                    continue;
                }
                BlockInfo block = blocks.get(blockId);
                TransactionInfo transaction = transactions.get(transactionId);
                block.addTransaction(transaction);
                transaction.setBlockId(blockId);
                if (blockId > 1) {
                    block.setParentBlockId(blockId - 1);
                }
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                //ignore all unparseable lines
            }
        }
    }
}
