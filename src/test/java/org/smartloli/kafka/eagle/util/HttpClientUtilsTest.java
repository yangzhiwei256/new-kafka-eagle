/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test dingding send msg.
 *
 * @author smartloli.
 * <p>
 * Created by Jan 1, 2019
 */
@Slf4j
public class HttpClientUtilsTest {

    @Value("kafka.eagle.im.dingding.url")
    private String dingdingUrl;

    private HttpPost createHttpPost(Map<String, Object> dingDingMarkdownMessage) {
        if (dingdingUrl == null || dingdingUrl.trim().isEmpty()) {
            return null;
        }
        HttpPost httpPost = new HttpPost(dingdingUrl);
        httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
        StringEntity sEntity = new StringEntity(JSONObject.toJSONString(dingDingMarkdownMessage), "utf-8");
        httpPost.setEntity(sEntity);
        return httpPost;
    }

    /**
     * send single user.
     *
     * @param mobiles
     * @param text
     */
    public void sendMarkdownToDingDing(String title, String text, List<String> mobiles) {
        Map<String, Object> dingDingMarkdownMessage = getDingDingMarkdownMessage(title, text, mobiles);
        HttpPost httpPost = createHttpPost(dingDingMarkdownMessage);
        if (httpPost == null) {
            return;
        }
        log.info("send mark down message to ding ding. title:{}, mobiles:{}, text:{}", title, mobiles.toString(), text);
        executeAndGetResponse(httpPost);
    }

    /**
     * send group.
     */
    public void sendMarkdownToDingDing(String title, String text) {
        Map<String, Object> dingDingMarkdownMessage = getDingDingMarkdownMessage(title, text, true);
        HttpPost httpPost = createHttpPost(dingDingMarkdownMessage);
        if (httpPost == null) {
            log.error("|resp error|title:{} text:{}|ding ding robot token is null. ", title, text);
            return;
        }
        log.info("send mark down message to ding ding. title:{}, text:{}", title, text);
        executeAndGetResponse(httpPost);
    }

    /**
     * send & get response result.
     * @param httpPost
     */
    private static void executeAndGetResponse(HttpPost httpPost) {
        CloseableHttpClient httpClient = HttpClients.custom().build();
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity(), "utf-8");
            log.info("dingding server result:" + result);
            httpClient.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * create markdown format map, do not point @user, option @all.
     *
     * @param title
     * @param text
     * @param isAtAll
     */
    private static Map<String, Object> getDingDingMarkdownMessage(String title, String text, boolean isAtAll) {
        Map<String, Object> map = new HashMap<>();
        map.put("msgtype", "markdown");

        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", title);
        markdown.put("text", text);
        map.put("markdown", markdown);

        Map<String, Object> at = new HashMap<>();
        at.put("isAtAll", false);
        map.put("at", at);
        return map;
    }

    /**
     * create markdown format map, point into @users
     *
     * @param title
     * @param text
     * @param atMobiles
     */
    private static Map<String, Object> getDingDingMarkdownMessage(String title, String text, List<String> atMobiles) {
        Map<String, Object> map = new HashMap<>();
        map.put("msgtype", "markdown");

        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", title);
        markdown.put("text", text);
        map.put("markdown", markdown);

        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", atMobiles);
        at.put("isAtAll", false);
        map.put("at", at);
        return map;
    }
}
