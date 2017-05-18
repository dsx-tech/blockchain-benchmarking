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
import uk.dsxt.bb.properties.proccessing.model.PropertiesFileInfo;
import uk.dsxt.bb.properties.proccessing.model.ResultType;
import uk.dsxt.bb.scenario.proccessing.model.ScenarioInfo;

import static uk.dsxt.bb.general.DirOrganizer.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


@Log4j2
public class GeneralCSVComposer {

    private static final String INTENSITY_FILE = "intensityScenarioList.csv";
    private static final String SIZE_FILE = "transactionSizeScenarioList.csv";
    private static final String SCALABILITY_FILE = "scalabilityScenarioList.csv";

    //    private static final String RESOURCES_INTENSITY_FILE = "resources_intensity.csv";
//    private static final String RESOURCES_SIZE_FILE = "resources_size.csv";
//    private static final String RESOURCES_SCALABILITY_FILE = "resources_scalability.csv";
//
//    private static final String[] INTENSITY_HEADER = {"blockchainType", "intensity", "maxThroughput",
//            "per90Throughput", "averageThroughput",
//            "maxLatency", "per90Latency", "averageLatency", "averageQueueInc"};
//
//    private static final String[] SIZE_HEADER = {"blockchainType", "transactionSize", "maxThroughput",
//            "per90Throughput", "averageThroughput",
//            "maxLatency", "per90Latency", "averageLatency", "averageQueueInc"};
//
//    private static final String[] SCALABILITY_HEADER = {"blockchainType", "numberOfNodes", "averageThroughput",
//            "averageLatency", "averageQueueInc"};
//
//    private static final String[] INTENSITY_R_HEADER = {"blockchainType", "intensity", "averageCPU",
//            "averageMem", "averageMemPercent", "averageIn", "averageOut"};
//
//    private static final String[] SIZE_R_HEADER = {"blockchainType", "transactionSize", "averageCPU",
//            "averageMem", "averageMemPercent", "averageIn", "averageOut"};
//
    private static final String[] HEADER = {"dirs"};


//    public static boolean createResultFiles() {
//        try {
//            tryCreateFile(GENERAL_RESULTS_PATH, GENERAL_INTENSITY_FILE, INTENSITY_HEADER);
//            tryCreateFile(GENERAL_RESULTS_PATH, GENERAL_SIZE_FILE, SIZE_HEADER);
//            tryCreateFile(GENERAL_RESULTS_PATH, GENERAL_SCALABILITY_FILE, SCALABILITY_HEADER);
//            tryCreateFile(GENERAL_RESOURCES_PATH, RESOURCES_INTENSITY_FILE, INTENSITY_R_HEADER);
//            tryCreateFile(GENERAL_RESOURCES_PATH, RESOURCES_SIZE_FILE, SIZE_R_HEADER);
//            tryCreateFile(GENERAL_RESOURCES_PATH, RESOURCES_SCALABILITY_FILE, SCALABILITY_R_HEADER);
//        } catch (IOException e) {
//            log.error(e.getMessage());
//            return false;
//        }
//        return true;
//    }

    private static void tryCreateFile(String path, String name, String[] header) throws IOException {
        File file = new File(path + name);
        file.createNewFile();
        CSVWriter writer = new CSVWriter(new FileWriter(file),
                ',', '\u0000');
        writer.writeNext(header);
        writer.flush();
    }

    public static boolean createScenarioListFiles() {
        try {
            String path = DirOrganizer.ETHEREUM_RESULTS_PATH;
            tryCreateFile(path, INTENSITY_FILE, HEADER);
            tryCreateFile(path, SIZE_FILE, HEADER);
            tryCreateFile(path, SCALABILITY_FILE, HEADER);

            path = DirOrganizer.FABRIC_RESULTS_PATH;
            tryCreateFile(path, INTENSITY_FILE, HEADER);
            tryCreateFile(path, SIZE_FILE, HEADER);
            tryCreateFile(path, SCALABILITY_FILE, HEADER);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public static void addScenarioDirsInfo(PropertiesFileInfo properties, ResultType type) {
        String path = null;
        switch (properties.getBlockchainType()) {
            case ETHEREUM:
                path = DirOrganizer.ETHEREUM_RESULTS_PATH;
                break;
            case FABRIC:
                path = DirOrganizer.FABRIC_RESULTS_PATH;
                break;
        }
        switch (type) {
            case INTENSITY:
                path += INTENSITY_FILE;
                break;
            case SIZE:
                path += SIZE_FILE;
                break;
            case SCALABILITY:
                path += SCALABILITY_FILE;
                break;
            case OTHERS:
                return;
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter
                (path, true),
                ',', '\u0000')) {
            File file = new File(properties.getPathToScenarioDir());
            String[] entry = {file.getName()};
            writer.writeNext(entry);
            writer.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

//    public static void addScenarioInfo(ScenarioInfo scenarioInfo, ResultType type) {
//        String path = null;
//        String resPath = null;
//        switch (type) {
//            case INTENSITY:
//                path = GENERAL_INTENSITY_FILE;
//                resPath = RESOURCES_INTENSITY_FILE;
//                break;
//            case SIZE:
//                path = GENERAL_SIZE_FILE;
//                resPath = RESOURCES_SIZE_FILE;
//                break;
//            case SCALABILITY:
//                path = GENERAL_SCALABILITY_FILE;
//                resPath = RESOURCES_SCALABILITY_FILE;
//                break;
//        }
//        try (CSVWriter writer = new CSVWriter(new FileWriter
//                (GENERAL_RESULTS_PATH + path, true),
//                ',', '\u0000')) {
//            switch (type) {
//                case INTENSITY:
//                    fillIntensityCSV(scenarioInfo, writer);
//                    break;
//                case SIZE:
//                    fillSizeCSV(scenarioInfo, writer);
//                    break;
//                case SCALABILITY:
//                    fillScalabilityCSV(scenarioInfo, writer);
//                    break;
//            }
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//        try (CSVWriter writer = new CSVWriter(new FileWriter
//                (GENERAL_RESOURCES_PATH + resPath, true),
//                ',', '\u0000')) {
//            switch (type) {
//                case INTENSITY:
//                    fillIntensityResourcesCSV(scenarioInfo, writer);
//                    break;
//                case SIZE:
//                    fillSizeResourcesCSV(scenarioInfo, writer);
//                    break;
//                case SCALABILITY:
//                    fillScalaResourcesCSV(scenarioInfo, writer);
//                    break;
//            }
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }

    private static void fillScalaResourcesCSV(ScenarioInfo scenarioInfo, CSVWriter writer)
            throws IOException {
        String[] entry = {
                String.valueOf(scenarioInfo.getBlockchainType()),
                String.valueOf(scenarioInfo.getNumberOfNodes()),
                String.valueOf(scenarioInfo.getAverageCPU()),
                String.valueOf(scenarioInfo.getAverageMem()),
                String.valueOf(scenarioInfo.getAverageMemPercent()),
                String.valueOf(scenarioInfo.getAverageIn()),
                String.valueOf(scenarioInfo.getAverageOut())};
        writer.writeNext(entry);
        writer.flush();
    }

    private static void fillSizeResourcesCSV(ScenarioInfo scenarioInfo, CSVWriter writer)
            throws IOException {
        String[] entry = {
                String.valueOf(scenarioInfo.getBlockchainType()),
                String.valueOf(scenarioInfo.getAverageTransactionSize()),
                String.valueOf(scenarioInfo.getAverageCPU()),
                String.valueOf(scenarioInfo.getAverageMem()),
                String.valueOf(scenarioInfo.getAverageMemPercent()),
                String.valueOf(scenarioInfo.getAverageIn()),
                String.valueOf(scenarioInfo.getAverageOut())};
        writer.writeNext(entry);
        writer.flush();
    }

    private static void fillIntensityResourcesCSV(ScenarioInfo scenarioInfo, CSVWriter writer)
            throws IOException {
        String[] entry = {
                String.valueOf(scenarioInfo.getBlockchainType()),
                String.valueOf(scenarioInfo.getIntensity()),
                String.valueOf(scenarioInfo.getAverageCPU()),
                String.valueOf(scenarioInfo.getAverageMem()),
                String.valueOf(scenarioInfo.getAverageMemPercent()),
                String.valueOf(scenarioInfo.getAverageIn()),
                String.valueOf(scenarioInfo.getAverageOut())};
        writer.writeNext(entry);
        writer.flush();
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
                String.valueOf(scenarioInfo.getAverageTransactionSize()),
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
