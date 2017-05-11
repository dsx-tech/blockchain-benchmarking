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
import uk.dsxt.bb.properties.proccessing.model.PropertiesFileInfo;
import uk.dsxt.bb.properties.proccessing.model.ResultType;
import uk.dsxt.bb.scenario.proccessing.model.ScenarioInfo;

import uk.dsxt.bb.scenario.proccessing.ScenarioParser;
import uk.dsxt.bb.properties.proccessing.PropertiesComparator;

import javax.script.ScriptException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Log4j2
public class App {

    public static void main(String[] args) {
        //create all dirs if they don't exist
        if (!DirOrganizer.createDirStructure()) {
            return;
        }
        if (!GeneralCSVComposer.createResultFiles()) {
            return;
        }
        process(ResultType.INTENSITY);
        process(ResultType.SIZE);
        process(ResultType.SCALABILITY);
        process(ResultType.OTHERS);

        if(args.length == 0) {
            log.error("Missing path to R directory");
        }
        RScriptRunner.run(args[0]);
    }

    private static void process(ResultType type) {
        PropertiesComparator comparator = new PropertiesComparator();
        List<PropertiesFileInfo> properties = comparator.compare(type);
        if (properties != null) {
            for (PropertiesFileInfo property : properties) {
                ScenarioInfo scenarioInfo = ScenarioParser.parseScenario(property);
                if (scenarioInfo != null && type != ResultType.OTHERS) {
                    GeneralCSVComposer.addScenarioInfo(scenarioInfo, type);
                }
            }
        }
    }
}

