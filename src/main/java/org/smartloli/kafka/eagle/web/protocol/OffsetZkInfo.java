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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.smartloli.kafka.eagle.web.util.DateUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Kafka消费者偏移量offset
 * @author smartloli.
 * Created by Aug 16, 2016
 */
@Data
public class OffsetZkInfo {

    /** 偏移量 **/
    private long offset = -1L;

    /** 创建日期 **/
    @JSONField(format = DateUtils.DATA_FORMAT_YEAR_MON_DAY_HOUR_MIN_SEC)
    private Date create;

    /** 修改日期 **/
    @JSONField(format = DateUtils.DATA_FORMAT_YEAR_MON_DAY_HOUR_MIN_SEC)
    private Date modify;

    /** 拥有者 **/
    private String owners;

    /** 分区号 **/
    private Set<Integer> partitions = new HashSet<>();

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
