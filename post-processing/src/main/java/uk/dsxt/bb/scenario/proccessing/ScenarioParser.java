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
package uk.dsxt.bb.scenario.proccessing;

import uk.dsxt.bb.properties.proccessing.model.PropertiesFileInfo;
import uk.dsxt.bb.scenario.proccessing.model.BlockchainInfo;
import uk.dsxt.bb.scenario.proccessing.model.ScenarioInfo;

public class ScenarioParser {

    public static ScenarioInfo parseScenario(PropertiesFileInfo props) {
        CSVParser parser = new CSVParser(props.getPathToScenarioDir());
        BlockchainInfo blockchainInfo = parser.parseCSVs(props.getBlockchainType());
        if (blockchainInfo == null) {
            return null;
        }
        ResultsAnalyzer resultsAnalyzer = new ResultsAnalyzer(blockchainInfo, props);
        blockchainInfo = resultsAnalyzer.analyze();
        if(blockchainInfo == null) {
            return null;
        }
        CSVComposer composer = new CSVComposer(blockchainInfo);
        composer.composeCSVs(props.getPathToScenarioDir());
        return blockchainInfo.getScenarioInfo();
    }
}
