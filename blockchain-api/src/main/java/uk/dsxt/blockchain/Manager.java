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

package uk.dsxt.blockchain;

import uk.dsxt.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.datamodel.blockchain.BlockchainPeer;

import java.io.IOException;
import java.util.List;

public interface Manager {

    void start();

    void stop();

    String sendMessage(byte[] body);

    List<Message> getNewMessages();

    BlockchainBlock getBlock(long id) throws IOException;

    BlockchainPeer[] getPeers() throws IOException;

    BlockchainChainInfo getChain() throws IOException;
}
