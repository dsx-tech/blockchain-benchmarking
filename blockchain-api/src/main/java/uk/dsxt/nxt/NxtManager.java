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

package uk.dsxt.nxt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.blockchain.Manager;
import uk.dsxt.blockchain.Message;
import uk.dsxt.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.datamodel.blockchain.BlockchainPeer;
import uk.dsxt.utils.HttpHelper;
import uk.dsxt.utils.PropertiesHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Log4j2
public class NxtManager implements Manager {

    private final File workingDir;
    private final String jarPath;
    private final ObjectMapper mapper;
    private final String mainAddress;
    private final String port;
    private final HttpHelper httpHelper;
    private final List<String> javaOptions = new ArrayList<>();

    private final String passphrase;
    private final String name;
    private final String nxtPropertiesPath;
    private final boolean useUncommittedTransactions;

    private String accountId;
    private String selfAccount;
    private Process nxtProcess;
    private boolean isInitialized = false;

    private Set<String> loadedTransactions = new HashSet<>();
    private Set<String> loadedBlocks = new HashSet<>();

    public NxtManager(Properties properties, String nxtPropertiesPath, String name, String mainAddress, String passphrase,
                            int connectionTimeout, int readTimeout) {
        this.nxtPropertiesPath = nxtPropertiesPath;
        this.name = name;
        this.mainAddress = mainAddress;
        this.passphrase = passphrase;
        this.useUncommittedTransactions = Boolean.valueOf(properties.getProperty("nxt.useUncommittedTransactions",
                Boolean.FALSE.toString()));
        workingDir = new File(System.getProperty("user.dir"));
        log.info("Working directory (user.dir): {}", workingDir.getAbsolutePath());

        jarPath = properties.getProperty("nxt.jar.path");

        String javaOptionsStr = properties.getProperty("nxt.javaOptions");
        if (javaOptionsStr != null && !javaOptionsStr.isEmpty()) {
            for (String property : javaOptionsStr.split(";")) {
                if (!property.isEmpty())
                    javaOptions.add(property);
            }
        }

        httpHelper = new HttpHelper(connectionTimeout, readTimeout);

        mapper = new ObjectMapper();
        mapper.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Properties nxtProperties = PropertiesHelper.loadPropertiesFromPath(nxtPropertiesPath);
        port = properties.getProperty("nxt.apiServerPort");
        nxtProperties.setProperty("nxt.peerServerPort", properties.getProperty("nxt.peerServerPort"));
        nxtProperties.setProperty("nxt.apiServerPort", port);
        nxtProperties.setProperty("nxt.dbDir", properties.getProperty("nxt.dbDir"));
        nxtProperties.setProperty("nxt.testDbDir", properties.getProperty("nxt.dbDir"));
        nxtProperties.setProperty("nxt.defaultPeers", properties.getProperty("nxt.defaultPeers"));
        nxtProperties.setProperty("nxt.defaultTestnetPeers", properties.getProperty("nxt.defaultTestnetPeers"));
        nxtProperties.setProperty("nxt.isOffline", properties.getProperty("nxt.isOffline"));
        nxtProperties.setProperty("nxt.isTestnet", properties.getProperty("nxt.isTestnet"));
        nxtProperties.setProperty("nxt.timeMultiplier", properties.getProperty("nxt.timeMultiplier"));
        try (FileOutputStream fos = new FileOutputStream(nxtPropertiesPath)) {
            nxtProperties.store(fos, "");
        } catch (Exception e) {
            String errorMessage = String.format("Can't save wallet. Error: %s", e.getMessage());
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

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
    public BlockchainBlock getBlock(String peerURL, long id) throws IOException {
        return null;
    }

    @Override
    public BlockchainPeer[] getPeers(String peerURL) throws IOException {
        return null;
    }

    @Override
    public BlockchainChainInfo getChain(String peerURL) throws IOException {
        return null;
    }
}
