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

package uk.dsxt.bb.fabric;

import java.io.File;

class FabricConstants {

    private FabricConstants() {};

    static final String HOME_PATH = System.getProperty("user.home");
    private static final String FILE_SEPARATOR = File.separator;

    private static final String FIRST_PEER_ADDRESS = "172.17.0.3:7051";

    private static final String DOCKER_VOLUME_SOCK = "-v /var/run/docker.sock:/var/run/docker.sock";
    private static final String DOCKER_RUN_COMMAND = "docker run --rm -i";
    private static final String DOCKER_RUN_FABRIC_MEMBERSRVC = "hyperledger/fabric-membersrvc membersrvc";
    private static final String DOCKER_PORT_MEMBERSRVC = "-p 7054:7054";

    private static final String DOCKER_FIRST_PEER_PORT = "-p 7051:7051";
    private static final String DOCKER_VOLUME_PATH_TO_CHAINCODE = String.format("-v %s" + FILE_SEPARATOR + "go" +
                    FILE_SEPARATOR + "src" + FILE_SEPARATOR + "github.com" + FILE_SEPARATOR + "hyperledger" +
                    FILE_SEPARATOR + "fabric" + FILE_SEPARATOR + "examples" + FILE_SEPARATOR +
                    "chaincode:/opt/gopath/src/github.com/hyperledger/fabric/examples/chaincode", HOME_PATH);
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

    static final String DOCKER_PEER_NODE_START = "hyperledger/fabric-peer peer node start";
    static final String DOCKER_PEER_DISCOVERY_ROOTNODE = "-e CORE_PEER_DISCOVERY_ROOTNODE=";
    static final String START_PEER = String.join(" ", DOCKER_RUN_COMMAND, DOCKER_VOLUME_SOCK,
            DOCKER_VOLUME_PATH_TO_CHAINCODE, DOCKER_CORE_LOGGING_LEVEL, DOCKER_CORE_PEER_ID, DOCKER_CORE_PEER_ADDRESSAUTODETECT,
            DOCKER_CORE_PEER_ADDRESS + FIRST_PEER_ADDRESS, DOCKER_CORE_PBFT_GENERAL_N, DOCKER_CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN,
            DOCKER_CORE_PBFT_GENERAL_MODE, DOCKER_CORE_GENERAL_TIMEOUT_REQUEST, DOCKER_CORE_PBFT_GENERAL_BATCHSIZE,
            DOCKER_CORE_PBFT_GENERAL_VIEWCHANGEPERIOD, DOCKER_CORE_PBFT_GENERAL_TIMEOUT_NULLREQUEST );

    static final String START_FIRST_PEER = String.join(" ", START_PEER,
            DOCKER_FIRST_PEER_PORT, DOCKER_PEER_NODE_START);

    static final String DOCKER_START_MEMBERSERVICE = String.join(" ", DOCKER_RUN_COMMAND, DOCKER_VOLUME_SOCK, DOCKER_PORT_MEMBERSRVC,
            DOCKER_RUN_FABRIC_MEMBERSRVC);

    static final String CHAIN_REQUEST = "chain";
    static final String BLOCK_REQUEST = "chain/blocks/";
    static final String PEERS_REQUEST = "network/peers";

    static final String WRITE_METHOD = "write";
    static final String READ_METHOD = "read";

    static final long TIMESTAMP_TO_GET_MESSAGES = -600000;
    static final String MESSAGE_SEPARATOR = ";";
}
