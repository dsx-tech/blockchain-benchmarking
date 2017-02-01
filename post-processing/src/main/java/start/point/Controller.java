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
package start.point;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import model.BlockInfo;
import model.TransactionInfo;
import processing.Processor;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Controller {

    private  final static Logger log = LogManager.getLogger(Controller.class);
    private static final String LOAD_GENERATOR_FILE = "loadGenerator.csv";
    private static final String BLOCK_DISTRIBUTION_FILE = "blockDistribution.csv";
    private static final String INTENSITIES_FILE = "intensity.csv";
    private static final String TRANSACTIONS_FILE = "transactions.csv";
    private static final String BLOCKS_FILE = "blocks.csv";
    private static final String UNVERIFIED_TRANSACTIONS_FILE = "unverifiedTransactions.csv";

    public void run() {
        List<TransactionInfo> transactions = new ArrayList<>();
        List<BlockInfo> blocks = new ArrayList<>();
        //create CSVReader for each file
        try (CSVReader reader = new CSVReader(new FileReader(LOAD_GENERATOR_FILE), ',')) {
            //call corresponding parser
            transactions = parseLoadGeneratorCSV(reader);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVReader reader = new CSVReader(new FileReader(BLOCK_DISTRIBUTION_FILE), ',')) {
            //call corresponding parser
            blocks = parseBlockDistributionCSV(reader);
            //todo call blockchain API to fill empty fields in blockInfo
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        //call post-processor
        Processor processor = new Processor(transactions, blocks);
        processor.process();
        //save results to file
        try (CSVWriter writer = new CSVWriter(new FileWriter(INTENSITIES_FILE))) {
            fillIntensitiesCSV(writer, processor.getIntensities());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(TRANSACTIONS_FILE))) {
            fillTransactionsCSV(writer, processor.getTransactions());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(BLOCKS_FILE))) {
            fillBlocksCSV(writer, processor.getBlocks());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(UNVERIFIED_TRANSACTIONS_FILE))) {
            fillUnverifiedTransactionsCSV(writer, processor.getUnverifiedTransactions());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


    private List<TransactionInfo> parseLoadGeneratorCSV(CSVReader reader) throws IOException {
        String[] nextLine;
        List<TransactionInfo> transactions = new ArrayList<>();
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length < 6) {
                //ignore all unparseable lines
                log.error("");
                continue;
            }
            try {
                long time = Long.parseLong(nextLine[0]);
                long transactionId = Long.parseLong(nextLine[1]);
                int transactionSize = Integer.parseInt(nextLine[2]);
                int nodeId = Integer.parseInt(nextLine[3]);
                int responseCode = Integer.parseInt(nextLine[4]);
                String responseMessage = nextLine[5];
                transactions.add(new TransactionInfo(time, transactionId, transactionSize, nodeId, responseCode, responseMessage));
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
                log.error("");
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

    private void fillIntensitiesCSV(CSVWriter writer, Map<Long, Integer> intensities) throws IOException {
        SortedSet<Long> times = new TreeSet<>(intensities.keySet());
        for (Long time : times) {
            String[] entry = {String.valueOf(time), String.valueOf(intensities.get(time))};
            writer.writeNext(entry);
            writer.flush();
        }
    }


    private void fillTransactionsCSV(CSVWriter writer, List<TransactionInfo> transactionInfos) throws IOException {
        for (TransactionInfo transactionInfo : transactionInfos) {
            String[] entry = {String.valueOf(transactionInfo.getTransactionId()),
                    String.valueOf(transactionInfo.getBlockId()),
                    String.valueOf(transactionInfo.getTransactionSize()),
                    String.valueOf(transactionInfo.getTime()),
                    String.valueOf(transactionInfo.getNodeId())};
            writer.writeNext(entry);
            writer.flush();
        }
    }

    private void fillBlocksCSV(CSVWriter writer, List<BlockInfo> blockDistributionInfos) throws IOException {
        for (BlockInfo blockDistributionInfo : blockDistributionInfos) {
            String[] entry = {String.valueOf(blockDistributionInfo.getBlockId()),
                    String.valueOf(blockDistributionInfo.getMaxNodeTime()),
                    String.valueOf(blockDistributionInfo.getMaxNodeTime95()),
                    String.valueOf(blockDistributionInfo.getVerificationTime())};
            writer.writeNext(entry);
            writer.flush();
        }
    }

    private void fillUnverifiedTransactionsCSV(CSVWriter writer, Map<Long, Integer> unverifiedTransactions) throws IOException {
        SortedSet<Long> times = new TreeSet<>(unverifiedTransactions.keySet());
        for (Long time : times) {
            String[] entry = {String.valueOf(time), String.valueOf(unverifiedTransactions.get(time))};
            writer.writeNext(entry);
            writer.flush();
        }
    }
}
