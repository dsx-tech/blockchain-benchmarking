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

import uk.dsxt.bb.properties.proccessing.model.PropertiesFileInfo;
import uk.dsxt.bb.properties.proccessing.model.ResultType;
import uk.dsxt.bb.scenario.proccessing.model.ScenarioInfo;

import uk.dsxt.bb.scenario.proccessing.ScenarioParser;
import uk.dsxt.bb.properties.proccessing.PropertiesComparator;

import javax.script.ScriptException;

import java.io.FileNotFoundException;
import java.util.List;

public class ResultsAnalyzerMain {

    public static void main(String[] args) throws ScriptException,
            InterruptedException, FileNotFoundException {

        //create all dirs if they don't exist
        if (!DirOrganizer.createDirStructure()) {
            return;
        }
        if (!GeneralCSVComposer.createGeneralResultFiles()) {
            return;
        }
        process(ResultType.INTENSITY);
        process(ResultType.SIZE);
        process(ResultType.SCALABILITY);
    }

    private static void process(ResultType type) {
        PropertiesComparator comparator = new PropertiesComparator();
        List<PropertiesFileInfo> properties = comparator.compare(type);
        if (properties != null) {
            for (PropertiesFileInfo property : properties) {
                ScenarioInfo scenarioInfo = ScenarioParser.parseScenario(property);
                if (scenarioInfo != null) {
                    GeneralCSVComposer.addToIntensity(scenarioInfo, type);
                }
            }
        }
    }
}

