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
    private int throughputMax;
   //todo private int throughput95;
    private int mediumThroughput;
    private int mediumIntensity;
    private int mediumTransactionSize;
    private int mediumBlockSize;
    private long mediumLatency;

    public ScenarioInfo(int numberOfNodes, int throughputMax,
                        int mediumThroughput, int mediumIntensity,
                        int mediumTransactionSize, int mediumBlockSize, long mediumLatency) {
        this.numberOfNodes = numberOfNodes;
        this.throughputMax = throughputMax;
        this.mediumThroughput = mediumThroughput;
        this.mediumIntensity = mediumIntensity;
        this.mediumTransactionSize = mediumTransactionSize;
        this.mediumBlockSize = mediumBlockSize;
        this.mediumLatency = mediumLatency;
    }
}
