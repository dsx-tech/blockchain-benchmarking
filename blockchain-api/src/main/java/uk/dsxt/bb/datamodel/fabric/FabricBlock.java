/*
 ******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2017 DSX Technologies Limited.                               *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************
 */

package uk.dsxt.bb.datamodel.fabric;

import lombok.AllArgsConstructor;
import lombok.ToString;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;

import java.util.List;

@ToString
@AllArgsConstructor
public class FabricBlock implements BlockchainBlock {
    private List<FabricTransaction> transactions;
    private String stateHash;
    private String previousBlockHash;

    @Override
    public String getHash() {
        return stateHash;
    }

    @Override
    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    @Override
    public FabricTransaction[] getTransactions() {
        return transactions == null ? new FabricTransaction[0] : transactions.toArray(new FabricTransaction[transactions.size()]);
    }


}
