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

import com.fasterxml.jackson.core.JsonParseException;
import com.google.gson.Gson;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.blockchain.Message;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.bb.datamodel.blockchain.BlockchainPeer;
import uk.dsxt.bb.datamodel.nem.NemBlock;
import uk.dsxt.bb.datamodel.nem.NemChainInfo;
import uk.dsxt.bb.datamodel.nem.NemPeers;
import uk.dsxt.bb.utils.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mikhail Wall
 */

public class NemManager implements Manager {

    private static final Logger log = LogManager.getLogger(NemManager.class.getName());

    private static final String CHAIN_REQUEST = "/chain/height";
    private static final String BLOCK_REQUEST = "/block/at/public";
    private static final String GET_PEERS_REQUEST = "/node/peer-list/all";

    private static final String URL = "http://127.0.0.1:7890";
    public static HttpHelper httpHelper = new HttpHelper(120000, 120000);

    public static void main(String[] args) throws IOException {
        Manager manager = new NemManager();
        //System.out.println(manager.getBlock(132611L).getHash());
        //        manager.getBlock(1L);
        Arrays.stream(manager.getPeers()).forEach(System.out::println);
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
        String request = String.join("", URL, BLOCK_REQUEST);
        try {
            String block = Request.Post(request)
                    .bodyString(String.format("{\"height\":%d}", id), ContentType.APPLICATION_JSON)
                    .execute().returnContent().asString();
            return new Gson().fromJson(block, NemBlock.class);
        } catch (JsonParseException e) {
            log.error("Json parse error", e);
            throw e;
        } catch (Exception e) {
            log.error("Exception while calling url", e);
            throw e;
        }
    }

    @Override
    public BlockchainPeer[] getPeers() throws IOException {
        String request = String.join("", URL, GET_PEERS_REQUEST);
        try {
            String peers = Request.Get(request).execute().returnContent().asString();
            return new Gson().fromJson(peers, NemPeers.class).getActive();
        } catch (JsonParseException e) {
            log.error("Json parse error", e);
            throw e;
        } catch (Exception e) {
            log.error("Exception while calling url", e);
            throw e;
        }
    }

    @Override
    public BlockchainChainInfo getChain() throws IOException {
        try {
            String request = String.join("", URL, CHAIN_REQUEST);
            String height = Request.Get(request).execute().returnContent().asString();

            return new Gson().fromJson(height, NemChainInfo.class);
        } catch (JsonParseException e) {
            log.error("Json parse error", e);
            throw e;
        } catch (Exception e) {
            log.error("Exception while calling url", e);
            throw e;
        }
    }

    @Override
    public void authorize(String user, String password) {

    }
}
