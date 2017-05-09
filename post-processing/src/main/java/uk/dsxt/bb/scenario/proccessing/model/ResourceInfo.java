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
public class ResourceInfo {

    private long time;
    private String nodeId;
    private double cpuPercent;
    private double memPercent;
    private int memByte;
    private int downloaded;
    private int uploaded;

    public ResourceInfo(long time, String nodeId, double cpuPercent,
                        double memPercent, int memByte, int downloaded, int uploaded) {
        this.time = time;
        this.nodeId = nodeId;
        this.cpuPercent = cpuPercent;
        this.memPercent = memPercent;
        this.memByte = memByte;
        this.downloaded = downloaded;
        this.uploaded = uploaded;
    }
}
