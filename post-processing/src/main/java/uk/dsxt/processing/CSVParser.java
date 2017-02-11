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
package uk.dsxt.processing;

import au.com.bytecode.opencsv.CSVReader;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.model.BlockInfo;
import uk.dsxt.model.BlockchainInfo;
import uk.dsxt.model.NodeInfo;
import uk.dsxt.model.TransactionInfo;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Log4j2
public class CSVParser {

    //input files
    private static final String PATH = "post-processing/src/main/resources/";
    private static final String LOAD_GENERATOR_FILE = "loadGeneratorOutput.csv";
    private static final String BLOCK_DISTRIBUTION_FILE = "blockDistribution.csv";
    private static final String NODES_FILE = "nodes.csv";
    private static final String BLOCKCHAIN_INFO_FILE = "blockchainInfo.csv";
    private BlockchainInfo blockchainInfo;

    public CSVParser() {
        Map<Long, TransactionInfo> transactions = new HashMap<>();
        Map<Long, BlockInfo> blocks = new HashMap<>();
        List<NodeInfo> nodes = new ArrayList<>();
        blockchainInfo = new BlockchainInfo(blocks, transactions, nodes);
    }

    public BlockchainInfo parseCSVs() {
        //create CSVReader for each file
        try (CSVReader reader = new CSVReader(new FileReader(PATH + LOAD_GENERATOR_FILE), ',')) {
            //call corresponding parser
            blockchainInfo.setTransactions(parseLoadGeneratorCSV(reader));
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        try (CSVReader reader = new CSVReader(new FileReader(PATH + BLOCK_DISTRIBUTION_FILE), ',')) {
            //call corresponding parser
            blockchainInfo.setBlocks(parseBlockDistributionCSV(reader));
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        try (CSVReader reader = new CSVReader(new FileReader(PATH + BLOCKCHAIN_INFO_FILE), ',')) {
            //call corresponding parser
            blockchainInfo.setBlocks(parseBlockchainInfo(reader));
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        try (CSVReader reader = new CSVReader(new FileReader(PATH + NODES_FILE), ',')) {
            //call corresponding parser
            blockchainInfo.setNodes(parseNodesCSV(reader));
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        return blockchainInfo;
    }

    private Map<Long, BlockInfo> parseBlockchainInfo(CSVReader reader) throws IOException {
        String[] nextLine;
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length < 2) {
                //ignore all unparseable lines
                log.error("Unparseable line: " + Arrays.toString(nextLine));
                continue;
            }
            try {
                long transactionId = Long.parseLong(nextLine[0]);
                long blockId = Integer.parseInt(nextLine[1]);
                BlockInfo block = blockchainInfo.getBlocks().get(blockId);
                TransactionInfo transaction = blockchainInfo.getTransactions().get(transactionId);

                if (!nextLine[2].isEmpty() && block != null) {
                    int parentBlockId = Integer.parseInt(nextLine[2]);
                    block.setParentBlockId(parentBlockId);
                }
                if (block != null && transaction != null) {
                    block.addTransaction(transaction);
                }
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                //ignore all unparseable lines
            }
        }
        return blockchainInfo.getBlocks();
    }

    private Map<Long, TransactionInfo> parseLoadGeneratorCSV(CSVReader reader) throws IOException {
        String[] nextLine;
        Map<Long, TransactionInfo> transactions = new HashMap<>();
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length < 6) {
                //ignore all unparseable lines
                log.error("Unparseable line: " + Arrays.toString(nextLine));
                continue;
            }
            try {
                long time = Long.parseLong(nextLine[0]);
                long transactionId = Long.parseLong(nextLine[1]);
                int transactionSize = Integer.parseInt(nextLine[2]);
                int nodeId = Integer.parseInt(nextLine[3]);
                int responseCode = Integer.parseInt(nextLine[4]);
                String responseMessage = nextLine[5];

                transactions.put(transactionId, new TransactionInfo(time, transactionId,
                        transactionSize, nodeId, responseCode, responseMessage));
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                //ignore all unparseable lines
            }
        }
        return transactions;
    }

    private Map<Long, BlockInfo> parseBlockDistributionCSV(CSVReader reader) throws IOException {
        String[] nextLine;
        Map<Long, BlockInfo> blocks = new HashMap<>();
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length <= 0) {
                //ignore all unparseable lines
                log.error("Unparseable line: " + Arrays.toString(nextLine));
                continue;
            }
            try {
                long blockId = Integer.parseInt(nextLine[0]);
                List<BlockInfo.NodeTime> nodeTimes = new ArrayList<>();

                for (int i = 1; i < nextLine.length; i++) {
                    if (!nextLine[i].isEmpty()) {
                        nodeTimes.add(new BlockInfo.NodeTime(i, Long.parseLong(nextLine[i])));
                    }
                }
                blocks.put(blockId, new BlockInfo(blockId, nodeTimes));
            } catch (NumberFormatException e) {
                //ignore all unparseable lines
                log.error(e.getMessage());
            }
        }
        return blocks;
    }

    private List<NodeInfo> parseNodesCSV(CSVReader reader) throws IOException {
        String[] nextLine;
        List<NodeInfo> nodes = new ArrayList<>();
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length < 3) {
                //ignore all unparseable lines
                log.error("Unparseable line: " + Arrays.toString(nextLine));
                continue;
            }
            try {
                long time = Long.parseLong(nextLine[0]);
                int nodeId = Integer.parseInt(nextLine[1]);
                String state = nextLine[2];
                if (!state.equals("start") && !state.equals("stop")) {
                    log.error("Unparseable line: " + Arrays.toString(nextLine));
                    continue;
                }
                NodeInfo node = getNodeById(nodeId, nodes);
                if (node == null) {
                    node = new NodeInfo(nodeId);
                    nodes.add(node);
                }
                if (state.equals("start")) {
                    node.addStartTime(time);
                } else {
                    node.addStopTime(time);
                }
            } catch (NumberFormatException e) {
                //ignore all unparseable lines
                log.error(e.getMessage());
            }
        }
        return nodes;
    }

    private NodeInfo getNodeById(int id, List<NodeInfo> nodes) {
        for (NodeInfo node : nodes) {
            if (node.getNodeId() == id) {
                return node;
            }
        }
        return null;
    }
}
