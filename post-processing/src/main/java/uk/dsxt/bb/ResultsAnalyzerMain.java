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

import uk.dsxt.bb.model.BlockchainInfo;
import uk.dsxt.bb.processing.ResultsAnalyzer;
import uk.dsxt.bb.processing.CSVComposer;
import uk.dsxt.bb.processing.CSVParser;

import java.io.File;

public class ResultsAnalyzerMain {

    public static void main(String[] args) {
        BlockchainInfo blockchainInfo = CSVParser.parseCSVs();
        if (blockchainInfo == null) {
            return;
        }
        ResultsAnalyzer resultsAnalyzer = new ResultsAnalyzer(blockchainInfo);
        blockchainInfo = resultsAnalyzer.analyze();
        File file = new File(CSVComposer.RESULT_PATH);
        if (!file.exists() || file.isFile()) {
            file.mkdir();
        }
        CSVComposer composer = new CSVComposer(blockchainInfo);
        composer.composeCSVs();
//        try {
//            Runtime.getRuntime().exec("Rscript \\post-processing\\src\\main\\resources\\graphsDrawer.R \\post-processing\\src\\main\\resources\\results"");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
