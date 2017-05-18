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

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import uk.dsxt.bb.general.DirOrganizer;
import uk.dsxt.bb.scenario.proccessing.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static uk.dsxt.bb.properties.proccessing.PropertiesComparator.PROP_FILE_NAME;

@Log4j2
public class CSVComposer {

    public static final String CSV_DIR = "/csv/";

    //output file names
    private static final String TRANSACTIONS_FILE = "transactions.csv";
    private static final String BLOCKS_FILE = "blocks.csv";
    private static final String TIME_FILE = "time.csv";
    private static final String RESOURCES_FILE = "resources.csv";
    private static final String TRANSACTION_LATENCIES = "transLatencies.csv";
    private static final String THROUGHPUT = "throughput.csv";
    private static final String LATENCY_QUARTILS = "latencyQuartils.csv";

    //header lines
    private static final String[] TIME_HEADER = {"time", "blockGeneration", "throughput",
            "latency", "intensity", "transactionSize", "blockSize", "numberTransactionsInBlock", "transactionQueue"};
    private static final String[] TRANSACTIONS_HEADER = {"transactionId", "blockId",
            "transactionSize", "transactionCreationTime", "nodeId", "blockLatency"};
    private static final String[] BLOCKS_HEADER = {"blockId", "creationTime", "blockLatency"};
    private static final String[] RESOURCES_HEADER = {"time", "nodeId",
            "cpu", "usedMemory", "usedMemory%"
            , "downloaded", "uploaded"};
    private static final String[] TRANSACTION_LATENCIES_HEADER = {"latency"};
    private static final String[] THROUGHPUT_HEADER = {"throughput"};


    private BlockchainInfo blockchainInfo;

    public CSVComposer(BlockchainInfo blockchainInfo) {
        this.blockchainInfo = blockchainInfo;
    }

    public void composeCSVs(String resultDirName) {

        File dir = new File(resultDirName);
        String resultPath = createResultDir(dir.getName());
        if (resultPath == null) {
            return;
        }
        File resultDir = new File(resultPath);
        try {
            FileUtils.copyFile(
                    new File(dir.getCanonicalPath() + "/" + PROP_FILE_NAME),
                    new File(resultDir.getParentFile().getCanonicalPath()
                            + "/" + PROP_FILE_NAME));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(resultPath + TIME_FILE), ',', '\u0000')) {
            fillTimeCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(resultPath + TRANSACTIONS_FILE), ',', '\u0000')) {
            fillTransactionsCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(resultPath + BLOCKS_FILE), ',', '\u0000')) {
            fillBlocksCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(resultPath + RESOURCES_FILE), ',', '\u0000')) {
            fillResourcesCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(resultPath + TRANSACTION_LATENCIES), ',', '\u0000')) {
            fillLatencies(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(resultPath + THROUGHPUT), ',', '\u0000')) {
            fillThroughput(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(resultPath + LATENCY_QUARTILS), ',', '\u0000')) {
            fillLatencyQuartils(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void fillLatencyQuartils(CSVWriter writer) throws IOException {
        for (TransactionInfo tr : blockchainInfo.getTransactions().values()) {
            if (tr.getLatencyQuartils().size() <= 1) {
                continue;
            }
            String[] entry = new String[tr.getLatencyQuartils().size()];
            for (int i = 0; i < tr.getLatencyQuartils().size(); i++) {
                entry[i] = String.valueOf(tr.getLatencyQuartils().get(i + 2));
            }
            writer.writeNext(entry);
            writer.flush();
        }
    }

    private void fillThroughput(CSVWriter writer) throws IOException {
        writer.writeNext(THROUGHPUT_HEADER);
        for (TimeSegmentInfo tr : blockchainInfo.getTimeSegments().values()) {
            String[] entry = {String.valueOf(tr.getThroughput())};
            writer.writeNext(entry);
            writer.flush();
        }
    }


    private void fillLatencies(CSVWriter writer) throws IOException {
        writer.writeNext(TRANSACTION_LATENCIES_HEADER);
        for (TransactionInfo tr : blockchainInfo.getTransactions().values()) {
            if (tr.getLatency() == Double.POSITIVE_INFINITY) {
                continue;
            }
            String[] entry = {String.valueOf(tr.getLatency())};
            writer.writeNext(entry);
            writer.flush();
        }
    }

    private String createResultDir(String dirName) {
        String resPath = null;
        switch (blockchainInfo.getBlockchainType()) {
            case ETHEREUM:
                resPath = DirOrganizer.ETHEREUM_RESULTS_PATH + dirName + CSV_DIR;
                break;
            case FABRIC:
                resPath = DirOrganizer.FABRIC_RESULTS_PATH + dirName + CSV_DIR;
                break;
        }
        File resDir = new File(resPath);
        if (resDir.exists()) {
            log.error("Result directory with name " + dirName + " already exists");
            return null;
        }
        resDir.mkdirs();
        return resPath;
    }


    private void fillResourcesCSV(CSVWriter writer) throws IOException {
        writer.writeNext(RESOURCES_HEADER);
        for (ResourceInfo resourceInfo : blockchainInfo.getResources()) {
            String[] entry = {
                    String.valueOf(resourceInfo.getTime()),
                    String.valueOf(resourceInfo.getNodeId()),
                    String.valueOf(resourceInfo.getCpuPercent()),
                    String.valueOf(resourceInfo.getMem()),
                    String.valueOf(resourceInfo.getMemPercent()),
                    String.valueOf(resourceInfo.getDownloaded()),
                    String.valueOf(resourceInfo.getUploaded())};
            writer.writeNext(entry);
            writer.flush();
        }
    }

    private void fillTimeCSV(CSVWriter writer) throws IOException {
        writer.writeNext(TIME_HEADER);
        int i = 0;
        int start = (int) (blockchainInfo.getTimeSegments().values().size() * 0.05);
        int end = blockchainInfo.getTimeSegments().values().size() -
                (int) (blockchainInfo.getTimeSegments().values().size() * 0.05);
        for (TimeSegmentInfo timeSegmentInfo : blockchainInfo.getTimeSegments().values()) {
//            if (i < start || i > end) {
//                i++;
//                continue;
//            }
            String[] entry = {String.valueOf(timeSegmentInfo.getTime()),
                    String.valueOf(timeSegmentInfo.getBlockGenerationFrequency()),
                    String.valueOf(timeSegmentInfo.getThroughput()),
                    String.valueOf(timeSegmentInfo.getLatency()),
                    String.valueOf(timeSegmentInfo.getIntensity()),
                    String.valueOf(timeSegmentInfo.getTransactionSize()),
                    String.valueOf(timeSegmentInfo.getBlockSize()),
                    String.valueOf(timeSegmentInfo.getNumberTransactionsInBlock()),
                    String.valueOf(timeSegmentInfo.getTransactionQueueLength())};
            writer.writeNext(entry);
            writer.flush();
            // i++;
        }
    }

    private void fillTransactionsCSV(CSVWriter writer) throws IOException {
        writer.writeNext(TRANSACTIONS_HEADER);
        for (TransactionInfo transactionInfo : blockchainInfo.getTransactions().values()) {
            String[] entry = {String.valueOf(transactionInfo.getTransactionId()),
                    String.valueOf(transactionInfo.getBlockId()),
                    String.valueOf(transactionInfo.getTransactionSize()),
                    String.valueOf(transactionInfo.getTime()),
                    String.valueOf(transactionInfo.getNodeId()),
            String.valueOf(transactionInfo.getLatency())};
            writer.writeNext(entry);
            writer.flush();
        }
    }

    private void fillBlocksCSV(CSVWriter writer) throws IOException {
        writer.writeNext(BLOCKS_HEADER);
        for (BlockInfo blockDistributionInfo : blockchainInfo.getBlocks().values()) {
            String[] entry = {String.valueOf(blockDistributionInfo.getBlockId()),
                    String.valueOf(blockDistributionInfo.getCreationTime()),
                    String.valueOf(blockDistributionInfo.getLatency())};
            writer.writeNext(entry);
            writer.flush();
        }
    }
}
