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
package uk.dsxt.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BlockInfo {

    private long blockId;
    private List<TransactionInfo> transactionIds;
    private long parentBlockId;
    private List<NodeTime> nodeTimes;
    private long maxNodeTime95;
    private long maxNodeTime;
    private long verificationTime;

    public BlockInfo(long blockId, List<NodeTime> nodeTimes) {
        this.blockId = blockId;
        this.nodeTimes = nodeTimes;
        this.transactionIds = new ArrayList<>();
    }

    public void calculateMaxTime() {
        List<Long> times = new ArrayList<>();
        for (NodeTime nodeTime : nodeTimes) {
            times.add(nodeTime.getTime());
        }
        times.sort(Long::compareTo);
        maxNodeTime = times.get(times.size() - 1);
        maxNodeTime95 = times.get((int) (times.size() * 0.95 - 1));
    }

    public void addTransaction(TransactionInfo transaction) {
        transactionIds.add(transaction);
    }

    @Data
    public static class NodeTime {
        private int nodeId;
        private long time;

        public NodeTime(int nodeId, long time) {
            this.nodeId = nodeId;
            this.time = time;
        }
    }
}