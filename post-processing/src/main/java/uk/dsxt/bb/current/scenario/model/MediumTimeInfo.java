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
public class MediumTimeInfo {

    private long mediumDstrbTime95;
    private long mediumDstrbTime100;
    private long mediumVerificationTime;
    private int numberOfBlocks;

    public MediumTimeInfo() {
        this.mediumDstrbTime95 = 0;
        this.mediumDstrbTime100 = 0;
        this.mediumVerificationTime = -1;
        this.numberOfBlocks = 0;
    }
}
