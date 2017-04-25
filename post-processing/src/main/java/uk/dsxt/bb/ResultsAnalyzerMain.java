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


import org.renjin.script.RenjinScriptContext;
import uk.dsxt.bb.current.scenario.model.BlockchainInfo;
import uk.dsxt.bb.current.scenario.processing.CSVComposer;

import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.DoubleVector;
import uk.dsxt.bb.current.scenario.processing.CSVParser;
import uk.dsxt.bb.current.scenario.processing.ResultsAnalyzer;

import javax.script.ScriptException;

import java.io.File;
import java.io.FileNotFoundException;

public class ResultsAnalyzerMain {

    public static void main(String[] args) throws ScriptException, InterruptedException, FileNotFoundException {
        //create all dirs if they don't exist
        createAllDirs();
        BlockchainInfo blockchainInfo = CSVParser.parseCSVs();
        if (blockchainInfo == null) {
            return;
        }
        ResultsAnalyzer resultsAnalyzer = new ResultsAnalyzer(blockchainInfo);
        blockchainInfo = resultsAnalyzer.analyze();
        CSVComposer composer = new CSVComposer(blockchainInfo);
        composer.composeCSVs();
    }

    public static final String CURRENT_GRAPHS_PATH = "post-processing/src/main/resources/results/graphs";

    private static void createAllDirs() {
        File file = new File(CSVComposer.RESULT_PATH);
        if (!file.exists() || file.isFile()) {
            file.mkdirs();
        }
        file = new File(CURRENT_GRAPHS_PATH);
        if (!file.exists() || file.isFile()) {
            file.mkdirs();
        }
    }
}
