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
import uk.dsxt.bb.properties.proccessing.model.PropertiesFileInfo;
import uk.dsxt.bb.properties.proccessing.model.ResultType;

import static uk.dsxt.bb.general.DirOrganizer.*;

import java.io.File;
import java.util.*;

@Log4j2
public class PropertiesComparator {

    private static final String propFileName = "test-manager.properties";

    public List<PropertiesFileInfo> compare(ResultType type) {
        String path = null;
        switch (type) {
            case INTENSITY:
                path = INTENSITY_LOGS_PATH;
                break;
            case SIZE:
                path = SIZE_LOGS_PATH;
                break;
            case SCALABILITY:
                path = SCALABILITY_LOGS_PATH;
                break;
            case OTHERS:
                path = OTHERS_LOGS_PATH;
                break;
        }
        List<PropertiesFileInfo> props = getAllProps(path);
        if (props == null || props.size() == 0) {
            return null;
        }
        if(type == ResultType.OTHERS) {
            return props;
        }
        PropertiesFileInfo modelProp = props.get(0);
        for (PropertiesFileInfo property : props) {
            boolean equals = false;
            switch (type) {
                case INTENSITY:
                    equals = modelProp.equalsExceptIntensity(property);
                    break;
                case SIZE:
                    equals = modelProp.equalsExceptSize(property);
                    break;
                case SCALABILITY:
                    equals = modelProp.equalsFully(property);
                    break;
            }
            if (!equals) {
                return null;
            }
        }
        return props;
    }

    private List<PropertiesFileInfo> getAllProps(String dir) {
        List<PropertiesFileInfo> files = new ArrayList<>();
        File logsDir = new File(dir);
        List<File> propFiles = getAllPropFiles(logsDir);
        if (propFiles == null) {
            return null;
        }
        for (File file : propFiles) {
            PropertiesFileInfo props = PropertiesParser.parse(file);
            if (props != null) {
                files.add(props);
            }
        }
        return files;
    }

    /**
     * gets all prop files from all scenario cases
     **/
    private List<File> getAllPropFiles(File dir) {
        if (!dir.isDirectory() || dir.listFiles() == null) {
            log.error("No scenario results found in dir " + dir.getName());
            return null;
        }
        List<File> propFiles = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory() && file.listFiles() != null) {
                File propFile = findPropfile(file);
                if (propFile != null) {
                    propFiles.add(propFile);
                }
            }
        }
        return propFiles;
    }

    /**
     * finds prop file in scenario dir
     **/
    public File findPropfile(File dir) {
        if (!dir.isDirectory() || dir.listFiles() == null) {
            log.error("No scenario logs found in " + dir.getName());
            return null;
        }
        for (File file : dir.listFiles()) {
            if (file.getName().equals(propFileName) && file.isFile()) {
                return file;
            }
        }
        log.error("No property file found in " + dir.getName());
        return null;
    }
}
