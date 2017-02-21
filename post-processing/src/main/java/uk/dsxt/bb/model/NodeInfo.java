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

package uk.dsxt.bb.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeInfo {

    private int nodeId;
    private List<TimeSpan> workTimes;
    private List<Long> startTimes;
    private List<Long> stopTimes;

    public NodeInfo(int nodeId) {
        this.nodeId = nodeId;
        this.startTimes = new ArrayList<>();
        this.stopTimes = new ArrayList<>();
        this.workTimes = new ArrayList<>();
    }

    public void addStartTime(Long time) {
        startTimes.add(time);
    }

    public void addStopTime(Long time) {
        stopTimes.add(time);
    }

    public void addWorkTime(TimeSpan time) {
        workTimes.add(time);
    }

    @Data
    public static class TimeSpan {
        private long startTime;
        private long endTime;

        public TimeSpan(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
