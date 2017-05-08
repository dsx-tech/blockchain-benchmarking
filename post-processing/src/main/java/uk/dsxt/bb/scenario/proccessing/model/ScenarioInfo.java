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
import uk.dsxt.bb.properties.proccessing.model.BlockchainType;
import uk.dsxt.bb.properties.proccessing.model.PropertiesFileInfo;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScenarioInfo {

    private int numberOfNodes;
    private double maxThroughput;
    private double per95Throughput;
    private double averageThroughput;
    private double intensity;
    private int minTransactionSize;
    private int maxTransactionSize;
    private double maxLatency;
    private double per95Latency;
    private double averageLatency;
    private double averageQueueInc;
    private BlockchainType blockchainType;


    public ScenarioInfo(BlockchainInfo blockchainInfo, PropertiesFileInfo propertiesFileInfo) {
        calculateInfo(blockchainInfo, propertiesFileInfo);
    }

    private void calculateInfo(BlockchainInfo blockchainInfo, PropertiesFileInfo properties) {
        blockchainType = properties.getBlockchainType();
        intensity = properties.getNumOfThreads() * properties.getNumOfNodes() *
                (1000.0 / properties.getTransactionDelay());
        minTransactionSize = properties.getMinMessageLength();
        maxTransactionSize = properties.getMaxMessageLength();

        List<Double> throughputs = new ArrayList<>();
        List<Double> latencies = new ArrayList<>();
        List<Double> queues = new ArrayList<>();

        int prevQueue;
        int queue = 0;
        for (TimeSegmentInfo timeSegmentInfo : blockchainInfo.getTimeSegments().values()) {
            throughputs.add(timeSegmentInfo.getThroughput());
            latencies.add(timeSegmentInfo.getLatency());
            prevQueue = queue;
            queue = timeSegmentInfo.getTransactionQueueLength();
            queues.add((double) (queue - prevQueue));
        }
        throughputs.sort(Double::compareTo);
        latencies.sort(Double::compareTo);
        maxThroughput = throughputs.get(throughputs.size() - 1);
        maxLatency = latencies.get(latencies.size() - 1);

        averageThroughput = calculateAverage(throughputs);
        averageLatency = calculateAverage(latencies);
        averageQueueInc = calculateAverage(queues);

        per95Throughput = calculate95Percentile(throughputs);
        per95Latency = calculate95Percentile(latencies);
    }

    private double calculateAverage(List<Double> list) {
        double sum = 0f;
        if (!list.isEmpty()) {
            for (double mark : list) {
                sum += mark;
            }
            return sum / list.size();
        }
        return sum;
    }

    private double calculate95Percentile(List<Double> list) {
        double sum = 0f;
        if (!list.isEmpty()) {
            for (double elem : list) {
                sum += elem;
            }
            sum = sum * 0.9;
            double checkSum = 0f;
            for (double elem : list) {
                checkSum += elem;
                if (checkSum >= sum) {
                    return elem;
                }
            }
        }
        return sum;
    }

}
