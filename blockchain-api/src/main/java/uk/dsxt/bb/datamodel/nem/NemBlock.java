package uk.dsxt.bb.datamodel.nem;

import lombok.Value;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;

/*
 * *****************************************************************************
 *  * Blockchain benchmarking framework                                          *
 *  * Copyright (C) 2016 DSX Technologies Limited.                               *
 *  * *
 *  * This program is free software: you can redistribute it and/or modify       *
 *  * it under the terms of the GNU General Public License as published by       *
 *  * the Free Software Foundation, either version 3 of the License, or          *
 *  * (at your option) any later version.                                        *
 *  * *
 *  * This program is distributed in the hope that it will be useful,            *
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 *  * See the GNU General Public License for more details.                       *
 *  * *
 *  * You should have received a copy of the GNU General Public License          *
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *  * *
 *  * Removal or modification of this copyright notice is prohibited.            *
 *  * *
 *  *****************************************************************************
 */
@Value
public class NemBlock implements BlockchainBlock {
    long timeStamp;
    String signature;
    NemPrevBlockHash prevBlockHash;
    int type;
    NemTransaction[] transactions;
    long version;
    String signer;
    long height;

    @Override
    public String getHash() {
        return signature;
    }

    @Override
    public String getPreviousBlockHash() {
        return prevBlockHash.getData();
    }

    @Override
    public long getTime() {
        return timeStamp;
    }

    @Override
    public NemTransaction[] getTransactions() {
        return transactions;
    }
}
