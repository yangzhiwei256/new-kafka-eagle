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
package org.smartloli.kafka.eagle.web.protocol.topic;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.smartloli.kafka.eagle.web.util.DateUtils;

import java.util.Date;

/**
 * Record topic query sql.
 *
 * @author smartloli.
 *
 *         Created by Jul 27, 2019
 */
@Data
public class TopicSqlHistory {

    private int id;
    private String cluster;
    private String username;
    private String host;
    private String ksql;
    private String status;
    private long spendTime;

    @JsonFormat(pattern = DateUtils.DATA_FORMAT_YEAR_MON_DAY_HOUR_MIN_SEC, locale = "zh", timezone = "GMT+8")
    private Date created;

    private String tm;

    public String toString() {
        return JSON.toJSONString(this);
    }

}
