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
public class TimeSegmentInfo {

    private long time = 0;
    // number of transactions in all blocks created at this time period
    private int throughput = 0;
    private long latency = 0;
    private int intensity = 0;
    private int transactionSize = 0;
    private int numberOfTransactions = 0;
    private int blockSize = 0;
    private int numberOfBlocks = 0;
    private int numberTransactionsInBlock = 0;
    private int distributionThroughput = 0;

    public TimeSegmentInfo(long time) {
        this.time = time;
    }
}
