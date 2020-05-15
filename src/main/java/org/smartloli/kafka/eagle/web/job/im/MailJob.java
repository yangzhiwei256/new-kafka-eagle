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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.message.BasicNameValuePair;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.web.util.HttpClientUtils;

import java.util.Arrays;

/**
 * Add alarm message to wechat job queue.
 * 
 * @author smartloli.
 *
 *         Created by Oct 27, 2019
 */
@Slf4j
public class MailJob implements Job {

    /**
     * Send alarm information by mail.
     */
    public void execute(JobExecutionContext jobContext) {
        BaseJobContext bjc = (BaseJobContext) jobContext.getJobDetail().getJobDataMap().get(KafkaConstants.JOB_PARAMS);
        try {
            JSONObject object = JSON.parseObject(bjc.getData());
            BasicNameValuePair address = new BasicNameValuePair("address", object.getString("address"));
            BasicNameValuePair msg = new BasicNameValuePair("msg", object.getString("msg"));
            HttpClientUtils.doPostForm(bjc.getUrl(), Arrays.asList(address, msg));
        } catch (Exception e) {
            log.error("Send alarm message has error by mail", e);
        }
    }

}
