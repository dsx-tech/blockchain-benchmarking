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
package processing;

import au.com.bytecode.opencsv.CSVReader;
import model.BlockInfo;
import model.BlockchainInfo;
import model.TransactionInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVParser {

    //input files
    private static final String PATH = "post-processing/src/main/resources/";
    private static final String LOAD_GENERATOR_FILE = "loadGeneratorOutput.csv";
    private static final String BLOCK_DISTRIBUTION_FILE = "blockDistribution.csv";
    private static final String NODES_FILE = "nodes.csv";
    private static final String BLOCKCHAIN_INFO_FILE = "blockchainInfo.csv";
    private BlockchainInfo blockchainInfo;
    private final static Logger log = LogManager.getLogger(CSVParser.class);

    public CSVParser() {
        List<TransactionInfo> transactions = new ArrayList<>();
        List<BlockInfo> blocks = new ArrayList<>();
        blockchainInfo = new BlockchainInfo(blocks, transactions);
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
        return blockchainInfo;
    }

    private List<BlockInfo> parseBlockchainInfo(CSVReader reader) throws IOException {
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
                int blockId = Integer.parseInt(nextLine[1]);
                BlockInfo block = blockchainInfo.getBlockById(blockId);
                TransactionInfo transaction = blockchainInfo.getTransactionById(transactionId);

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

    private List<TransactionInfo> parseLoadGeneratorCSV(CSVReader reader) throws IOException {
        String[] nextLine;
        List<TransactionInfo> transactions = new ArrayList<>();
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

                transactions.add(new TransactionInfo(time, transactionId,
                        transactionSize, nodeId, responseCode, responseMessage));
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                //ignore all unparseable lines
            }
        }
        return transactions;
    }

    private List<BlockInfo> parseBlockDistributionCSV(CSVReader reader) throws IOException {
        String[] nextLine;
        List<BlockInfo> blocks = new ArrayList<>();
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length <= 0) {
                //ignore all unparseable lines
                log.error("Unparseable line: " + Arrays.toString(nextLine));
                continue;
            }
            try {
                int blockId = Integer.parseInt(nextLine[0]);
                List<BlockInfo.NodeTime> nodeTimes = new ArrayList<>();

                for (int i = 1; i < nextLine.length; i++) {
                    if (!nextLine[i].isEmpty()) {
                        nodeTimes.add(new BlockInfo.NodeTime(i, Long.parseLong(nextLine[i])));
                    }
                }
                blocks.add(new BlockInfo(blockId, nodeTimes));
            } catch (NumberFormatException e) {
                //ignore all unparseable lines
                log.error(e.getMessage());
            }
        }
        return blocks;
    }
}
