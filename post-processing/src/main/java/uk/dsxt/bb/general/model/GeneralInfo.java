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

package uk.dsxt.bb.general.model;

import lombok.Data;
import uk.dsxt.bb.general.model.enums.*;

import java.util.List;

@Data
public class GeneralInfo {

    private List<IntensityInfo> intensities;
    private List<SizeInfo> sizes;
    private List<NumberOfNodesInfo> numberOfNodesInfos;

    public SizeInfo getSizeInfo(int size, SizeDispersionType sizeDispersionType,
                                NumberOfNodesType numberOfNodesType, IntensityType intensityType) {
        for (SizeInfo sizeInfo : sizes) {
            if (sizeInfo.getSizeOfTransaction() == size && sizeInfo.getIntensityType() == intensityType
                    && sizeInfo.getNumberOfNodesType() == numberOfNodesType
                    && sizeInfo.getSizeDispersionType() == sizeDispersionType) {
                return sizeInfo;
            }
        }
        return null;
    }

    public void addSizeInfo(SizeInfo sizeInfo) {
        sizes.add(sizeInfo);
    }

    public IntensityInfo getIntensityInfo(int intensity,
                                          TransactionSizeType size,
                                          NumberOfNodesType numberOfNodesType,
                                          IntensityDispersionType dispersionType) {
        for (IntensityInfo intensityInfo : intensities) {
            if (intensityInfo.getIntensity() == intensity
                    && intensityInfo.getTransactionSizeType() == size
                    && intensityInfo.getNumberOfNodesType() == numberOfNodesType
                    && intensityInfo.getIntensityDispersionType() == dispersionType) {
                return intensityInfo;
            }
        }
        return null;
    }


    public void addIntensity(IntensityInfo intensityInfo) {
        intensities.add(intensityInfo);
    }
}
