/******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2017 DSX Technologies Limited.                               *
 * *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 * *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 * *
 * Removal or modification of this copyright notice is prohibited.            *
 * *
 ******************************************************************************/

package uk.dsxt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.dsxt.remote.instance.LoadGeneratorInstance;
import uk.dsxt.remote.instance.LoadGeneratorInstancesManager;
import uk.dsxt.remote.instance.RemoteInstance;
import uk.dsxt.remote.instance.RemoteInstancesManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;


/**
 * @author phd
 */
public class Main {
    private final static Logger logger = LogManager.getLogger(Main.class);
    private final static String userNameOnRemoteInstance = "ec2-user";
    private final static String prefix = "hyperledger";
    public static void main(String[] args) throws Exception {
        String pemKeyPath = args[0];
        Path logPath = Paths.get(prefix, "log");
        logger.debug("pem key path: " + pemKeyPath);
        final int blockchainInstancesAmount = 4;
        final int loadGeneratorInstancesAmount = 1;
        if (!Files.exists(Paths.get(prefix, "instances"))) {
            logger.error("File \"instances\" doesn't exists");
            return;
        }
        List<String> allHosts = Files.readAllLines(Paths.get(prefix, "instances"));

        List<RemoteInstance> blockchainInstances = allHosts.subList(0, blockchainInstancesAmount)
                                                            .stream()
                                                            .map(host -> new RemoteInstance(userNameOnRemoteInstance, host, 22, pemKeyPath, logPath))
                                                            .collect(Collectors.toList());

        List<LoadGeneratorInstance> loadGeneratorInstances = new ArrayList<>();
        for (int i = 0; i < loadGeneratorInstancesAmount; ++i) {
            loadGeneratorInstances.add(new LoadGeneratorInstance(userNameOnRemoteInstance,
                    allHosts.get(i + loadGeneratorInstancesAmount), 22, pemKeyPath, blockchainInstances.get(i).getHost(), logPath));
        }
        runBlockchain(blockchainInstances);
        runLoadGenerators(loadGeneratorInstances, blockchainInstances);
    }

    private static void runBlockchain(List<RemoteInstance> blockchainInstances) throws Exception {
        RemoteInstancesManager<RemoteInstance> blockchainInstancesManager = new RemoteInstancesManager<>();
        blockchainInstancesManager.setRootInstance(blockchainInstances.get(0));
        blockchainInstancesManager.addCommonInstances(blockchainInstances.subList(1, blockchainInstances.size()));

        blockchainInstancesManager.uploadFilesForAll(Arrays.asList(
                Paths.get(prefix, "chaincode_example02"),
                Paths.get(prefix, "initEnv.sh")
        ));

        blockchainInstancesManager.executeCommandsForAll(singletonList("bash initEnv.sh"));

        List<Path> rootFiles = new ArrayList<>();
        rootFiles.add(Paths.get(prefix, "root", "docker-compose.yml"));
        rootFiles.add(Paths.get(prefix, "root", "startRoot.sh"));
        blockchainInstancesManager.uploadFilesForRoot(rootFiles);
        blockchainInstancesManager.executeCommandsForRoot(singletonList(
                "bash startRoot.sh && touch startRoot.complete")
        );

        List<Path> commonFiles = new ArrayList<>();
        commonFiles.add(Paths.get(prefix, "common", "docker-compose.yml"));
        commonFiles.add(Paths.get(prefix, "common", "startCommon.sh"));
        blockchainInstancesManager.uploadFilesForCommon(commonFiles);
        blockchainInstancesManager.executeCommandsForCommon(Collections.singletonList(
                "bash startCommon.sh && touch startCommon.complete")
        );

        sleep(20000);
        blockchainInstancesManager.executeCommandsForAll(singletonList("CORE_CHAINCODE_ID_NAME=mycc " +
                "CORE_PEER_ADDRESS=0.0.0.0:7051 " +
                "nohup ./chaincode_example02 >/dev/null 2>chaincode.log &")
        );
        logger.info("Chaincode started");
        sleep(3000);
        blockchainInstancesManager.stop();
    }
    private static void runLoadGenerators(List<LoadGeneratorInstance> loadGeneratorInstances,
                                          List<RemoteInstance> blockchainInstances) throws Exception {
        logger.debug("runLoadGenerators start");
        if (loadGeneratorInstances.size() != blockchainInstances.size()) {
            throw new IllegalArgumentException("amount of loaderGenerators must be equals amount of blockchain instances");
        }

        LoadGeneratorInstancesManager loadGeneratorsManager = new LoadGeneratorInstancesManager();
        loadGeneratorsManager.setRootInstance(loadGeneratorInstances.get(0));
        loadGeneratorsManager.addCommonInstances(loadGeneratorInstances);

        loadGeneratorsManager.executeCommandsForCommon(Arrays.asList(
                "pkill -f 'java -jar'",
                "sudo yum -y install java-1.8.0")
        );
        loadGeneratorsManager.uploadFilesForCommon(singletonList(Paths.get(prefix, "load-generator.jar")));

        loadGeneratorsManager.executeCommandsForCommon(singletonList(
                "nohup java -jar load-generator.jar ${LOAD_TARGET} 1000 >/dev/null 2>load-generator-stdout.log &"));

        logger.debug("runLoadGenerators end");
        loadGeneratorsManager.stop();
    }
}
