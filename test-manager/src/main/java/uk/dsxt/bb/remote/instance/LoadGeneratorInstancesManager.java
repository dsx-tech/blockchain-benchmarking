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
public class LoadGeneratorInstancesManager extends RemoteInstancesManager<LoadGeneratorInstance> {

    public LoadGeneratorInstancesManager(String masterIp, int masterPort) {
        super(masterIp, masterPort);
    }

    @Override
    protected String getEnvVariables(LoadGeneratorInstance remoteInstance) {
        String params = String.format("%d %d %d %d %d %s %s %s %s %s %s",
                remoteInstance.getAmountOfTransactions(),
                remoteInstance.getAmountOfThreadsPerTarget(),
                remoteInstance.getMinLength(),
                remoteInstance.getMaxLength(),
                remoteInstance.getDelay(),
                remoteInstance.getHost(),
                getMasterIp() + ":" + getMasterPort(),
                "/home/ubuntu/credentials",
                remoteInstance.getBlockchainType(),
                remoteInstance.getBlockchainPort(),
                String.join(" ", remoteInstance.getLoadTargets()));
        return String.format("export LOAD_PARAMS=\"%s\"; ", params);
    }
}
