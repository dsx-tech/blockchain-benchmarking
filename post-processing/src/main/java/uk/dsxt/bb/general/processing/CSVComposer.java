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

package uk.dsxt.bb.general.processing;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.general.model.GeneralInfo;
import uk.dsxt.bb.general.model.IntensityInfo;

import java.io.FileWriter;
import java.io.IOException;

@Log4j2
public class CSVComposer {

    public static final String RESULT_PATH = "post-processing/src/main/resources/results/general/csv/";
    //file names
    public static final String INTENSITIES_FILE = "intensities.csv";
    public static final String SIZE_FILE = "size.csv";
    public static final String NUMBER_OF_NODES_FILE = "numberOfNodes.csv";
    //headers
    private static final String[] INTENSIIES_HEADER = {"intensity", "dispersionType",
            "numberOfNodesType", "sizeType", "numberOfUnverifiedTransactions",
            "mediumDistributionTime", "mediumVerificationTime"};
    private static final String[] SIZE_HEADER = {"size", "dispersionType",
            "numberOfNodesType", "intensityType", "numberOfUnverifiedTransactions",
            "mediumDistributionTime", "mediumVerificationTime"};
    private static final String[] NUMBER_OF_NODES_HEADER = {"numberOfNodes", "intensityType",
            "sizeType", "numberOfUnverifiedTransactions", "mediumDistributionTime",
            "mediumVerificationTime"};

    private GeneralInfo generalInfo;

    public CSVComposer(GeneralInfo generalInfo) {
        this.generalInfo = generalInfo;
    }

    public void composeCSVs() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + INTENSITIES_FILE), ',', '\u0000')) {
            fillIntensitiesCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + SIZE_FILE), ',', '\u0000')) {
//            fillTransactionsCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(RESULT_PATH + NUMBER_OF_NODES_FILE), ',', '\u0000')) {
//            fillBlocksCSV(writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void fillIntensitiesCSV(CSVWriter writer) throws IOException {
        writer.writeNext(INTENSIIES_HEADER);
        for (IntensityInfo intensityInfo : generalInfo.getIntensities()) {
            String[] entry = {String.valueOf(intensityInfo.getIntensity()),
                    String.valueOf(intensityInfo.getIntensityDispersionType()),
                    String.valueOf(intensityInfo.getNumberOfNodesType()),
                    String.valueOf(intensityInfo.getTransactionSizeType()),
                    String.valueOf(intensityInfo.getNumberOfUnverifiedTransactions()),
                    String.valueOf(intensityInfo.getMediumDistributionTime()),
                    String.valueOf(intensityInfo.getMediumVerificationTime())};
            writer.writeNext(entry);
            writer.flush();
        }
    }
}
