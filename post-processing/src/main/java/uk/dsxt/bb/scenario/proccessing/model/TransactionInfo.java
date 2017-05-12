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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TransactionInfo {

    private long time;
    private String transactionId;
    private double transactionSize;
    private String nodeId;
    //private int responseCode;
    private long blockId;
    private double latency;
    private Map<Integer, Double> latencyQuartils;

    public TransactionInfo(long time, String transactionId, int transactionSize,
                           String nodeId) {
        this.time = time;
        this.transactionId = transactionId;
        this.transactionSize = transactionSize;
        this.nodeId = nodeId;
        latencyQuartils = new HashMap<>();

        // this.responseCode = responseCode;
    }

    public void calculateLatency(BlockchainInfo blockchainInfo) {
        BlockInfo block = blockchainInfo.getBlocks().get(blockId);
        if (block == null || block.getLatency() == -1) {
            latency = Double.POSITIVE_INFINITY;
            return;
        }
        for (int i = 2; i <= block.getLatencyQuartils().size()+1; i++) {
            latencyQuartils.put(i,
                    block.getCreationTime() + block.getLatencyQuartils().get(i) - time);
        }
        latency = block.getCreationTime() + block.getLatency() - time;
    }
}
