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
package uk.dsxt.bb.scenario.proccessing;

import au.com.bytecode.opencsv.CSVReader;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import uk.dsxt.bb.properties.proccessing.model.BlockchainType;
import uk.dsxt.bb.properties.proccessing.model.PropertiesFileInfo;
import uk.dsxt.bb.scenario.proccessing.model.BlockchainInfo;
import uk.dsxt.bb.scenario.proccessing.model.BlockInfo;
import uk.dsxt.bb.scenario.proccessing.model.ResourceInfo;
import uk.dsxt.bb.scenario.proccessing.model.TransactionInfo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Log4j2
public class CSVParser {

    private static final String LOGS_DIR = "/logs";

    private static final String BLOCKS_DIR = "/blocks";
    private static final String TRANSACTIONS_DIR = "/transactions";
    private static final String TRANSACTIONS_PER_BLOCK_DIR = "/transactionsPerBlock";
    private static final String RESOURCES_DIR = "/resource_monitors";

    private enum DirType {
        BLOCKS,
        TRANSACTIONS,
        RESOURCES,
        TRANSACTIONS_PER_BLOCK
    }

    private String scenarioDir;
    private BlockchainInfo blockchainInfo;

    public CSVParser(String scenarioDir) {
        this.scenarioDir = scenarioDir + LOGS_DIR;
    }

    public BlockchainInfo parseCSVs(BlockchainType type) {
        // String scenarioDir = properties.getPathToScenarioDir() ;
        blockchainInfo = new BlockchainInfo(type);

        if (
                parseDir(TRANSACTIONS_DIR, DirType.TRANSACTIONS) &&
                parseDir(RESOURCES_DIR, DirType.RESOURCES) &&
                parseDir(TRANSACTIONS_PER_BLOCK_DIR, DirType.TRANSACTIONS_PER_BLOCK) &&
                        parseDir(BLOCKS_DIR, DirType.BLOCKS)) {
            return blockchainInfo;
        }
        return null;
//        //blocks
//        File blocksDir = new File(scenarioDir + BLOCKS_DIR);
//        if (!blocksDir.isDirectory() || blocksDir.listFiles() == null) {
//            log.error("Can't find blocks directory in " + scenarioDir);
//            return null;
//        }
//        blockchainInfo.setNumberOfNodes(blocksDir.listFiles().length);
//        for (File file : blocksDir.listFiles()) {
//            try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
//                //call corresponding parser
//                String nodeId = FilenameUtils.removeExtension(file.getName());
//                blockchainInfo.setBlocks
//                        (parseBlockCSV(reader, nodeId, blockchainInfo.getBlocks()));
//            } catch (IOException e) {
//                log.error(e.getMessage());
//                return null;
//            }
//        }
//        //transactions
//        File transactionsDir = new File(scenarioDir + TRANSACTIONS_DIR);
//        if (!transactionsDir.isDirectory() || transactionsDir.listFiles() == null) {
//            log.error("Can't find transactions directory in " + scenarioDir);
//            return null;
//        }
//        for (File file : transactionsDir.listFiles()) {
//            try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
//                //call corresponding parser
//                String nodeId = FilenameUtils.removeExtension(file.getName());
//                nodeId = nodeId.substring(0, nodeId.length() - 5);
//                blockchainInfo.addTransactions(parseLoadCSV(reader, nodeId));
//            } catch (IOException e) {
//                log.error(e.getMessage());
//                return null;
//            }
//        }
//        //resources
//        File resourcesDir = new File(scenarioDir + RESOURCES_DIR);
//        if (!resourcesDir.isDirectory() || resourcesDir.listFiles() == null) {
//            log.error("Can't find resources directory in " + scenarioDir);
//            return null;
//        }
//        for (File file : resourcesDir.listFiles()) {
//            try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
//                //call corresponding parser
//                String nodeId = FilenameUtils.removeExtension(file.getName());
//                nodeId = nodeId.substring(0, nodeId.length() - 10);
//                blockchainInfo.setResources(parseResourcesCSV(reader, nodeId));
//            } catch (IOException e) {
//                log.error(e.getMessage());
//                return null;
//            }
//        }
//        //transactions per block
//        File transactionsPerBlockDir = new File(scenarioDir + TRANSACTIONS_PER_BLOCK_DIR);
//        if (!transactionsPerBlockDir.isDirectory() || transactionsPerBlockDir.listFiles() == null) {
//            log.error("Can't find transactionsPerBlock directory in " + scenarioDir);
//            return null;
//        }
//        for (File file : transactionsPerBlockDir.listFiles()) {
//            try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
//                //call corresponding parser
//                parseTransactionsPerBlockCSV(reader,
//                        blockchainInfo.getBlocks(), blockchainInfo.getTransactions());
//            } catch (IOException e) {
//                log.error(e.getMessage());
//                return null;
//            }
//        }
//        return blockchainInfo;
    }


    private boolean parseDir(String path, DirType type) {
        File dir = new File(scenarioDir + path);
        if (!dir.isDirectory() || dir.listFiles() == null) {
            log.error("Can't find " + path + " directory in " + scenarioDir);
            return false;
        }
        if (type == DirType.BLOCKS) {
            blockchainInfo.setNumberOfNodes(dir.listFiles().length);
        }
        for (File file : dir.listFiles()) {
            try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
                //call corresponding parser
                String nodeId = FilenameUtils.removeExtension(file.getName());
                switch (type) {
                    case BLOCKS:
                        parseBlockCSV(reader, nodeId, blockchainInfo.getBlocks());
                        break;
                    case TRANSACTIONS:
                        nodeId = nodeId.substring(0, nodeId.length() - 5);
                        blockchainInfo.addTransactions(parseLoadCSV(reader, nodeId));
                        break;
                    case RESOURCES:
                        if (nodeId.contains("deploy")) {
                            break;
                        }
                        nodeId = nodeId.substring(0, nodeId.length() - 10);
                        blockchainInfo.addResources(parseResourcesCSV(reader, nodeId));
                        break;
                    case TRANSACTIONS_PER_BLOCK:
                        blockchainInfo.setBlocks(parseTransactionsPerBlockCSV(reader,
                                 blockchainInfo.getTransactions()));
                        break;
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    private static Map<String, TransactionInfo> parseLoadCSV(CSVReader reader, String nodeId)
            throws IOException {
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
                String status = nextLine[3];
                if (status.equals("FAIL")) {
                    log.info("Failed transaction of size " + size);
                    continue;
                }
                TransactionInfo transactionInfo = new TransactionInfo(creationTime,
                        transactionId, size, nodeId);
                transactions.put(transactionId, transactionInfo);
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                //ignore all unparseable lines
            }
        }
        return transactions;
    }

    private static List<ResourceInfo> parseResourcesCSV(CSVReader reader, String nodeId)
            throws IOException {
        List<ResourceInfo> resources = new ArrayList<>();
        String[] nextLine;
        //skip header
        reader.readNext();
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length < 6) {
                //ignore all unparseable lines
                log.error("Unparseable line: " + Arrays.toString(nextLine));
                continue;
            }
            try {
                long time = Long.parseLong(nextLine[0]);
                double cpu = Double.parseDouble(nextLine[1]);
                int mem = Integer.parseInt(nextLine[2]);
                double memPercent = Double.parseDouble(nextLine[3]);
                int downloaded = Integer.parseInt(nextLine[4]);
                int uploaded = Integer.parseInt(nextLine[5]);
                ResourceInfo resourceInfo = new ResourceInfo(time, nodeId, cpu,
                        memPercent, mem, downloaded, uploaded);
                resources.add(resourceInfo);
            } catch (NumberFormatException e) {
                //ignore all unparseable lines
                log.error(e.getMessage());
            }
        }
        return resources;
    }


    private static void parseBlockCSV(CSVReader reader, String nodeId,
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
                long time = Long.parseLong(nextLine[1]); // * 1000;
                if (!blocks.containsKey(blockId)) {
                   // log.info("Block with no transactions");
                    continue;
                }
                    BlockInfo block = blocks.get(blockId);
                    block.addDistributionTime(new BlockInfo.DistributionTime(nodeId, time));
            } catch (NumberFormatException e) {
                //ignore all unparseable lines
                log.error(e.getMessage());
            }
        }
    }

    private static Map<Long, BlockInfo> parseTransactionsPerBlockCSV(CSVReader reader,
                                                  //   Map<Long, BlockInfo> blocks,
                                                     Map<String, TransactionInfo> transactions) throws IOException {
        Map<Long, BlockInfo> blocks = new HashMap<>();
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

//                if (!blocks.containsKey(blockId)) {
//                    log.info("Unknown block found : " + blockId);
//                    continue;
//                }
                if (!transactions.containsKey(transactionId)) {
                    log.info("Unknown transaction found : " + transactionId);
                    continue;
                }
                BlockInfo block;
                if(!blocks.containsKey(blockId)) {
                    block = new BlockInfo(blockId);
                    blocks.put(blockId, block);
                }

                block = blocks.get(blockId);
                       // blocks.get(blockId);
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
        return blocks;
    }
}
