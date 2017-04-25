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
package uk.dsxt.bb.current.scenario.model;

import lombok.Data;

@Data
public class ScenarioInfo {

    private int numberOfNodes;
    private int maxThroughput;
   //todo private int throughput95;
    private int averageThroughput;
    private int averageIntensity;
    private int averageTransactionSize;
    private int averageBlockSize;
    private long averageLatency;

    public ScenarioInfo(int numberOfNodes, int maxThroughput,
                        int averageThroughput, int averageIntensity,
                        int averageTransactionSize, int averageBlockSize, long averageLatency) {
        this.numberOfNodes = numberOfNodes;
        this.maxThroughput = maxThroughput;
        this.averageThroughput = averageThroughput;
        this.averageIntensity = averageIntensity;
        this.averageTransactionSize = averageTransactionSize;
        this.averageBlockSize = averageBlockSize;
        this.averageLatency = averageLatency;
    }
}
