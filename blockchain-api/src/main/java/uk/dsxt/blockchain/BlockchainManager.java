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

import uk.dsxt.bitcoin.BitcoinManager;
import uk.dsxt.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.datamodel.blockchain.BlockchainPeer;
import uk.dsxt.ethereum.EthereumManager;
import uk.dsxt.fabric.FabricManager;
import uk.dsxt.utils.PropertiesHelper;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class BlockchainManager implements Manager {

    private String blockchainType = "fabric";
    private Properties properties = PropertiesHelper.loadProperties("blockchain");

    private Manager manager;

    BlockchainManager(Properties properties, String blockchainType) {
        this.blockchainType = blockchainType;
        switch (blockchainType) {
            case "fabric":
                try {
                    manager = new FabricManager(
                            getProperty("admin"),
                            getProperty("passphrase"),
                            getProperty("memberServiceUrl"),
                            getProperty("peer"),
                            Boolean.parseBoolean(getProperty("isInit")),
                            Integer.parseInt(getProperty("validatingPeerID")),
                            getProperty("peerToConnect"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case "bitcoin":
                manager = new BitcoinManager(
                        getProperty("url"),
                        getProperty("rpcuser"),
                        getProperty("rpcpassword"),
                        getProperty("datadir")
                );
                break;
            case "ethereum":
                manager = new EthereumManager (
                        getProperty("url"),
                        Integer.parseInt(getProperty("rpcport")),
                        getProperty("rpcapi"),
                        getProperty("rpccorsdomain"),
                        getProperty("datadir"),
                        Integer.parseInt(getProperty("networkid")),
                        Integer.parseInt(getProperty("port")),
                        Integer.parseInt(getProperty("maxpeers")),
                        getProperty("genesis_directory")
                );
        }
    }

    private String getProperty(String name) {
        return properties.getProperty(String.format("%s.%s", blockchainType, name));
    }

    @Override
    public String sendMessage(byte[] body) {
        return manager.sendMessage(body);
    }

    @Override
    public List<Message> getNewMessages() {
        return manager.getNewMessages();
    }

    @Override
    public BlockchainBlock getBlock(String peerURL, long id) throws IOException {
        return manager.getBlock(peerURL, id);
    }

    @Override
    public BlockchainPeer[] getPeers(String peerURL) throws IOException {
         return manager.getPeers(peerURL);
    }

    @Override
    public BlockchainChainInfo getChain(String peerURL) throws IOException {
        return manager.getChain(peerURL);
    }

    @Override
    public void start() {
        manager.start();
    }

    @Override
    public void stop() {
        manager.stop();
    }
}
