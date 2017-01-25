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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author phd
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String loadTargetHost = args[0];
        int amountOfTransactions = Integer.parseInt(args[1]);

        String requestBody ="{\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"method\": \"invoke\",\n" +
                "  \"params\": {\n" +
                "      \"type\": 1,\n" +
                "      \"chaincodeID\":{\n" +
                "          \"name\":\"mycc\"\n" +
                "      },\n" +
                "      \"ctorMsg\": {\n" +
                "         \"args\":[\"invoke\", \"b\", \"a\", \"10\"]\n" +
                "      }\n" +
                "  },\n" +
                "  \"id\": 3\n" +
                "}";

        List<Future<HttpResponse<JsonNode>>> responses = new ArrayList<>();
        for (int i = 0; i < amountOfTransactions; ++i) {
            responses.add(Unirest.post(
                    "http://" + loadTargetHost + ":7050" + "/chaincode")
                    .body(requestBody)
                    .asJsonAsync());
        }
        while (!responses.stream().allMatch(f -> f.isDone() || f.isCancelled())) {
            Thread.sleep(10000);
        }
    }
}
