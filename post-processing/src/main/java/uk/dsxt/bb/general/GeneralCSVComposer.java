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
package uk.dsxt.bb.general;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.properties.proccessing.model.ResultType;
import uk.dsxt.bb.scenario.proccessing.model.ScenarioInfo;

import static uk.dsxt.bb.general.DirOrganizer.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


@Log4j2
public class GeneralCSVComposer {

    private static final String GENERAL_INTENSITY_FILE = "intensity.csv";
    private static final String GENERAL_SIZE_FILE = "size.csv";
    private static final String GENERAL_SCALABILITY_FILE = "scalability.csv";

    private static final String[] INTENSITY_HEADER = {"blockchainType", "intensity", "maxThroughput",
            "per90Throughput", "averageThroughput",
            "maxLatency", "per90Latency", "averageLatency", "averageQueueInc"};

    private static final String[] SIZE_HEADER = {"blockchainType", "transactionSize", "maxThroughput",
            "per90Throughput", "averageThroughput",
            "maxLatency", "per90Latency", "averageLatency", "averageQueueInc"};

    private static final String[] SCALABILITY_HEADER = {"blockchainType", "numberOfNodes", "averageThroughput",
            "averageLatency", "averageQueueInc"};


    public static boolean createGeneralResultFiles() {
        File generalResDir = new File(GENERAL_RESULTS_PATH);
        if (!generalResDir.exists() || generalResDir.isFile()) {
            if (!generalResDir.mkdirs()) {
                log.error("Couldn't create general results directory");
                return false;
            }
        }
        try {
            tryCreateFile(GENERAL_INTENSITY_FILE, INTENSITY_HEADER);
            tryCreateFile(GENERAL_SIZE_FILE, SIZE_HEADER);
            tryCreateFile(GENERAL_SCALABILITY_FILE, SCALABILITY_HEADER);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    private static void tryCreateFile(String name, String[] header) throws IOException {
        File file = new File(GENERAL_RESULTS_PATH + name);
        file.createNewFile();
        CSVWriter writer = new CSVWriter(new FileWriter(file),
                ',', '\u0000');
        writer.writeNext(header);
        writer.flush();
    }


    public static void addToIntensity(ScenarioInfo scenarioInfo, ResultType type) {
        String path = null;
        switch (type) {
            case INTENSITY:
                path = GENERAL_INTENSITY_FILE;
                break;
            case SIZE:
                path = GENERAL_SIZE_FILE;
                break;
            case SCALABILITY:
                path = GENERAL_SCALABILITY_FILE;
                break;
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter
                (GENERAL_RESULTS_PATH + path, true),
                ',', '\u0000')) {
            switch (type) {
                case INTENSITY:
                    fillIntensityCSV(scenarioInfo, writer);
                    break;
                case SIZE:
                    fillSizeCSV(scenarioInfo, writer);
                    break;
                case SCALABILITY:
                    fillScalabilityCSV(scenarioInfo, writer);
                    break;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static void fillScalabilityCSV(ScenarioInfo scenarioInfo, CSVWriter writer)
            throws IOException {
        String[] entry = {
                String.valueOf(scenarioInfo.getBlockchainType()),
                String.valueOf(scenarioInfo.getNumberOfNodes()),
                String.valueOf(scenarioInfo.getAverageThroughput()),
                String.valueOf(scenarioInfo.getAverageLatency()),
                String.valueOf(scenarioInfo.getAverageQueueInc())};
        writer.writeNext(entry);
        writer.flush();
    }

    private static void fillIntensityCSV(ScenarioInfo scenarioInfo, CSVWriter writer)
            throws IOException {
        String[] entry = {
                String.valueOf(scenarioInfo.getBlockchainType()),
                String.valueOf(scenarioInfo.getIntensity()),
                String.valueOf(scenarioInfo.getMaxThroughput()),
                String.valueOf(scenarioInfo.getPer95Throughput()),
                String.valueOf(scenarioInfo.getAverageThroughput()),
                String.valueOf(scenarioInfo.getMaxLatency()),
                String.valueOf(scenarioInfo.getPer95Latency()),
                String.valueOf(scenarioInfo.getAverageLatency()),
                String.valueOf(scenarioInfo.getAverageQueueInc())};
        writer.writeNext(entry);
        writer.flush();
    }

    private static void fillSizeCSV(ScenarioInfo scenarioInfo, CSVWriter writer)
            throws IOException {
        String[] entry = {
                String.valueOf(scenarioInfo.getBlockchainType()),
                String.valueOf(scenarioInfo.getMaxTransactionSize()),
                String.valueOf(scenarioInfo.getMaxThroughput()),
                String.valueOf(scenarioInfo.getPer95Throughput()),
                String.valueOf(scenarioInfo.getAverageThroughput()),
                String.valueOf(scenarioInfo.getMaxLatency()),
                String.valueOf(scenarioInfo.getPer95Latency()),
                String.valueOf(scenarioInfo.getAverageLatency()),
                String.valueOf(scenarioInfo.getAverageQueueInc())};
        writer.writeNext(entry);
        writer.flush();
    }
}
