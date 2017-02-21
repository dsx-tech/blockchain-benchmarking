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

import lombok.Data;

@Data
public class TimeInfo {

    private TimeAndSize timeAndSize;
    private long mediumDstrbTime95;
    private long mediumDstrbTime100;
    private int numberOfBlocks;

    public TimeInfo(TimeAndSize timeAndSize) {
        this.timeAndSize = timeAndSize;
        mediumDstrbTime95 = 0;
        mediumDstrbTime100 = 0;
        numberOfBlocks = 0;
    }

    /**
     * Sets the start of a time span paired to a size span
     * time spans are currently of fixed size: Analyzer.TIME_INTERVAL
     */
    @Data
    public static class TimeAndSize {
        private long time;
        private SizeSpan sizeSpan;

        public TimeAndSize(long time, SizeSpan sizeSpan) {
            this.time = time;
            this.sizeSpan = sizeSpan;
        }
    }

    @Data
    public static class SizeSpan {
        private int blockSizeMin;
        private int blockSizeMax;

        public SizeSpan(int blockSizeMin, int blockSizeMax) {
            this.blockSizeMin = blockSizeMin;
            this.blockSizeMax = blockSizeMax;
        }
    }
}
