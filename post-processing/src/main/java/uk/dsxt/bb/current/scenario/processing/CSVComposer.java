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

package uk.dsxt.bb.current.scenario.processing;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.current.scenario.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Log4j2
public class CSVComposer {

    public static final String RESULT_PATH = "post-processing/src/main/resources/results/csv/";
    //output file names
    private static final String TRANSACTIONS_FILE = "transactions.csv";
    private static final String BLOCKS_FILE = "blocks.csv";
    private static final String TIME_FILE = "time.csv";
    private static final String GENERAL_FILE = "general.csv";

    //header lines
    private static final String[] TIME_HEADER = {"time", "throughput","throughputDistributed",
            "distributionLatency", "intensity", "transactionSize", "blockSize", "numberTransactionsInBlock"};
    private static final String[] TRANSACTIONS_HEADER = {"transactionId", "blockId",
            "transactionSize", "transactionCreationTime", "nodeId"};
    private static final String[] BLOCKS_HEADER = {"blockId", "creationTime", "latency95", "latencyMax"};
    //todo don't recreate file every time
    private static final String[] GENERAL_HEADER = {"numberOfNodes", "maxThroughput", "averageThroughput",
            "averageIntensity", "averageTransactionSize", "averageBlockSize", "averageLatency"};
    private BlockchainInfo blockchainInfo;

    private boolean generalExists = false;

    public CSVComposer(BlockchainInfo blockchainInfo) {
        this.blockchainInfo = blockchainInfo;
    }

    public void composeCSVs() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + TIME_FILE), ',', '\u0000')) {
            fillTimeCSV(writer);
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
        generalExists = new File(RESULT_PATH + GENERAL_FILE).exists();
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + GENERAL_FILE, true), ',', '\u0000')) {
            fillGeneralCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void fillGeneralCSV(CSVWriter writer) throws IOException {
        if(!generalExists) {
            writer.writeNext(GENERAL_HEADER);
        }
        ScenarioInfo info = blockchainInfo.getScenarioInfo();
        String[] entry = {String.valueOf(info.getNumberOfNodes()),
                String.valueOf(info.getMaxThroughput()),
                String.valueOf(info.getAverageThroughput()),
                String.valueOf(info.getAverageIntensity()),
                String.valueOf(info.getAverageTransactionSize()),
                String.valueOf(info.getAverageBlockSize()),
                String.valueOf(info.getAverageLatency())};
        writer.writeNext(entry);
        writer.flush();
    }

    private void fillTimeCSV(CSVWriter writer) throws IOException {
        writer.writeNext(TIME_HEADER);
        for (TimeSegmentInfo timeSegmentInfo : blockchainInfo.getTimeSegments().values()) {
            String[] entry = {String.valueOf(timeSegmentInfo.getTime()),
                    String.valueOf(timeSegmentInfo.getThroughput()),
                    String.valueOf(timeSegmentInfo.getDistributionThroughput()),
                    String.valueOf(timeSegmentInfo.getLatency()),
                    String.valueOf(timeSegmentInfo.getIntensity()),
                    String.valueOf(timeSegmentInfo.getTransactionSize()),
                    String.valueOf(timeSegmentInfo.getBlockSize()),
                    String.valueOf(timeSegmentInfo.getNumberTransactionsInBlock())};
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
                    String.valueOf(blockDistributionInfo.getDistributionTime100())};
            writer.writeNext(entry);
            writer.flush();
        }
    }
}
