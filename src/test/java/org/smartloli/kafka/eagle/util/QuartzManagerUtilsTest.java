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

import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.job.im.DingDingJob;
import org.smartloli.kafka.eagle.web.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.web.util.QuartzManagerUtils;

import java.util.Date;

/**
 * TODO
 *
 * @author smartloli.
 *
 *         Created by Oct 25, 2019
 */
@Slf4j
public class QuartzManagerUtilsTest {

    public static void main(String[] args) {
        log.info("Send msg, date : [" + new Date().toString() + "]");
        String jobName = "ke_job_id_" + new Date().getTime();
        String jobName2 = "ke_job2_id_" + new Date().getTime();
        BaseJobContext bjc = new BaseJobContext();
        bjc.setData("test");
        bjc.setUrl("http://www.kafka-eagle.org");
        QuartzManagerUtils.addJob(bjc, jobName, DingDingJob.class, QuartzManagerUtils.getCron(new Date(), 5));
        QuartzManagerUtils.addJob(bjc, jobName2, DingDingJob.class, QuartzManagerUtils.getCron(new Date(), 10));
    }

}
