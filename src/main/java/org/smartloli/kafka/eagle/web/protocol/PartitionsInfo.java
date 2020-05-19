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
package org.smartloli.kafka.eagle.web.protocol;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.smartloli.kafka.eagle.web.util.DateUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Kafka主题分区信息
 * @author smartloli.
 * Created by Mar 30, 2016
 */
@Data
public class PartitionsInfo{

    /** 分区ID **/
    private int id = 0;

    /** 主题名称 **/
    private String topic;

    /** 分区列表 **/
    private Set<String> partitions = new HashSet<String>();

    /** 分区数量 **/
    private int partitionNumbers = 0;

    /** Broker倾斜数 **/
    private long brokersSkewed;

    /** Broker 使用率 **/
    private long brokersSpread;

    /**
     * 主分区是否倾斜
     */
    private long brokersLeaderSkewed;

    /**
     * 创建时间
     */
    @JSONField(format = DateUtils.DATA_FORMAT_YEAR_MON_DAY_HOUR_MIN_SEC)
    private Date created;

    /**
     * 更新时间
     */
    @JSONField(format = DateUtils.DATA_FORMAT_YEAR_MON_DAY_HOUR_MIN_SEC)
    private Date modify;
}
