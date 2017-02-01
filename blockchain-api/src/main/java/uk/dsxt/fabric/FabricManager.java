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
    private static final String CHAIN_NAME = "chain1";
    private static final String ADMIN = "admin";
    private static final String PASSPHRASE = "Xurw3yU9zI0l";
    private static final String MEMBER_SERVICE_URL = "grpc://172.17.0.1:7054";
    private static final String KEY_VAL_STORE = "/test.properties";
    private static final String MAIN_PEER = "grpc://172.17.0.3:7051";
    private static final String PEER_FOR_LOGGER = String.join("",
            MAIN_PEER.replaceFirst("grpc", "http").replaceFirst(".$", "0"), "/");
    private static final String CHAINCODE_PATH = "github.com/hyperledger/fabric/examples/chaincode/go/evoting";
    private static final String AFFILIATION = "bank_a";
    private static final String CHAINCODE_NAME = "mycc";

    private static final String CHAIN_REQUEST = "chain";
    private static final String BLOCK_REQUEST = "chain/blocks/";
    private static final String PEERS_REQUEST = "network/peers";

    private static final Logger log = LogManager.getLogger(FabricManager.class.getName());

    private final String peerToConnect;
    private final String memberServiceUrl;
    private final String chainCodePath;
    private boolean isInit;

    private ChainCodeResponse deployResponse;
    private Process fabricProcess;
    private Chain chain;

    private ExecutorService executorService;

    public FabricManager(String chainCodePath, String memberServiceUrl, String peer, boolean isInit) {
        this.chainCodePath = chainCodePath;
        this.memberServiceUrl = memberServiceUrl;
        this.peerToConnect = peer;
        this.isInit = isInit;
        FabricManager.setEnv("GOPATH", HOME_PATH.concat("/go"));
    }

    @Override
    public void start() {
        try {
            if (!isInit) {
                Runtime rt = Runtime.getRuntime();
                fabricProcess = rt.exec("docker-compose up");
            }
            // sleep for initiating fabric. If we will not run sleep than we will get exception "Peer not respond"
            sleep(5);

            chain = new Chain(CHAIN_NAME);
            chain.setMemberServicesUrl(memberServiceUrl, null);
            chain.setKeyValStore(new FileKeyValStore(HOME_PATH + KEY_VAL_STORE));
            chain.addPeer(peerToConnect, null);

            Member registrar = chain.getMember(ADMIN);
            if (!registrar.isEnrolled()) {
                registrar = chain.enroll(ADMIN, PASSPHRASE);
            }
            chain.setRegistrar(registrar);

            // sleep for initializing variables in fabric manager

            deployResponse = initChaincode();

            // sleep for waiting when chaincode is deployed
            sleep(5);
        } catch (IOException | EnrollmentException | CertificateException e) {
            log.error("Cannot make instance of FabricManager class", e);
        }

        executorService = Executors.newFixedThreadPool(2);
        PrintOutputToConsole errorReported = PrintOutputToConsole.getStreamWrapper(fabricProcess.getErrorStream(), "ERROR");
        PrintOutputToConsole outputMessage = PrintOutputToConsole.getStreamWrapper(fabricProcess.getInputStream(), "OUTPUT");
        executorService.execute(errorReported);
        executorService.execute(outputMessage);
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
                executorService.shutdownNow();
                fabricProcess.destroyForcibly();
        } catch (Exception e) {
            log.error("stop method failed", e);
        }
    }
    
    private ChainCodeResponse initChaincode() {
        DeployRequest request = new DeployRequest();

        request.setChaincodePath(chainCodePath);
        request.setArgs(new ArrayList<>(Collections.singletonList("init")));
        
        Member member = getMember(ADMIN, AFFILIATION);
        request.setChaincodeName(CHAINCODE_NAME);

        return member.deploy(request);
    }

    @Override
    public String sendMessage(byte[] body) {

        InvokeRequest request = new InvokeRequest();

        request.setArgs(new ArrayList<>(Arrays.asList("write", new String(body, StandardCharsets.UTF_8))));
        request.setChaincodeID(deployResponse.getChainCodeID());
        request.setChaincodeName(deployResponse.getChainCodeID());

        Member member = getMember(ADMIN, AFFILIATION);
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

        request.setArgs(new ArrayList<>(Arrays.asList("read", id)));
        request.setChaincodeID(deployResponse.getChainCodeID ());
        request.setChaincodeName(deployResponse.getChainCodeID());

        Member member = getMember(ADMIN, AFFILIATION);
        
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

    @Override
    public FabricChain getChain(String peerURL) throws IOException {
        String url = String.join("", peerURL, CHAIN_REQUEST);
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
    public FabricBlock getBlock(String peerURL, long id) throws IOException {
        try {
            String block = Request.Get(String.join("", peerURL, BLOCK_REQUEST, Long.toString(id)))
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
    public FabricPeer[] getPeers(String peerURL) throws IOException {
        try {
            String peers = Request.Get(String.join("", peerURL, PEERS_REQUEST)).execute().returnContent().asString();
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
