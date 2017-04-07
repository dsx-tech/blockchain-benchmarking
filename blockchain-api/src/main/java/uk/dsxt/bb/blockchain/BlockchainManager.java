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

package uk.dsxt.bb.blockchain;

import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.ethereum.EthereumManager;
import uk.dsxt.bb.bitcoin.BitcoinManager;
import uk.dsxt.bb.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.bb.datamodel.blockchain.BlockchainPeer;
import uk.dsxt.bb.fabric.FabricManager;
import uk.dsxt.bb.multichain.MultichainManager;

import java.io.IOException;
import java.util.List;

@Log4j2
public class BlockchainManager implements Manager {

    private String blockchainType;
    private String url;

    private Manager manager;

    public BlockchainManager(String blockchainType, String url) {
        this.blockchainType = blockchainType;
        this.url = url;
        switch (blockchainType) {
            case "fabric":
                manager = new FabricManager(url);
                break;
            case "bitcoin":
                manager = new BitcoinManager(url);
                break;
            case "ethereum":
                manager = new EthereumManager(url);
                break;
            case "multichain":
                manager = new MultichainManager(url);
                break;
            default:
                log.error(String.format("%s blockchain currently not supported or not exist. Currently supported: " +
                        "fabric, bitcoin, ethereum, multichain", blockchainType));
                break;
        }
    }

    @Override
    public String sendTransaction(String to, String from, String amount) {
        if (manager != null) {
            return manager.sendTransaction(to, from, amount);
        }

        return null;
    }

    @Override
    public String sendMessage(byte[] body) {
        if (manager != null)
            return manager.sendMessage(body);

        return null;
    }

    @Override
    public List<Message> getNewMessages() {
        if (manager != null)
            return manager.getNewMessages();

        return null;
    }

    @Override
    public BlockchainBlock getBlock(long id) throws IOException {
        if (manager != null)
            return manager.getBlock(id);

        return null;
    }

    @Override
    public BlockchainPeer[] getPeers() throws IOException {
        if (manager != null)
            return manager.getPeers();

        return null;
    }

    @Override
    public BlockchainChainInfo getChain() throws IOException {
        if (manager != null)
            return manager.getChain();

        return null;
    }

    @Override
    public void authorize(String user, String password) {
        if (manager != null)
            manager.authorize(user, password);
    }

    public static void main(String[] args) throws IOException {
        BlockchainManager manager = new BlockchainManager("bitcoin", "http://127.0.0.1:6290");
        manager.authorize("multichainrpc", "3NPieLHgUEfEsdJeQpQstPDmHX1yasatPAw3SYGtY2Jr");
        System.out.println(manager.getBlock(0));
    }
}
