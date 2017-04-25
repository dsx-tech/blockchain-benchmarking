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

package uk.dsxt.bb.eris;

import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.blockchain.Message;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.bb.datamodel.blockchain.BlockchainPeer;
import uk.dsxt.bb.datamodel.eris.ErisChainInfo;
import uk.dsxt.bb.utils.JSONRPCHelper;

import java.io.IOException;
import java.util.List;

public class ErisManager implements Manager {
    private String url;

    private enum ErisMethods {
        GET_BLOCKCHAIN_INFO("erisdb.getBlockchainInfo");

        private final String method;

        ErisMethods(String method) {
            this.method = method;
        }

        public String getMethod() {
            return method;
        }
    }

    ErisManager(String url) {
        this.url = url;
    }

    @Override
    public String sendTransaction(String to, String from, String amount) {
        return null;
    }

    @Override
    public String sendMessage(byte[] body) {
        return null;
    }

    @Override
    public List<Message> getNewMessages() {
        return null;
    }

    @Override
    public BlockchainBlock getBlock(long id) throws IOException {
        return null;
    }

    @Override
    public BlockchainPeer[] getPeers() throws IOException {
        return new BlockchainPeer[0];
    }

    @Override
    public BlockchainChainInfo getChain() throws IOException {
        return JSONRPCHelper.post(url, ErisMethods.GET_BLOCKCHAIN_INFO.getMethod(), ErisChainInfo.class);
    }

    @Override
    public void authorize(String user, String password) {

    }

    public static void main(String[] args) throws IOException {
        ErisManager erisManager = new ErisManager("http://192.168.99.100:1337/rpc");
        System.out.println(erisManager.getChain());
    }
}
