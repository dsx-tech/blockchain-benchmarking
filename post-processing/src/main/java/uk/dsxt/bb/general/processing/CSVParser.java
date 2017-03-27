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

import au.com.bytecode.opencsv.CSVReader;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.general.model.GeneralInfo;
import uk.dsxt.bb.general.model.IntensityInfo;
import uk.dsxt.bb.general.model.enums.IntensityDispersionType;
import uk.dsxt.bb.general.model.enums.NumberOfNodesType;
import uk.dsxt.bb.general.model.enums.TransactionSizeType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
public class CSVParser {

    private static final String PATH = "post-processing/src/main/resources/general.result/";
    private static final String INTENSITIES_FILE = "intensities.csv";

    public static GeneralInfo parseCSVs() {
        GeneralInfo generalInfo = new GeneralInfo();
        //parse intensities.csv
        File intensitiesFile = new File(PATH + INTENSITIES_FILE);
        if (intensitiesFile.exists() && intensitiesFile.isFile()) {
            try (CSVReader reader = new CSVReader(new FileReader(PATH + INTENSITIES_FILE), ',')) {
                //call corresponding parser
                generalInfo.setIntensities(parseIntensities(reader));
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }
        } else {
            generalInfo.setIntensities(new ArrayList<>());
        }
        return generalInfo;
    }

    private static List<IntensityInfo> parseIntensities(CSVReader reader) throws IOException {
        String[] nextLine;
        List<IntensityInfo> intensities = new ArrayList<>();
        //Read one line at a time
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length < 7) {
                //ignore all unparseable lines
                log.error("Unparseable line: " + Arrays.toString(nextLine));
                continue;
            }
            try {
                int intensity = Integer.parseInt(nextLine[0]);
                IntensityDispersionType intensityDispersionType;

                if (nextLine[1].equals("HIGH")) {
                    intensityDispersionType = IntensityDispersionType.HIGH;
                } else {
                    intensityDispersionType = IntensityDispersionType.LOW;
                }

                NumberOfNodesType numberOfNodesType;

                if (nextLine[1].equals("FEW")) {
                    numberOfNodesType = NumberOfNodesType.FEW;
                } else if (nextLine[1].equals("SOME")) {
                    numberOfNodesType = NumberOfNodesType.SOME;
                } else {
                    numberOfNodesType = NumberOfNodesType.MANY;
                }

                TransactionSizeType transactionSizeType;

                if (nextLine[1].equals("BIG")) {
                    transactionSizeType = TransactionSizeType.BIG;
                } else if (nextLine[1].equals("MIDDLE")) {
                    transactionSizeType = TransactionSizeType.MIDDLE;
                } else {
                    transactionSizeType = TransactionSizeType.SMALL;
                }

                int numberOfUnverifiedTransactions = Integer.parseInt(nextLine[4]);
                long mediumDistributionTime = Long.parseLong(nextLine[5]);
                long mediumVerificationTime = Integer.parseInt(nextLine[6]);
                IntensityInfo intensityInfoInfo = new IntensityInfo(intensity,
                        intensityDispersionType, numberOfNodesType,
                        transactionSizeType, numberOfUnverifiedTransactions,
                        mediumDistributionTime, mediumVerificationTime);

                intensities.add(intensityInfoInfo);
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                //ignore all unparseable lines
            }
        }
        return intensities;
    }
}

