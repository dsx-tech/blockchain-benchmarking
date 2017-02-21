/*
 ******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2016 DSX Technologies Limited.                               *
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

package uk.dsxt.bb.datamodel.bitcoin;

import lombok.Value;
import uk.dsxt.bb.datamodel.blockchain.BlockchainPeer;

import java.math.BigDecimal;

@Value
public class BitcoinPeer implements BlockchainPeer {
    long id;
    String addr;
    String services;
    boolean relaytxes;
    long lastend;
    long lastrecv;
    long bytesent;
    long conntime;
    long timeoffset;
    BigDecimal pingtime;
    BigDecimal minping;
    long version;
    String subver;
    boolean inbound;
    long startingheight;
    long banscore;
    long synced_headers;
    long synced_blocks;
    boolean whitelisted;
    BitcoinByteSentPerMsg bytesent_per_msg;
    BitcoinBytesRecvPerMsg bytesrecv_per_msg;

    @Override
    public String getId() {
        return Long.toString(id);
    }

    @Override
    public String getAddress() {
        return addr;
    }
}
