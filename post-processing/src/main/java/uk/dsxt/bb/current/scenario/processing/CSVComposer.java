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
    private static final String[] TIME_HEADER = {"time", "throughput",
            "distributionLatency", "intensity", "transactionSize", "blockSize", "numberTransactionsInBlock"};
    private static final String[] TRANSACTIONS_HEADER = {"transactionId", "blockId",
            "transactionSize", "transactionCreationTime", "nodeId"};
    private static final String[] BLOCKS_HEADER = {"blockId", "creationTime", "maxNodeTime95", "maxNodeTime"};
    //todo don't recreate file every time
    private static final String[] GENERAL_HEADER = {"numberOfNodes", "maxThroughput", "mediumThroughput",
            "mediumIntensity", "mediumTransactionSize", "mediumBlockSize", "mediumLatency"};
    private BlockchainInfo blockchainInfo;

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
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + GENERAL_FILE), ',', '\u0000')) {
            fillGeneralCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void fillGeneralCSV(CSVWriter writer) throws IOException {
        writer.writeNext(GENERAL_HEADER);
        ScenarioInfo info = blockchainInfo.getScenarioInfo();
        String[] entry = {String.valueOf(info.getNumberOfNodes()),
                String.valueOf(info.getThroughputMax()),
                String.valueOf(info.getMediumThroughput()),
                String.valueOf(info.getMediumIntensity()),
                String.valueOf(info.getMediumTransactionSize()),
                String.valueOf(info.getMediumBlockSize()),
                String.valueOf(info.getMediumLatency())};
        writer.writeNext(entry);
        writer.flush();
    }

    private void fillTimeCSV(CSVWriter writer) throws IOException {
        writer.writeNext(TIME_HEADER);
        for (TimeInfo timeInfo : blockchainInfo.getTimeInfos().values()) {
            String[] entry = {String.valueOf(timeInfo.getTime()),
                    String.valueOf(timeInfo.getThroughput()),
                    String.valueOf(timeInfo.getLatency()),
                    String.valueOf(timeInfo.getIntensity()),
                    String.valueOf(timeInfo.getTransactionSize()),
                    String.valueOf(timeInfo.getBlockSize()),
                    String.valueOf(timeInfo.getNumberTransactionsInBlock())};
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
