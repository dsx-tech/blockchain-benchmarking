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
package uk.dsxt.bb.properties.proccessing;

import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.properties.proccessing.model.BlockchainType;
import uk.dsxt.bb.properties.proccessing.model.PropertiesFileInfo;

import java.io.*;
import java.util.Properties;

@Log4j2
public class PropertiesParser {
    private static final String threadsProp = "amount.threads.per.target";
    private static final String minMesLengthProp = "message.length.min";
    private static final String maxMesLengthProp = "message.length.max";
    private static final String blockchainTypeProp = "blockchain.type";
    private static final String numNodesProp = "blockchain.instances.amount";
    private static final String mesDelayProp = "message.delay";
    private static final String numLoadGenProp = "load_generator.instances.amount";


    public static PropertiesFileInfo parse(File file) {
        Properties prop = new Properties();
        try (FileReader reader = new FileReader(file)) {
            // load a properties file
            prop.load(reader);
            // get the property value and print it out
            int threads = Integer.parseInt(prop.getProperty(threadsProp));
            int minSize = Integer.parseInt(prop.getProperty(minMesLengthProp));
            int maxSize = Integer.parseInt(prop.getProperty(maxMesLengthProp));
            int numNodes = Integer.parseInt(prop.getProperty(numNodesProp));
            int delay = Integer.parseInt(prop.getProperty(mesDelayProp));
            int numLoadGenerators = Integer.parseInt(prop.getProperty(numLoadGenProp));
            BlockchainType type;
            if (prop.getProperty(blockchainTypeProp).equals("ethereum")) {
                type = BlockchainType.ETHEREUM;
            } else if (prop.getProperty(blockchainTypeProp).equals("fabric")) {
                type = BlockchainType.FABRIC;
            } else {
                log.error("Unknown blockchain type " + prop.getProperty(blockchainTypeProp));
                return null;
            }
            return new PropertiesFileInfo(file.getParentFile().getCanonicalPath(),
                    threads, minSize, maxSize, numNodes, delay,
                    type, numLoadGenerators);

        } catch (IOException | RuntimeException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }
}
