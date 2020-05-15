/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.job.im;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.web.util.HttpClientUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Add alarm message to wechat job queue.
 * 
 * @author smartloli.
 * Created by Oct 27, 2019
 */
@Slf4j
public class WeChatJob implements Job {

    /**
     * Send alarm information by wechat.
     */
    public void execute(JobExecutionContext jobContext) {
        BaseJobContext bjc = (BaseJobContext) jobContext.getJobDetail().getJobDataMap().get(KafkaConstants.JOB_PARAMS);
        try {
            Map<String, Object> wechatMarkdownMessage = new HashMap<>();
            wechatMarkdownMessage.put("msgtype", "markdown");

            Map<String, Object> markdown = new HashMap<>();
            markdown.put("content", bjc.getData());
            wechatMarkdownMessage.put("markdown", markdown);

            wechatMarkdownMessage.put("touser", KafkaConstants.TOUSER);
            wechatMarkdownMessage.put("toparty", KafkaConstants.TOPARTY);
            wechatMarkdownMessage.put("totag", KafkaConstants.TOTAG);
            wechatMarkdownMessage.put("agentid", KafkaConstants.AGENTID);

            String result = HttpClientUtils.doPostJson(bjc.getUrl(), JSONObject.toJSONString(wechatMarkdownMessage));
            log.info("DingDing SendMsg Result: {}", result);
        } catch (Exception e) {
            log.error("Send alarm message has error by wechat", e);
        }
    }
}
