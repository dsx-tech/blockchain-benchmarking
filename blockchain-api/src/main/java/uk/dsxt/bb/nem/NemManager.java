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

package uk.dsxt.bb.nem;

import org.json.JSONObject;
import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.blockchain.Message;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.bb.datamodel.blockchain.BlockchainPeer;
import uk.dsxt.bb.utils.*;

import java.io.IOException;
import java.util.List;

/**
 * @author Mikhail Wall
 */

public class NemManager implements Manager {

    public static HttpHelper httpHelper = new HttpHelper(120000, 120000);

    public static void main(String[] args) throws IOException {
        Manager manager = new NemManager();
        manager.getBlock(1L);
    }

    @Override
    public String sendTransaction(String to, String from, long amount) {
        return null;
    }

    @Override
    public String sendMessage(byte[] body) {
        return null;
    }

    @Override
    public String sendMessage(String from, String to, String message) {
        return null;
    }

    @Override
    public List<Message> getNewMessages() {
        return null;
    }

    @Override
    public BlockchainBlock getBlock(long id) throws IOException {
//            String ans = httpHelper.request("http://127.0.0.1:7890/chain/height", RequestType.GET);
//            System.out.println(ans);
            //JSONRPCHelper.post("http://127.0.0.1:7890/block/at/public", null, )
//        JSONObject block = null;
//        JSONObject urlParam = new JSONObject();
//        urlParam.put("height", height);
//        String blockString = HttpUtil.httpPostWithJSON("/block/at/public", urlParam.toString());
//        if(blockString==null || "".equals(blockString.trim())){
//            System.out.println("fail to get the Nemesis block data!");
//            return null;
//        }
//        block = JSONObject.fromObject(blockString);
//        if(block==null){
//            System.out.println("fail to get the Nemesis block data!");
//            return null;
//        }
//        return block;
        return null;
    }

    @Override
    public BlockchainPeer[] getPeers() throws IOException {
        return new BlockchainPeer[0];
    }

    @Override
    public BlockchainChainInfo getChain() throws IOException {
        return null;
    }

    @Override
    public void authorize(String user, String password) {

    }
}
