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

package uk.dsxt.bb.fabric;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.blockchain.Message;
import uk.dsxt.bb.datamodel.fabric.FabricChain;
import uk.dsxt.bb.datamodel.fabric.FabricBlock;
import uk.dsxt.bb.datamodel.fabric.FabricPeer;
import uk.dsxt.bb.datamodel.fabric.FabricTransaction;
import uk.dsxt.bb.utils.PropertiesHelper;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FabricManager implements Manager {

    private static final Logger log = LogManager.getLogger(FabricManager.class.getName());

    private static HFClient client = null;
    private static Channel channel = null;

    private String chaincodeName;
    private Peer peer;

    public FabricManager(String peer) {
        Properties properties = PropertiesHelper.loadProperties(String.format("fabric-%s", peer.replaceAll("[/:]", "")));
        this.chaincodeName = properties.getProperty(FabricProperties.CHAINCODE_NAME);
        try {
            initJavaSDK(peer, properties);
        } catch (BaseException e) {
            log.error("Failed to init fabric-java-sdk {}", e.getMessage());
        }
    }

    private void initJavaSDK(String peer, Properties properties) throws BaseException {
        client = HFClient.createNewInstance();
        log.info("Created new instance for HFCClient...");

        CryptoSuite cs = CryptoSuite.Factory.getCryptoSuite();
        User user = new FabricUser(
                properties.getProperty(FabricProperties.PATH_TO_PRIVATE_KEY),
                properties.getProperty(FabricProperties.PATH_TO_CERT),
                properties.getProperty(FabricProperties.ORGANISATION));

        try {
            log.info("Setting crypto suite...");
            client.setCryptoSuite(cs);
            log.info("Set crypto suite");

            log.info("Setting user {}...", user);
            client.setUserContext(user);
            log.info("Set user {}", user);

            log.info("Setting channel to client...");
            String channelName = properties.getProperty(FabricProperties.CHANNEL_NAME);
            channel = client.newChannel(channelName);
            log.info("Set channel to client");

            log.info("Adding peer with address {} to channel {}...", peer, channelName);
            this.peer = client.newPeer("peer", peer);
            channel.addPeer(this.peer);
            log.info("Added peer with address {} to channel {}", peer, channelName);

            String orderer = properties.getProperty(FabricProperties.ORDERER);
            log.info("Adding orderer with address {} to channel {}...", orderer, channelName);
            channel.addOrderer(client.newOrderer("orderer", orderer));
            log.info("Added new orderer with address {} to channel {}", orderer, channelName);

            log.info("Initializing channel {}...", channelName);
            channel.initialize();
            log.info("Initialized channel {}", channelName);
        } catch (InvalidArgumentException | TransactionException | CryptoException e) {
            log.error("Failed to initialize FabricManager: {}", e.getMessage());
            throw new BaseException("Failed to initialize FabricManager");
        }
    }

    private String timestampToKey(long timestamp) {
        return String.format("%016d", timestamp);
    }

    @Override
    public String sendTransaction(String to, String from, long amount) {
        return null;
    }

    @Override
    public String sendMessage(byte[] body) {
        String message = new String(body);
        TransactionProposalRequest req = client.newTransactionProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName(chaincodeName).build();
        req.setChaincodeID(cid);
        req.setFcn(FabricConstants.WRITE_METHOD);

        String timestampToKey = timestampToKey(Instant.now().toEpochMilli());
        req.setArgs(new String[] {message, timestampToKey});
        log.info("Executing message {}, timestamp {}", message, timestampToKey);
        Collection<ProposalResponse> resps;
        try {
            resps = channel.sendTransactionProposal(req);
            channel.sendTransaction(resps);

            return resps.stream().findFirst().get().getTransactionID();
        } catch (ProposalException | InvalidArgumentException e) {
            log.error("Failed to send transaction to blockchain {}", e);
        }

        return null;
    }

    @Override
    public String sendMessage(String from, String to, String message) {
        return null;
    }

    @Override
    public List<Message> getNewMessages() {
        QueryByChaincodeRequest req = client.newQueryProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName(chaincodeName).build();
        req.setChaincodeID(cid);
        req.setFcn(FabricConstants.READ_METHOD);

        String timestampToKey = timestampToKey(FabricConstants.TIMESTAMP_TO_GET_MESSAGES);
        req.setArgs(new String[]{timestampToKey});

        log.info("Getting new messages for timestamp {}...", timestampToKey);
        List<Message> result = new ArrayList<>();
        try {
            Collection<ProposalResponse> resps = channel.queryByChaincode(req);

            for (ProposalResponse resp : resps) {
                String payload = new String(resp.getChaincodeActionResponsePayload());
                log.info("Got messages: {}", payload);
                String[] messages = payload.split(FabricConstants.MESSAGE_SEPARATOR);
                int id = 0;
                for (String message : messages) {
                    result.add(new Message(Integer.toString(id), message, true));
                    id++;
                }
            }
        } catch (InvalidArgumentException | ProposalException e) {
            log.error("Failed to get messages {}", e);
        }
        return result;
    }

    @Override
    public FabricBlock getBlockById(long id) throws IOException {
        try {
            BlockInfo returnedBlock = channel.queryBlockByNumber(peer, id);
            FabricBlock fabricBlock = getBlock(returnedBlock);
            log.info("Fabric block got by id {}, block {}", id, fabricBlock);

            return fabricBlock;
        } catch (InvalidArgumentException | ProposalException e) {
            log.error("Failed to get block with id {}, {}", id, e);
        }
        return null;
    }

    private FabricBlock getBlock(BlockInfo block) throws IOException, InvalidArgumentException {

        List<FabricTransaction> transactions = StreamSupport.stream(block.getEnvelopeInfos().spliterator(), false).map(FabricTransaction::new).collect(Collectors.toList());

        final long blockNumber = block.getBlockNumber();
        return new FabricBlock(transactions,
                Hex.encodeHexString(SDKUtils.calculateBlockHash(
                        blockNumber,
                        block.getPreviousHash(),
                        block.getDataHash())),
                Hex.encodeHexString(block.getPreviousHash()));
    }

    @Override
    public FabricBlock getBlockByHash(String hash) throws IOException {
        try {
            BlockInfo returnedBlock = channel.queryBlockByHash(peer, Hex.decodeHex(hash.toCharArray()));

            FabricBlock fabricBlock = getBlock(returnedBlock);
            log.info("Fabric block got by hash {}, block {}", hash, fabricBlock);

            return fabricBlock;
        } catch (InvalidArgumentException | ProposalException | DecoderException e) {
            log.error("Failed to get block with {}, {}", hash, e);
        }
        return null;
    }

    @Override
    public FabricPeer[] getPeers() throws IOException {

        return channel.getPeers().stream().map(FabricPeer::new).toArray(FabricPeer[]::new);
    }

    @Override
    public FabricChain getChain() throws IOException {
        try {
            BlockchainInfo channelInfo = channel.queryBlockchainInfo(peer);

            FabricChain fabricChain = new FabricChain(
                    channelInfo.getHeight(),
                    Hex.encodeHexString(channelInfo.getCurrentBlockHash()),
                    Hex.encodeHexString(channelInfo.getPreviousBlockHash()));
            log.info("Got fabric chain info: {}", fabricChain);

            return fabricChain;
        } catch (ProposalException | InvalidArgumentException e) {
            log.error("Cannot get chain info from peer {}, error {}", peer, e);
        }
        return null;
    }

    @Override
    public void authorize(String user, String password) {

    }
}
