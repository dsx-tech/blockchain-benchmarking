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

package uk.dsxt.bb.utils;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * @Description: Get Data From NIS
 * @author lu
 * @date 2016年8月4日
 */
public class HttpUtil {

    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_TEXT_JSON = "text/json";
    private static String nodeAddress = null;

    static {
        try{
            Configuration config = new PropertiesConfiguration("nis.properties");
            nodeAddress = config.getString("node.protocol") + "://" + config.getString("node.domain") + ":" + config.getString("node.port");
        } catch(ConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    public static String httpPostWithJSON(String path, String json){
        String result = "";
        try{
            HttpClient httpClient = HttpConnectionManager.getHttpClient();
            HttpPost httpPost = new HttpPost(nodeAddress + path);
            httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
            StringEntity se = new StringEntity(json);
            se.setContentType(CONTENT_TYPE_TEXT_JSON);
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
            httpPost.setEntity(se);
            HttpResponse response = httpClient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());
            httpPost.abort();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(" --- error --- ");
        }
        return result;
    }

    public static String httpGet(String path){
        String result = "";
        try{
            HttpClient httpClient = HttpConnectionManager.getHttpClient();
            HttpGet httpGet = new HttpGet(nodeAddress + path);
            HttpResponse response = httpClient.execute(httpGet);
            result = EntityUtils.toString(response.getEntity());
            httpGet.abort();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(" --- error --- ");
        }
        return result;
    }

    public static String httpGetByFullURL(String fullURL){
        String result = "";
        try{
            HttpClient httpClient = HttpConnectionManager.getHttpClient();
            HttpGet httpGet = new HttpGet(fullURL);
            HttpResponse response = httpClient.execute(httpGet);
            result = EntityUtils.toString(response.getEntity());
            httpGet.abort();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(" --- error --- ");
        }
        return result;
    }
}
