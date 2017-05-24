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

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

/**
 * @author phd
 */
@Getter
public class LoggerInstance extends BlockchainInteractingInstance {
    private String logFile;
    private int requestPeriod;

    @Setter
    private boolean isRunning;

    public LoggerInstance(String userName, String host, int port, String keyPath, Path logPath, String blockchainType, String target, String blockchainPort, String logFile, int requestPeriod) {
        super(userName, host, port, keyPath, logPath, blockchainType, target, blockchainPort);
        this.logFile = logFile;
        this.requestPeriod = requestPeriod;
    }
}
