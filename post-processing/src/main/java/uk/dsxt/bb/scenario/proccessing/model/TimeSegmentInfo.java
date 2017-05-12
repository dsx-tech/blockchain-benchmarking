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
package uk.dsxt.bb.scenario.proccessing.model;

import lombok.Data;

@Data
public class TimeSegmentInfo {

    private long time = 0;
    // number of transactions in all blocks created at this time period
    private double throughput = 0;
    private double latency = 0;
    private double intensity = 0;
    private double transactionSize = 0;
    private int numberOfTransactionsIntegrated = 0;
    private int numberOfTransactionsGenerated = 0;
    private double blockSize = 0;
    private int numberOfBlocks = 0;
    private double blockGenerationFrequency = 0f;
    private int numberTransactionsInBlock = 0;
    private int transactionQueueLength = 0;

    public TimeSegmentInfo(long time) {
        this.time = time;
    }
}
