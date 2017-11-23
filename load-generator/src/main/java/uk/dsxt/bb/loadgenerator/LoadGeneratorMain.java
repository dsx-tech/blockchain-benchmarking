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

package uk.dsxt.bb.loadgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.loadgenerator.data.Credential;
import uk.dsxt.bb.remote.instance.WorkFinishedTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author phd
 */
@Log4j2
public class LoadGeneratorMain {
    public static void main(String[] args) throws IOException {
        try {
            if (args.length < 8) {
                log.error("Incorrect arguments count in LoadGeneratorMain.main(). Need min 10. Actual args: {}", Arrays.toString(args));
                return;
            }
            int amountOfTransactions = Integer.parseInt(args[0]);
            int amountOfThreadsPerTarget = Integer.parseInt(args[1]);
            int minLength = Integer.parseInt(args[2]);
            int maxLength = Integer.parseInt(args[3]);
            int delay = Integer.parseInt(args[4]);
            String ip = args[5];
            String masterHost = args[6];
            String credentialsPath = args[7];
            String blockchainType = args[8];
            String blockchainPort = args[9];
            List<String> targets = new ArrayList<>();
            targets.addAll(Arrays.asList(args).subList(10, args.length));

//            List<Credential> accounts = new ArrayList<>();
//            if (Paths.get(credentialsPath).toFile().exists()) {
//                Files.readAllLines(Paths.get(credentialsPath)).forEach(line -> {
//                            String[] credential = line.split(" ");
//                            if (credential.length != 2) {
//                                log.error("Invalid credential: " + line);
//                                return;
//                            }
//                            accounts.add(new Credential(credential[0], credential[1]));
//                        }
//                );
//            }

            LoadManager loadManager = new LoadManager(targets, null, amountOfTransactions, amountOfThreadsPerTarget, minLength, maxLength, delay, blockchainType, blockchainPort);

            loadManager.start();
            loadManager.waitCompletion();

            log.info("Load generation finished, sending info to master node " + "http://" + masterHost + "/loadGenerator/state");
            WorkFinishedTO remoteInstanceStateTO = new WorkFinishedTO(ip);
            ObjectMapper mapper = new ObjectMapper();

            HttpResponse<JsonNode> response = Unirest.post("http://" + masterHost + "/loadGenerator/state")
                    .body(mapper.writeValueAsString(remoteInstanceStateTO)).asJson();
            log.info("Info sended to master node, response {}", response);
        } catch (IOException | UnirestException e) {
            log.error("Couldn't start Load Generator module. {}", e);
        }
        finally {
            Unirest.shutdown();
        }
    }
}
