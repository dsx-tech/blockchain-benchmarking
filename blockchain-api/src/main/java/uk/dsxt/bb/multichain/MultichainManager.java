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

package uk.dsxt.bb.multichain;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.blockchain.Message;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.bb.datamodel.blockchain.BlockchainPeer;
import uk.dsxt.bb.datamodel.multichain.MultichainBlock;
import uk.dsxt.bb.datamodel.multichain.MultichainInfo;
import uk.dsxt.bb.datamodel.multichain.MultichainPeer;
import uk.dsxt.bb.utils.InternalLogicException;
import uk.dsxt.bb.utils.JSONRPCHelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.List;

@AllArgsConstructor
@Log4j2
public class MultichainManager implements Manager {

    private String url;

    private enum MultichainMethods {
        GETBLOCK,
        GETINFO,
        GETNEWADDRESS,
        GETPEERINFO,
        SEND
    }

    private String sendMessage(String address, byte[] body) throws IOException {
        try {
            return JSONRPCHelper.post(url, MultichainMethods.SEND.name().toLowerCase(),
                    address, MultichainManager.toBigDecimal(body));
        } catch (InternalLogicException e) {
            log.error("Cannot send transaction", e);
        }
        return Strings.EMPTY;
    }
    @Override
    public String sendTransaction(String to, String from, String amount) {
        try {
            return sendMessage(to, amount.getBytes());
        } catch (IOException e) {
            log.error("Sending transaction failed", e);
        }
        return null;
    }

    @Override
    public String sendMessage(byte[] body) {
        throw new NotImplementedException();
    }

    @Override
    public List<Message> getNewMessages() {
        throw new NotImplementedException();
    }

    @Override
    public BlockchainBlock getBlock(long blockId) throws IOException {
        return JSONRPCHelper.post(url, MultichainMethods.GETBLOCK.name().toLowerCase(), MultichainBlock.class, blockId);
    }

    @Override
    public BlockchainPeer[] getPeers() throws IOException {
        return JSONRPCHelper.post(url, MultichainMethods.GETPEERINFO.name().toLowerCase(),
                MultichainPeer[].class);
    }

    @Override
    public BlockchainChainInfo getChain() throws IOException {
        return JSONRPCHelper.post(url, MultichainMethods.GETINFO.name().toLowerCase(), MultichainInfo.class);
    }

    @Override
    public void authorize(String user, String password) {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        });
    }

    public String getNewAddress() throws IOException {
        try {
            String postResult = JSONRPCHelper.post(url, MultichainMethods.GETNEWADDRESS.name().toLowerCase());
            if (postResult != null) {
                return postResult.replaceAll("^.|.$", "");
            }
        } catch (InternalLogicException e) {
            log.error("Cannot get new bitcoin wallet address", e);
        }
        return Strings.EMPTY;
    }

    private static BigDecimal toBigDecimal(byte[] bytes) {
        return new BigDecimal(ByteBuffer.wrap(bytes).toString());
    }

}
