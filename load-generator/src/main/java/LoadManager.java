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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author phd
 */
@Log4j2
public class LoadManager {
    private static final int BATCH_SIZE = 100;

    private List<String> targets;
    private int amountOfTransactions;
    private int amountOfThreadsPerTarget;
    private final int minLength;
    private final int maxLength;
    private ExecutorService executorService;
    private List<Logger> loggers;

    private final static String requestTemplate = "{\n" +
            "  \"jsonrpc\": \"2.0\",\n" +
            "  \"method\": \"invoke\",\n" +
            "  \"params\": {\n" +
            "      \"type\": 1,\n" +
            "      \"chaincodeID\":{\n" +
            "          \"name\":\"mycc\"\n" +
            "      },\n" +
            "      \"ctorMsg\": {\n" +
            "         \"args\":[\"write\", \"{message}\"]\n" +
            "      }\n" +
            "  },\n" +
            "  \"id\": 3\n" +
            "}";

    public LoadManager(List<String> targets, int amountOfTransactions, int amountOfThreadsPerTarget, int minLength, int maxLength) {
        this.targets = targets;
        this.amountOfTransactions = amountOfTransactions;
        this.amountOfThreadsPerTarget = amountOfThreadsPerTarget;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.loggers = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(amountOfThreadsPerTarget * targets.size());
    }

    public void start() throws IOException {
        for (String t: targets) {
            Logger logger = new Logger(Paths.get("load_logs", t + "_load.log"));
            loggers.add(logger);
            for (int i = 0; i < amountOfThreadsPerTarget; ++i) {
                executorService.submit(() -> {
                    List<String> logs = new ArrayList<>(BATCH_SIZE);
                    Random random = new Random();
                    for (int j = 0; j < amountOfTransactions; ++j) {
                        String message = generateMessage(random, minLength, maxLength);
                        long startTime = System.currentTimeMillis();
                        try {
                            HttpResponse<JsonNode> response = Unirest.post(
                                    "http://" + t + ":7050" + "/chaincode")
                                    .body(requestTemplate.replace("{message}", message))
                                    .asJson();
                            String id = response.getBody().getObject().getJSONObject("result").getString("message");

                            StringJoiner stringJoiner = new StringJoiner(",");
                            stringJoiner.add(id);
                            stringJoiner.add(Long.toString(startTime));
                            stringJoiner.add(Integer.toString(message.getBytes().length));
                            stringJoiner.add(Integer.toString(response.getStatus()));

                            logs.add(stringJoiner.toString());
                            if (j == amountOfTransactions - 1 || j % BATCH_SIZE == 0) {
                                logger.addLogs(logs);
                                logs = new ArrayList<>(BATCH_SIZE);
                            }
                        } catch (UnirestException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public void waitCompletion() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(24, TimeUnit.HOURS);
            for (Logger logger: loggers) {
                logger.shutdown();
            }
            Unirest.shutdown();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final char[] symbols;
    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch)
            tmp.append(ch);
        for (char ch = 'a'; ch <= 'z'; ++ch)
            tmp.append(ch);
        symbols = tmp.toString().toCharArray();
    }

    public static String generateMessage(Random random, int minLength, int maxLength) {
        StringBuilder tmp = new StringBuilder();
        int length = minLength + random.nextInt(maxLength - minLength);
        for (int i = 0; i < minLength + length; ++i) {
            tmp.append(symbols[random.nextInt(symbols.length)]);
        }
        return tmp.toString();
    }
}
