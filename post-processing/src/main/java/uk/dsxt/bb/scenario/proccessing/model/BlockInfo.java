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
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Log4j2
public class BlockInfo {

    private long blockId;
    private List<TransactionInfo> transactions;
    private long parentBlockId;
    private List<DistributionTime> distributionTimes;
    //private double latency95;
    private double latency;
    private double size;
    private double creationTime;
    // percentage of nodes on which transaction should be distributed to calculate latency
    private static final double percentageOfNodes = 0.9;


    public BlockInfo(long blockId, List<DistributionTime> distributionTimes) {
        this.blockId = blockId;
        this.distributionTimes = distributionTimes;
        this.transactions = new ArrayList<>();
    }

    public void calculateCreationTime() {
        List<Double> times = distributionTimes.stream()
                .map(DistributionTime::getTime).collect(Collectors.toList());
        if (times.isEmpty()) {
            log.error("No distribution times found in block " + blockId);
            creationTime = -1;
            return;
        }
        times.sort(Double::compareTo);
        creationTime = times.get(0);
    }

    public void calculateLatency() {
        List<Double> times = new ArrayList<>();
        for (DistributionTime distributionTime : distributionTimes) {
            times.add(distributionTime.getTime());
        }
        if(times.isEmpty()) {
            log.error("No distribution times found in block " + blockId);
            latency = -1;
            return;
        }
        times.sort(Double::compareTo);
        latency = times.get((int)(times.size()* percentageOfNodes - 1)) - creationTime;
    }

    public void addTransaction(TransactionInfo transaction) {
        transactions.add(transaction);
    }

    public void addDistributionTime(DistributionTime time) {
        distributionTimes.add(time);
    }

    @Data
    public static class DistributionTime {
        private String nodeId;
        private double time;

        public DistributionTime(String nodeId, double time) {
            this.nodeId = nodeId;
            this.time = time;
        }
    }
}