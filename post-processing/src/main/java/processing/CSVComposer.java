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

import au.com.bytecode.opencsv.CSVWriter;
import model.BlockInfo;
import model.BlockchainInfo;
import model.TransactionInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

public class CSVComposer {

    private static final String PATH = "post-processing/src/main/resources/";
    //output files
    private static final String INTENSITIES_FILE = "intensity.csv";
    private static final String TRANSACTIONS_FILE = "transactions.csv";
    private static final String BLOCKS_FILE = "blocks.csv";
    private static final String UNVERIFIED_TRANSACTIONS_FILE = "timeToUnverifiedTransactions.csv";
    private final static Logger log = LogManager.getLogger(CSVComposer.class);
    private BlockchainInfo blockchainInfo;

    public CSVComposer(BlockchainInfo blockchainInfo) {
        this.blockchainInfo = blockchainInfo;
    }

    public void composeCSVs() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(PATH + INTENSITIES_FILE))) {
            fillIntensitiesCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(PATH + TRANSACTIONS_FILE))) {
            fillTransactionsCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(PATH + BLOCKS_FILE))) {
            fillBlocksCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(PATH + UNVERIFIED_TRANSACTIONS_FILE))) {
            fillUnverifiedTransactionsCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void fillIntensitiesCSV(CSVWriter writer) throws IOException {
        SortedSet<Long> times = new TreeSet<>(blockchainInfo.getTimeToIntensities().keySet());
        for (Long time : times) {
            String[] entry = {String.valueOf(time), String.valueOf(blockchainInfo.getTimeToIntensities().get(time))};
            writer.writeNext(entry);
            writer.flush();
        }
    }


    private void fillTransactionsCSV(CSVWriter writer) throws IOException {
        for (TransactionInfo transactionInfo : blockchainInfo.getTransactions()) {
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
        for (BlockInfo blockDistributionInfo : blockchainInfo.getBlocks()) {
            String[] entry = {String.valueOf(blockDistributionInfo.getBlockId()),
                    String.valueOf(blockDistributionInfo.getMaxNodeTime95()),
                    String.valueOf(blockDistributionInfo.getMaxNodeTime()),
                    String.valueOf(blockDistributionInfo.getVerificationTime())};
            writer.writeNext(entry);
            writer.flush();
        }
    }

    private void fillUnverifiedTransactionsCSV(CSVWriter writer) throws IOException {
        SortedSet<Long> times = new TreeSet<>(blockchainInfo.getTimeToUnverifiedTransactions().keySet());
        for (Long time : times) {
            String[] entry = {String.valueOf(time), String.valueOf(blockchainInfo.getTimeToUnverifiedTransactions().get(time))};
            writer.writeNext(entry);
            writer.flush();
        }
    }
}
