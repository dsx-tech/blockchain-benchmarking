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

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.nio.charset.Charset;

@Log4j2
public class RScriptRunner {

    private static final String RSCRIPTS_DIR = "post-processing/src/main/resources/Rscripts/";

    private static final String GENERAL_SCRIPT = "generalGraphs.R";
    private static final String SCENARIO_SCRIPT = "scenarioGraphs.R";
    private static final String RESOURCES_SCRIPT = "resourcesGraphs.R";

    private static final String CSV_DIR = "/csv";

    public static void run(String pathToRDir) {
        pathToRDir = encloseAllSpaces(pathToRDir);
        File dirWithCSV = new File(DirOrganizer.GENERAL_RESULTS_PATH);
        String path;
        try {
            path = dirWithCSV.getCanonicalPath();
            runRScript(pathToRDir, path, ScriptType.GENERAL);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        dirWithCSV = new File(DirOrganizer.GENERAL_RESOURCES_PATH);
        try {
            path = dirWithCSV.getCanonicalPath();
            runRScript(pathToRDir, path, ScriptType.RESOURCES);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        dirWithCSV = new File(DirOrganizer.ETHEREUM_RESULTS_PATH);
        runForAllScenariosInDir(pathToRDir, dirWithCSV);

        dirWithCSV = new File(DirOrganizer.FABRIC_RESULTS_PATH);
        runForAllScenariosInDir(pathToRDir, dirWithCSV);
    }

    private static String encloseAllSpaces(String path) {
        return path.replace(" ", "\" \"");
    }

    private static void runForAllScenariosInDir(String pathToRDir, File dirWithCSV) {
        for (File file : dirWithCSV.listFiles()) {
            try {
                String path = file.getCanonicalPath() + CSV_DIR;
                runRScript(pathToRDir, path, ScriptType.SCENARIO);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private enum ScriptType {
        GENERAL,
        RESOURCES,
        SCENARIO
    }

    private static void runRScript(String pathToR, String path, ScriptType type) {
        String script = null;
        switch (type) {
            case GENERAL:
                script = GENERAL_SCRIPT;
                break;
            case RESOURCES:
                script = RESOURCES_SCRIPT;
                break;
            case SCENARIO:
                script = SCENARIO_SCRIPT;
                break;
        }
        File scriptFile = new File(RSCRIPTS_DIR + script);
        try {
            script = scriptFile.getCanonicalPath();
        } catch (IOException e) {
            log.error(e.getMessage());
            return;
        }
        try {
            String command = "Rscript " + script + " " + path;
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            log.error(readFromBuff(p.getErrorStream()));
            log.debug(readFromBuff(p.getInputStream()));
        } catch (IOException | InterruptedException e) {
            log.error("Couldn't execute script " + script + "\n" + e.getMessage());
        }
    }

    private static String readFromBuff(InputStream steam) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(steam, Charset.defaultCharset()));

        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }
}
