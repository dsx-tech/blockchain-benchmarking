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
    private double averageTransactionSize;

    private double maxLatency;
    private double per95Latency;
    private double averageLatency;

    private double averageQueueInc;

    private double averageCPU;
    private double averageMem;
    private double averageMemPercent;
    private double averageIn;
    private double averageOut;

    private BlockchainType blockchainType;

    public ScenarioInfo(BlockchainInfo blockchainInfo, PropertiesFileInfo propertiesFileInfo) {
        calculateInfo(blockchainInfo, propertiesFileInfo);
        calculateResources(blockchainInfo);
    }

    private void calculateInfo(BlockchainInfo blockchainInfo, PropertiesFileInfo properties) {
        blockchainType = properties.getBlockchainType();
//        intensity = properties.getNumOfThreads() * properties.getNumOfNodes() *
//                (1000.0 / properties.getTransactionDelay());

        List<Double> trSizes = new ArrayList<>();
        List<Double> throughputs = new ArrayList<>();
        List<Double> latencies = new ArrayList<>();
        List<Double> queues = new ArrayList<>();
        List<Double> intensities = new ArrayList<>();

        int prevQueue;
        int queue = 0;
        for (TimeSegmentInfo timeSegmentInfo : blockchainInfo.getTimeSegments().values()) {
            throughputs.add(timeSegmentInfo.getThroughput());
            latencies.add(timeSegmentInfo.getBlockLatency());
            intensities.add(timeSegmentInfo.getIntensity());
            if(timeSegmentInfo.getTransactionSize() != 0) {
                trSizes.add(timeSegmentInfo.getTransactionSize());
            }
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
        averageTransactionSize = calculateAverage(trSizes);
        intensity = calculateAverage(intensities);

        per95Throughput = calculate95Percentile(throughputs);
        per95Latency = calculate95Percentile(latencies);
    }

    private void calculateResources(BlockchainInfo blockchainInfo) {
        List<Double> cpus = new ArrayList<>();
        List<Double> mem = new ArrayList<>();
        List<Double> memPr = new ArrayList<>();
        List<Double> in = new ArrayList<>();
        List<Double> out = new ArrayList<>();

        for (ResourceInfo resource : blockchainInfo.getResources()) {
            cpus.add(resource.getCpuPercent());
            mem.add((double) resource.getMem());
            memPr.add(resource.getMemPercent());
            in.add((double) resource.getDownloaded());
            out.add((double) resource.getUploaded());
        }

        averageCPU = calculateAverage(cpus);
        averageMem = calculateAverage(mem);
        averageMemPercent = calculateAverage(memPr);
        averageIn = calculateAverage(in);
        averageOut = calculateAverage(out);
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
