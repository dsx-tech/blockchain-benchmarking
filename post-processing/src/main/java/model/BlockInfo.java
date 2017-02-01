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
package model;

import java.util.ArrayList;
import java.util.List;

public class BlockInfo {

    private int blockId;
    private List<Integer> transactionIds;
    private int parentBlockId;
    private List<NodeTime> nodeTimes;
    private long maxNodeTime95;
    private long maxNodeTime;
    private long verificationTime;

//    public BlockInfo(int blockId, List<Integer> transactionIds, int parentBlockId, List<NodeTime> nodeTimes) {
//        this.blockId = blockId;
//        this.transactionIds = transactionIds;
//        this.parentBlockId = parentBlockId;
//        this.nodeTimes = nodeTimes;
//    }

    public BlockInfo(int blockId, List<NodeTime> nodeTimes) {
        this.blockId = blockId;
        this.nodeTimes = nodeTimes;
    }

    public long getMaxNodeTime95() {
        return maxNodeTime95;
    }

    public long getMaxNodeTime() {
        return maxNodeTime;
    }

    public long getVerificationTime() {
        return verificationTime;
    }

    public void setMaxNodeTime95(long maxNodeTime95) {
        this.maxNodeTime95 = maxNodeTime95;
    }

    public void setMaxNodeTime(long maxNodeTime) {
        this.maxNodeTime = maxNodeTime;
    }

    public void setVerificationTime(long verificationTime) {
        this.verificationTime = verificationTime;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public List<Integer> getTransactionIds() {
        return transactionIds;
    }

    public void setTransactionIds(List<Integer> transactionIds) {
        this.transactionIds = transactionIds;
    }

    public int getParentBlockId() {
        return parentBlockId;
    }

    public void setParentBlockId(int parentBlockId) {
        this.parentBlockId = parentBlockId;
    }

    public List<NodeTime> getNodeTimes() {
        return nodeTimes;
    }

    public void setNodeTimes(List<NodeTime> nodeTimes) {
        this.nodeTimes = nodeTimes;
    }

    public void calculateMaxTime() {
        List<Long> times = new ArrayList<>();
        for (NodeTime nodeTime : nodeTimes) {
            times.add(nodeTime.getTime());
        }
        times.sort(Long::compareTo);
        maxNodeTime = times.get(times.size() - 1);
        maxNodeTime95 = times.get((int)(times.size() * 0.95 - 1));
    }

    public static class NodeTime {
        private int nodeId;
        private long time;

        public NodeTime(int nodeId, long time) {
            this.nodeId = nodeId;
            this.time = time;
        }

        public int getNodeId() {
            return nodeId;
        }

        public long getTime() {
            return time;
        }
    }
}