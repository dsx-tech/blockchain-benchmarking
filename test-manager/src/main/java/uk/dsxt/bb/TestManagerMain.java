/*
 ******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2016 DSX Technologies Limited.                               *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************
 */

package uk.dsxt.bb;

import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.test_manager.TestManager;
import uk.dsxt.bb.test_manager.TestManagerProperties;
import uk.dsxt.bb.utils.PropertiesHelper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * @author phd
 */
@Log4j2
public class TestManagerMain {
    public static void main(String[] args) throws Exception {
        String propertiesPath = args[0];
        Properties properties = PropertiesHelper.loadPropertiesFromPath(propertiesPath);

        TestManagerProperties testManagerProperties = TestManagerProperties.fromProperties(properties);

        List<String> allHosts = Files.readAllLines(Paths.get(testManagerProperties.getInstancesPath()));
        TestManager testManager = new TestManager(allHosts, testManagerProperties);
        testManager.start();
    }
}
