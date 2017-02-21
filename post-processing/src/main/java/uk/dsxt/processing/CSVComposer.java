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

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.model.BlockInfo;
import uk.dsxt.model.BlockchainInfo;
import uk.dsxt.model.TimeInfo;
import uk.dsxt.model.TransactionInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

@Log4j2
public class CSVComposer {

    public static final String RESULT_PATH = "post-processing/src/main/resources/results/";
    //output file names
    private static final String INTENSITIES_FILE = "intensity.csv";
    private static final String TRANSACTIONS_FILE = "transactions.csv";
    private static final String BLOCKS_FILE = "blocks.csv";
    private static final String UNVERIFIED_TRANSACTIONS_FILE = "timeToUnverifiedTransactions.csv";
    private static final String NUMBER_OF_NODES_FILE = "numberOfNodes.csv";
    private static final String DISTRIBUTIONS_FILE = "distributions.csv";
    //header lines
    private static final String[] INTENSIIES_HEADER = {"time", "intensity"};
    private static final String[] TRANSACTIONS_HEADER = {"transactionId", "blockId",
            "transactionSize", "transactionCreationTime", "nodeId"};
    private static final String[] BLOCKS_HEADER = {"blockId", "creationTime", "maxNodeTime95", "maxNodeTime", "verificationTime"};
    private static final String[] UNVERIFIED_HEADER = {"time", "numberOfUnverifiedTransactions"};
    private static final String[] NUMBER_OF_NODES_HEADER = {"time", "numberOfNodes"};
    private static final String[] DISTRIBUTIONS_HEADER = {"time", "minSize", "maxSize", "distributionTime95", "maxDistributionTime"};
    private BlockchainInfo blockchainInfo;

    public CSVComposer(BlockchainInfo blockchainInfo) {
        this.blockchainInfo = blockchainInfo;
    }

    public void composeCSVs() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + INTENSITIES_FILE), ',', '\u0000')) {
            fillIntensitiesCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + TRANSACTIONS_FILE), ',', '\u0000')) {
            fillTransactionsCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + BLOCKS_FILE), ',', '\u0000')) {
            fillBlocksCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + UNVERIFIED_TRANSACTIONS_FILE), ',', '\u0000')) {
            fillUnverifiedTransactionsCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + NUMBER_OF_NODES_FILE), ',', '\u0000')) {
            fillNumberOfNodesCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + DISTRIBUTIONS_FILE), ',', '\u0000')) {
            fillDistributionsCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void fillDistributionsCSV(CSVWriter writer) throws IOException {
        writer.writeNext(DISTRIBUTIONS_HEADER);
        SortedSet<Long> times = new TreeSet<>(blockchainInfo.getTimeInfos().keySet());
        for (Long time : times) {
            SortedSet<Integer> sizes = new TreeSet<>(blockchainInfo.getTimeInfos().get(time).keySet());
            for (Integer size : sizes) {
                TimeInfo t = blockchainInfo.getTimeInfos().get(time).get(size);
                String[] entry = {String.valueOf(time), String.valueOf(t.getTimeAndSize().getSizeSpan().getBlockSizeMin()),
                        String.valueOf(t.getTimeAndSize().getSizeSpan().getBlockSizeMax()),
                        String.valueOf(t.getMediumDstrbTime95()), String.valueOf(t.getMediumDstrbTime100())};
                writer.writeNext(entry);
                writer.flush();
            }
        }
    }

    private void fillNumberOfNodesCSV(CSVWriter writer) throws IOException {
        writer.writeNext(NUMBER_OF_NODES_HEADER);
        SortedSet<Long> times = new TreeSet<>(blockchainInfo.getTimeToNumNodes().keySet());
        for (Long time : times) {
            String[] entry = {String.valueOf(time), String.valueOf(blockchainInfo.getTimeToNumNodes().get(time))};
            writer.writeNext(entry);
            writer.flush();
        }
    }

    private void fillIntensitiesCSV(CSVWriter writer) throws IOException {
        writer.writeNext(INTENSIIES_HEADER);
        SortedSet<Long> times = new TreeSet<>(blockchainInfo.getTimeToIntensities().keySet());
        for (Long time : times) {
            String[] entry = {String.valueOf(time), String.valueOf(blockchainInfo.getTimeToIntensities().get(time))};
            writer.writeNext(entry);
            writer.flush();
        }
    }


    private void fillTransactionsCSV(CSVWriter writer) throws IOException {
        writer.writeNext(TRANSACTIONS_HEADER);
        for (TransactionInfo transactionInfo : blockchainInfo.getTransactions().values()) {
            String[] entry = {String.valueOf(transactionInfo.getTransactionId()),
                    String.valueOf(transactionInfo.getBlockId()),
                    String.valueOf(transactionInfo.getTransactionSize()),
                    String.valueOf(transactionInfo.getTime()),
                    String.valueOf(transactionInfo.getNodeId())};
            writer.writeNext(entry);
            writer.flush();
        }
    }

    private void fillBlocksCSV(CSVWriter writer) throws IOException {
        writer.writeNext(BLOCKS_HEADER);
        for (BlockInfo blockDistributionInfo : blockchainInfo.getBlocks().values()) {
            String[] entry = {String.valueOf(blockDistributionInfo.getBlockId()),
                    String.valueOf(blockDistributionInfo.getCreationTime()),
                    String.valueOf(blockDistributionInfo.getDistributionTime95()),
                    String.valueOf(blockDistributionInfo.getDistributionTime100()),
                    String.valueOf(blockDistributionInfo.getVerificationTime())};
            writer.writeNext(entry);
            writer.flush();
        }
    }

    private void fillUnverifiedTransactionsCSV(CSVWriter writer) throws IOException {
        writer.writeNext(UNVERIFIED_HEADER);
        SortedSet<Long> times = new TreeSet<>(blockchainInfo.getTimeToUnverifiedTransactions().keySet());
        for (Long time : times) {
            String[] entry = {String.valueOf(time), String.valueOf(blockchainInfo.getTimeToUnverifiedTransactions().get(time))};
            writer.writeNext(entry);
            writer.flush();
        }
    }
}
