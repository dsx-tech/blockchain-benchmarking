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

package uk.dsxt.fabric;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChainCodeException;
import org.hyperledger.fabric.sdk.exception.EnrollmentException;
import org.hyperledger.fabric.sdk.exception.RegistrationException;
import uk.dsxt.blockchain.Manager;
import uk.dsxt.blockchain.Message;
import uk.dsxt.datamodel.fabric.FabricBlock;
import uk.dsxt.datamodel.fabric.FabricChain;
import uk.dsxt.datamodel.fabric.FabricPeer;
import uk.dsxt.utils.PrintOutputToConsole;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FabricManager implements Manager {

    private static final String HOME_PATH = System.getProperty("user.home");

    private static final String CHAINCODE_PATH = "github.com/hyperledger/fabric/examples/chaincode/go/evoting";
    private static final String CHAINCODE_NAME = "mycc";
    private static final String AFFILIATION = "bank_a";
    private static final String KEY_VALUE_STORE = "/test.properties";

    private static final String DOCKER_VOLUME_SOCK = "-v /var/run/docker.sock:/var/run/docker.sock";
    private static final String DOCKER_RUN_COMMAND = "docker run --rm -i";
    private static final String DOCKER_RUN_FABRIC_MEMBERSRVC = "hyperledger/fabric-membersrvc membersrvc";
    private static final String DOCKER_PORT_MEMBERSRVC = "-p 7054:7054";

    private static final String DOCKER_FIRST_PEER_PORT = "-p 7051:7051";
    private static final String DOCKER_VOLUME_PATH_TO_CHAINCODE = String.format("-v %s/go/src/github.com/hyperledger/" +
            "fabric/examples/chaincode:/opt/gopath/src/github.com/hyperledger/fabric/examples/chaincode", HOME_PATH);
    private static final String DOCKER_CORE_LOGGING_LEVEL = "-e CORE_LOGGING_LEVEL=DEBUG";
    private static final String DOCKER_CORE_PEER_ID = "-e CORE_PEER_ID=vp0";
    private static final String DOCKER_CORE_PEER_ADDRESSAUTODETECT = "-e CORE_PEER_ADDRESSAUTODETECT=false";
    private static final String DOCKER_CORE_PEER_ADDRESS = "-e CORE_PEER_ADDRESS=";
    private static final String DOCKER_CORE_PBFT_GENERAL_N = "-e CORE_PBFT_GENERAL_N=4";
    private static final String DOCKER_CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN = "-e CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN=pbft";
    private static final String DOCKER_CORE_PBFT_GENERAL_MODE = "-e CORE_PBFT_GENERAL_MODE=batch";
    private static final String DOCKER_CORE_GENERAL_TIMEOUT_REQUEST = "-e CORE_PBFT_GENERAL_TIMEOUT_REQUEST=1.5s";
    private static final String DOCKER_CORE_PBFT_GENERAL_BATCHSIZE = "-e CORE_PBFT_GENERAL_BATCHSIZE=1";
    private static final String DOCKER_CORE_PBFT_GENERAL_VIEWCHANGEPERIOD = "-e CORE_PBFT_GENERAL_VIEWCHANGEPERIOD=2";
    private static final String DOCKER_CORE_PBFT_GENERAL_TIMEOUT_NULLREQUEST = "-e CORE_PBFT_GENERAL_TIMEOUT_NULLREQUEST=2.25s";
    private static final String DOCKER_PEER_NODE_START = "hyperledger/fabric-peer peer node start";
    private static final String DOCKER_PEER_DISCOVERY_ROOTNODE = "-e CORE_PEER_DISCOVERY_ROOTNODE=";

    private static final String START_PEER = String.join(" ", DOCKER_RUN_COMMAND, DOCKER_VOLUME_SOCK,
            DOCKER_VOLUME_PATH_TO_CHAINCODE, DOCKER_CORE_LOGGING_LEVEL, DOCKER_CORE_PEER_ID, DOCKER_CORE_PEER_ADDRESSAUTODETECT,
            DOCKER_CORE_PEER_ADDRESS + "172.17.0.3:7051",
            DOCKER_CORE_PBFT_GENERAL_N, DOCKER_CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN, DOCKER_CORE_PBFT_GENERAL_MODE,
            DOCKER_CORE_GENERAL_TIMEOUT_REQUEST, DOCKER_CORE_PBFT_GENERAL_BATCHSIZE, DOCKER_CORE_PBFT_GENERAL_VIEWCHANGEPERIOD,
            DOCKER_CORE_PBFT_GENERAL_TIMEOUT_NULLREQUEST );

    private static final String START_FIRST_PEER = String.join(" ", START_PEER,
            DOCKER_FIRST_PEER_PORT, DOCKER_PEER_NODE_START);

    private static final String CHAIN_REQUEST = "chain";
    private static final String BLOCK_REQUEST = "chain/blocks/";
    private static final String PEERS_REQUEST = "network/peers";

    private static final Logger log = LogManager.getLogger(FabricManager.class.getName());

    private String chainName;
    private String admin;
    private String passphrase;
    private String peer;
    private String memberServiceUrl;
    private String peerToConnect;
    private boolean isInit;
    private int validatingPeerID;

    private ChainCodeResponse deployResponse;
    private Process fabricProcess;
    private Process memberService;
    private Chain chain;

    enum ChaincodeFunction {INIT, READ, WRITE}

    public FabricManager(String admin, String passphrase, String memberServiceUrl,
                         String peer, boolean isInit, int validatingPeerID, String peerToConnect) throws InterruptedException {
        this.admin = admin;
        this.passphrase = passphrase;
        this.memberServiceUrl = memberServiceUrl;
        this.peer = peer;
        this.isInit = isInit;
        this.peerToConnect = peerToConnect;
        FabricManager.setEnv("GOPATH", HOME_PATH.concat("/go"));
        try {
            Runtime rt = Runtime.getRuntime();
            if (!isInit) {
                memberService = rt.exec(String.join(" ", DOCKER_RUN_COMMAND, DOCKER_VOLUME_SOCK, DOCKER_PORT_MEMBERSRVC,
                        DOCKER_RUN_FABRIC_MEMBERSRVC));
                fabricProcess = rt.exec(START_FIRST_PEER);
            } else {
                int peerID = validatingPeerID + 3;
                TimeUnit.SECONDS.sleep(validatingPeerID);
                String stPeer = START_PEER.replaceAll("CORE_PEER_ADDRESS=172.17.0.3:7051",
                        String.format("CORE_PEER_ADDRESS=172.17.0.%d:7051", peerID))
                        .replaceFirst("vp0", String.format("vp%d", validatingPeerID));
                String startAnotherPeer = String.join(" ", stPeer,
                        DOCKER_PEER_DISCOVERY_ROOTNODE.concat(peerToConnect), DOCKER_PEER_NODE_START);
                fabricProcess = rt.exec(startAnotherPeer);
            }
            start();
            TimeUnit.SECONDS.sleep(4);
            chain = new Chain(chainName);

            chain.setMemberServicesUrl(memberServiceUrl, null);

            chain.setKeyValStore(new FileKeyValStore(HOME_PATH.concat(KEY_VALUE_STORE)));
            chain.addPeer(peer, null);

            Member registrar = chain.getMember(admin);
            if (!registrar.isEnrolled()) {
                registrar = chain.enroll(admin, passphrase);
            }
            chain.setRegistrar(registrar);
            deployResponse = initChaincode();
        } catch (CertificateException | IOException | EnrollmentException e) {
            log.error("Failed to init FabricManager instance", e);
        }
    }

    @Override
    public void start() {
        PrintOutputToConsole errorReported = PrintOutputToConsole.getStreamWrapper(fabricProcess.getErrorStream(),
                "ERROR");
        PrintOutputToConsole outputMessage = PrintOutputToConsole.getStreamWrapper(fabricProcess.getInputStream(),
                "OUTPUT");

        errorReported.start();
        outputMessage.start();
    }

    private static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }

    @Override
    public void stop() {
        try {
            if (fabricProcess.isAlive())
                fabricProcess.destroyForcibly();
            if (memberService.isAlive())
                memberService.destroyForcibly();
        } catch (Exception e) {
            log.error("stop method failed", e);
        }
    }
    
    private ChainCodeResponse initChaincode() {
        DeployRequest request = new DeployRequest();

        request.setChaincodePath(CHAINCODE_PATH);
        request.setArgs(new ArrayList<>(Collections.singletonList(ChaincodeFunction.INIT.name().toLowerCase())));
        
        Member member = getMember(admin, AFFILIATION);
        request.setChaincodeName(CHAINCODE_NAME);

        return member.deploy(request);
    }

    @Override
    public String sendMessage(byte[] body) {

        InvokeRequest request = new InvokeRequest();

        request.setArgs(new ArrayList<>(Arrays.asList(ChaincodeFunction.WRITE.name().toLowerCase(),
                new String(body, StandardCharsets.UTF_8))));
        request.setChaincodeID(deployResponse.getChainCodeID());
        request.setChaincodeName(deployResponse.getChainCodeID());

        Member member = getMember(admin, AFFILIATION);
        String transactionID = null;
        try {
            transactionID = member.invoke(request).getMessage();
        } catch (ChainCodeException e) {
            log.error("Cannot send message", e);
        }
        
        return transactionID;
    }

    @Override
    public List<Message> getNewMessages() {

        List<Message> result = new ArrayList<>();

        String amountOfMessages = "0";

        ChainCodeResponse chainCodeResponse = getNewMessage(amountOfMessages);

        if (chainCodeResponse != null) {
            amountOfMessages = chainCodeResponse.getMessage();
        }

        int amount = Integer.parseInt(amountOfMessages);

        for (int i = 1; i <= amount; i++) {
            chainCodeResponse  = getNewMessage(Integer.toString(i));
            if (chainCodeResponse != null) {
                result.add(new Message(Integer.toString(i), chainCodeResponse.getMessage(), true));
            }
        }
        result.forEach(message -> {
            String str = message.getBody();
            log.info(str.concat(" "));
        });
        return result;
    }
    
    private ChainCodeResponse getNewMessage(String id) {

        QueryRequest request = new QueryRequest();

        request.setArgs(new ArrayList<>(Arrays.asList(ChaincodeFunction.READ.name().toLowerCase(), id)));
        request.setChaincodeID(deployResponse.getChainCodeID ());
        request.setChaincodeName(deployResponse.getChainCodeID());

        Member member = getMember(admin, AFFILIATION);
        
        try {
            return member.query(request);
        } catch (ChainCodeException e) {
            log.error("Cannot get new messages", e);
        }

        return null;
    }
    
    private Member getMember(String enrollmentId, String affiliation) {
        Member member = chain.getMember(enrollmentId);
        if (!member.isRegistered()) {

            RegistrationRequest registrationRequest = new RegistrationRequest();
            registrationRequest.setEnrollmentID(enrollmentId);
            registrationRequest.setAffiliation(affiliation);

            try {
                member = chain.registerAndEnroll(registrationRequest);
            } catch (RegistrationException | EnrollmentException e) {
                log.error("Cannot register or enroll member", e);
            }
        } else if (!member.isEnrolled()) {
            try {
                member = chain.enroll(enrollmentId, member.getEnrollmentSecret());
            } catch (EnrollmentException e) {
                log.error("Cannot enroll user", e);
            }
        }
        return member;
    }

    private static void sleep(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            log.error("Exception in sleep method", e);
        }
    }

    private String convertGrpcToHttp(String grpc) {
        return String.join("", grpc.replaceFirst("grpc", "http").replaceFirst(".$", "0"), "/");
    }

    @Override
    public FabricChain getChain() throws IOException {
        String url = String.join("", convertGrpcToHttp(peer), CHAIN_REQUEST);
        try {
            String chain = Request.Get(url).execute().returnContent().asString();
            return new Gson().fromJson(chain, FabricChain.class);
        } catch (JsonParseException e) {
            log.error("Json parse error", e);
            throw e;
        } catch (Exception e) {
            log.error("Exception while calling url", e);
            throw e;
        }
    }

    @Override
    public FabricBlock getBlock(long id) throws IOException {
        try {
            String block = Request.Get(String.join("", convertGrpcToHttp(peer), BLOCK_REQUEST, Long.toString(id)))
                    .execute().returnContent().asString();
            return new Gson().fromJson(block, FabricBlock.class);
        } catch (JsonParseException e) {
            log.error("Json parse error", e);
            throw e;
        } catch (Exception e) {
            log.error("Exception while calling url", e);
            throw e;
        }
    }

    @Override
    public FabricPeer[] getPeers() throws IOException {
        try {
            String peers = Request.Get(String.join("", convertGrpcToHttp(peer),
                    PEERS_REQUEST)).execute().returnContent().asString();
            return new Gson().fromJson(peers, FabricPeer[].class);
        } catch (JsonParseException e) {
            log.error("Json parse error", e);
            throw e;
        } catch (Exception e) {
            log.error("Exception while calling url", e);
            throw e;
        }
    }
}
