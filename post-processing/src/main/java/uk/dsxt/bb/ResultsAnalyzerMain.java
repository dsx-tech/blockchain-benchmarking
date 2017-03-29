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

package uk.dsxt.bb;

import uk.dsxt.bb.current.scenario.model.BlockchainInfo;
import uk.dsxt.bb.current.scenario.processing.ResultsAnalyzer;
import uk.dsxt.bb.current.scenario.processing.CSVComposer;
import uk.dsxt.bb.current.scenario.processing.CSVParser;
import uk.dsxt.bb.general.model.GeneralInfo;
import uk.dsxt.bb.general.processing.ResultCombiner;

import java.io.File;

public class ResultsAnalyzerMain {

    public static void main(String[] args) {
        //create all dirs if they don't exist
        createAllDirs();
        //process current scenario results1
        BlockchainInfo blockchainInfo = CSVParser.parseCSVs();
        if (blockchainInfo == null) {
            return;
        }
        ResultsAnalyzer resultsAnalyzer = new ResultsAnalyzer(blockchainInfo);
        blockchainInfo = resultsAnalyzer.analyze();
        CSVComposer composer = new CSVComposer(blockchainInfo);
        composer.composeCSVs();
        //combine results1 of current scenario with all previous results1
        GeneralInfo generalInfo = uk.dsxt.bb.general.processing.CSVParser.parseCSVs();
        generalInfo = ResultCombiner.combine(blockchainInfo, generalInfo);
        uk.dsxt.bb.general.processing.CSVComposer generalComposer =
                new uk.dsxt.bb.general.processing.CSVComposer(generalInfo);
        generalComposer.composeCSVs();

//        try {
//            Runtime.getRuntime().exec("Rscript \\post-processing\\src\\main\\resources\\graphsDrawer.R \\post-processing\\src\\main\\resources\\results1"");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    public static final String GENERAL_GRAPHS_PATH = "post-processing/src/main/resources/results/general/graphs";
    public static final String CURRENT_GRAPHS_PATH = "post-processing/src/main/resources/results/current scenario/graphs";

    /**
     * creates a dir system:
     * results1
     * |_general
     * |  |_graphs
     * |  |_csv
     * |_current scenario
     * |  |_graphs
     * |  |_csv
     */
    private static void createAllDirs() {
        File file = new File(CSVComposer.RESULT_PATH);
        if (!file.exists() || file.isFile()) {
            file.mkdirs();
        }
        file = new File(uk.dsxt.bb.general.processing.CSVComposer.RESULT_PATH);
        if (!file.exists() || file.isFile()) {
            file.mkdirs();
        }
        file = new File(GENERAL_GRAPHS_PATH);
        if (!file.exists() || file.isFile()) {
            file.mkdirs();
        }
        file = new File(CURRENT_GRAPHS_PATH);
        if (!file.exists() || file.isFile()) {
            file.mkdirs();
        }
    }
}
