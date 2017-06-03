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

package uk.dsxt.bb.eris;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.blockchain.Message;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.bb.datamodel.blockchain.BlockchainPeer;
import uk.dsxt.bb.datamodel.eris.ErisBlock;
import uk.dsxt.bb.datamodel.eris.ErisChainInfo;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Log4j2
public class ErisManager implements Manager {

    private String url;

    public ErisManager(String url) {
        this.url = url;
    }

    @Override
    public String sendTransaction(String to, String from, long amount) {
        return null;
    }

    @Override
    public String sendMessage(byte[] body) {
        String pathToApp = "/Users/mikhwall/.monax/apps/idi/app.js";
        Path path = Paths.get(pathToApp);
        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            //content =
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    public String sendReq() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://127.0.0.1:1337/unsafe/txpool");

        String json = "{\"priv_key\":\"23BCBF9CF741DA0E853879D5F29308971C32C0C269A03CEEEED13B2065781EADB162D3371F160B63723981B27CB9FA2C80C242E2895BEBB8CD3474679F7A4F05\"," +
                "\"data\":\"hello, world\", \"address\":\"68921F57F0C4BC2169F15F439EC9FEBE9C8387EF\", \"fee\":0.0001, \"gas_limit\":0.001}";
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        CloseableHttpResponse response = httpClient.execute(httpPost);
        System.out.println(response.toString());
        httpClient.close();
        return response.toString();
    }

    public void req() throws IOException {
        String json = "{\"priv_key\":\"23BCBF9CF741DA0E853879D5F29308971C32C0C269A03CEEEED13B2065781EADB162D3371F160B63723981B27CB9FA2C80C242E2895BEBB8CD3474679F7A4F05\"," +
        "\"data\":\"hello, world\", \"address\":\"68921F57F0C4BC2169F15F439EC9FEBE9C8387EF\", \"fee\":12, \"gas_limit\":223}";

        HttpResponse response = Request.Post("http://127.0.0.1:1337/unsafe/txpool")
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .bodyString(json, ContentType.APPLICATION_JSON)
                .execute().returnResponse();

        System.out.println(response);

    }

    public static String sendPostHttpRequestTransactMonax(String request, String key, String data, String address,
                                                          BigDecimal fee, BigDecimal gasLimit) throws IOException {
        try {
            URL url = new URL(request);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Key", key);
            if (data != null && data.length() > 0) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Length", String.valueOf(data.length()));
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                wr.close();
            }

            int responseCode = connection.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String sendMessage(String from, String to, String message) {
        return null;
    }

    @Override
    public List<Message> getNewMessages() {
        return null;
    }

    @Override
    public BlockchainBlock getBlock(long id) throws IOException {
        try {
            String block = Request.Get(url+String.format("/blockchain/blocks?q=height:%d", id)).execute().returnContent().asString();
            System.out.println(block);
            return new Gson().fromJson(block, ErisBlock.class);

        } catch (Exception e) {
            log.error("Exc", e);
        }
        return null;
    }

    @Override
    public BlockchainPeer[] getPeers() throws IOException {
        return new BlockchainPeer[0];
    }

    @Override
    public BlockchainChainInfo getChain() throws IOException {
        try {
            String chain = Request.Get(url+"/blockchain").execute().returnContent().asString();
            System.out.println(chain);
            return new Gson().fromJson(chain, ErisChainInfo.class);

        } catch (Exception e) {
            log.error("Exc", e);
        }
        return null;//JSONRPCHelper.post(url, ErisMethods.GET_BLOCKCHAIN_INFO.getMethod(), ErisChainInfo.class);
    }

    @Override
    public void authorize(String user, String password) {

    }

    public static void main(String[] args) throws IOException {
        ErisManager erisManager = new ErisManager("http://127.0.0.1:1337");
        erisManager.req();
        //System.out.println(erisManager.getChain());
    }
}
