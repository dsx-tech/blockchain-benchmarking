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

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

@Value
@AllArgsConstructor
@Log4j2
public class HttpHelper {
    int connectionTimeout;
    int readTimeout;

    public String request(String urlString, RequestType type) throws IOException {
        return request(urlString, (String) null, type);
    }

    public String request(String urlString, String content, RequestType type) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = null;
        InputStream is = null;
        StringBuilder response = new StringBuilder();
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(type.toString());
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);

            if (content != null && type == RequestType.POST) {
                connection.setRequestProperty("Content-type", "application/json");
                connection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(content);
                wr.flush();
                wr.close();
            }

            int code = connection.getResponseCode();
            //        if (code != Response.Status.OK.getStatusCode() && code != Response.Status.NO_CONTENT.getStatusCode())
            //            throw new InternalLogicException(String.format("request failed. code %s for url %s", code, urlString));

            try (BufferedReader in = new BufferedReader(new InputStreamReader(is = connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
        } catch (Exception e) {
            log.error("HttpHelper exception:" + e.getMessage() + ". url=" + urlString + ", content=" + content);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("HttpHelper, exception during closing input stream", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response.toString();
    }

    public String request(String urlString, Map<String, String> parameters, RequestType type) throws IOException {
        return request(urlString, buildContent(parameters), type);
    }

    private String buildContent(Map<String, String> parameters) throws UnsupportedEncodingException {
        if (parameters == null)
            return "";

        StringBuilder paramString = new StringBuilder();
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            if (param.getValue() == null)
                continue;
            if (paramString.length() > 0)
                paramString.append('&');
            paramString.append(param.getKey());
            paramString.append('=');
            paramString.append(URLEncoder.encode(param.getValue(), "UTF-8"));
        }
        return paramString.toString();
    }
}

