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
import uk.dsxt.bb.general.model.enums.IntensityDispersionType;
import uk.dsxt.bb.general.model.enums.NumberOfNodesType;
import uk.dsxt.bb.general.model.enums.TransactionSizeType;

import java.util.List;

@Data
public class GeneralInfo {
    List<IntensityInfo> intensities;

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
