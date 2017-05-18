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

import java.io.File;

public class DirOrganizer {

    public static final String MAIN_LOG_PATH = "post-processing/src/main/resources/logs/";

    public static final String INTENSITY_LOGS_PATH =
            MAIN_LOG_PATH + "intensity/";
    public static final String SIZE_LOGS_PATH =
            MAIN_LOG_PATH + "size/";
    public static final String SCALABILITY_LOGS_PATH =
            MAIN_LOG_PATH + "scalability/";
    public static final String OTHERS_LOGS_PATH =
            MAIN_LOG_PATH + "others/";

    public static final String ETHEREUM_RESULTS_PATH =
            "post-processing/src/main/resources/results/ethereum/";
    public static final String FABRIC_RESULTS_PATH =
            "post-processing/src/main/resources/results/fabric/";
    public static final String GENERAL_RESULTS_PATH =
            "post-processing/src/main/resources/results/";
//    public static final String GENERAL_RESOURCES_PATH =
//            "post-processing/src/main/resources/results/general/resources/csv/";


    public static boolean createDirStructure() {
        return createDir(ETHEREUM_RESULTS_PATH)
                && createDir(FABRIC_RESULTS_PATH)
                && createDir(GENERAL_RESULTS_PATH);
               // && createDir(GENERAL_RESOURCES_PATH);
    }

    private static boolean createDir(String path) {
        File file = new File(path);
        if (!file.exists() || file.isFile()) {
            return file.mkdirs();
        }
        return true;
    }
}
