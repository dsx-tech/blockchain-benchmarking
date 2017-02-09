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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author phd
 */
public class LoadGeneratorInstancesManager extends RemoteInstancesManager<LoadGeneratorInstance> {
    private final static Logger logger = LogManager.getLogger(LoadGeneratorInstancesManager.class);

    @Override
    protected List<String> resolveCommands(LoadGeneratorInstance remoteInstance, List<String> commands) {
        return super.resolveCommands(remoteInstance, commands)
                .stream()
                .map(command -> command.replace("${LOAD_TARGET}", remoteInstance.getLoadTargetHost()))
                .peek(command -> logger.debug("command mapped to " + command))
                .collect(Collectors.toList());
    }
}
