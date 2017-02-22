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

package uk.dsxt.bb.remote.instance;

/**
 * @author phd
 */
public class LoggerInstancesManager extends RemoteInstancesManager<LoggerInstance> {
    public LoggerInstancesManager(String masterIp, int masterPort) {
        super(masterIp, masterPort);
    }

    @Override
    protected String getEnvVariables(LoggerInstance remoteInstance) {
        String params = String.format("%s %s %s %d %s %s",
                remoteInstance.getBlockchainType(),
                "grpc://" + remoteInstance.getTarget() + ":7051",
                remoteInstance.getLogFile(),
                remoteInstance.getRequestPeriod(),
                remoteInstance.getHost(),
                getMasterIp() + ":" + getMasterPort()
                );
        return String.format("export LOG_PARAMS=\"%s\"; ", params);
    }
}
