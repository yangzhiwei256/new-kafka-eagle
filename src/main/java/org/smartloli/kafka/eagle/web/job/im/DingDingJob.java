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
import org.quartz.JobExecutionException;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.web.util.HttpClientUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Add alarm message to dingding job queue.
 * 
 * @author smartloli.
 *
 *         Created by Oct 27, 2019
 */
@Slf4j
public class DingDingJob implements Job {

	/** Send alarm information by dingding. */
	public void execute(JobExecutionContext jobContext) throws JobExecutionException {
		BaseJobContext bjc = (BaseJobContext) jobContext.getJobDetail().getJobDataMap().get(KafkaConstants.JOB_PARAMS);
		sendMsg(bjc.getData(), bjc.getUrl());
	}

	private int sendMsg(String data, String url) {
		try {
            Map<String, Object> dingDingMarkdownMessage = getDingDingMarkdownMessage(KafkaConstants.TITLE, data, true);
            String result = HttpClientUtils.doPostJson(url, JSONObject.toJSONString(dingDingMarkdownMessage));
            log.info("DingDing SendMsg Result: {}", result);
        } catch (Exception e) {
            log.error("Send alarm message has error by dingding", e);
            return 0;
        }
		return 1;
	}

	/**
	 * create markdown format map, do not point @user, option @all.
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
		at.put("isAtAll", isAtAll);
		map.put("at", at);
		return map;
	}

}
