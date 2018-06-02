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

package uk.dsxt.bb.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class JSONRPCHelper {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static AtomicInteger id = new AtomicInteger();
    private static HttpHelper httpHelper = new HttpHelper(120000, 120000);

    public static String post(String url, String method, Object... parameters) {
        try {

            Gson gson = new Gson();

            JsonObject req = new JsonObject();
            req.addProperty("id", id);
            req.addProperty("method", method);

            JsonArray params = new JsonArray();
            if (parameters != null) {
                for (Object o : parameters) {
                    params.add(gson.toJsonTree(o));
                }
            }

            req.add("params", params);

            String requestData = req.toString();

            String request = httpHelper.request(url, requestData, RequestType.POST);
            JsonParser parser = new JsonParser();
            JsonObject resp = (JsonObject) parser.parse(new StringReader(request));
            JsonElement result = resp.get("result");
            id.getAndIncrement();
            return result.toString();
        } catch (Exception e) {
            log.error("post method failed", e);
        }
        return null;
    }

    public static <T> T post(String url, String method, Class<T> tClass, Object... args) {
        return new Gson().fromJson(JSONRPCHelper.post(url, method, args), (Type) tClass);
    }

    public static String postToSendTransactionEthereum(String url, String sender, String receiver, String amount) {
        try {
            String params = String.format("{\"jsonrpc\":\"2.0\"," +
                    "\"method\":\"eth_sendTransaction\",\"params\":[{\"from\":\"%s\",\"to\":\"%s\"," +
                            "\"value\":\"%s\"}],\"id\":%s}",
                    sender, receiver, amount, id);
            String responseString = httpHelper.request(url, params, RequestType.POST);
            JsonNode responseJson = mapper.readTree(responseString);
            return responseJson.get("result").textValue();
        } catch (Exception e) {
            log.error("Cannot run post method for sending transactions in Ethereum", e);
        }

        return Strings.EMPTY;
    }

    public static String postToSendMessageEthereum(String url, String sender, String receiver, String message, String amount) {
        String responseString = "response is not received";
        try {
            String params = String.format("{\"jsonrpc\":\"2.0\"," +
                            "\"method\":\"eth_sendTransaction\",\"params\":[{\"from\":\"%s\",\"to\":\"%s\"," +
                            "\"value\":\"%s\", \"data\":\"%s\"}],\"id\":%s}",
                    sender, receiver, amount, "0x" + Hex.encodeHexString(message.getBytes()), id);

            responseString = httpHelper.request(url, params, RequestType.POST);
            JsonResult result = mapper.readValue(responseString, JsonResult.class);
            if (result == null || result.getResult() == null)  {
                log.warn("null result, response=" + responseString);
                return Strings.EMPTY;
            } else {
                return result.getResult();
            }
        } catch (Exception e) {
            log.error("Cannot run post method for sending transactions in Ethereum, response=" + responseString, e);
        }

        return Strings.EMPTY;
    }

    /**
     * As only {@link #result} field is important, don't parse whole object
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class JsonResult {
        @JsonProperty
        @Getter
        private String result;
    }
}
