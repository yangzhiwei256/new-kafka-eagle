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

/**
 * Kafka代理节点数据
 * @author smartloli.
 * Created by Aug 17, 2016
 */
@Data
public class KafkaBrokerInfo {

    /** 节点序号 **/
    private int id;

    /** 节点主机名 **/
    private String host;

    /** 节点端口 **/
    private int port;

    /** 节点创建时间 **/
    @JSONField(format = DateUtils.DATA_FORMAT_YEAR_MON_DAY_HOUR_MIN_SEC)
    private Date created;

    /** 节点修改时间 **/
    @JSONField(format = DateUtils.DATA_FORMAT_YEAR_MON_DAY_HOUR_MIN_SEC)
    private Date modify;

    /**
     * JMX端口
     **/
    private int jmxPort;

    /**
     * 版本号
     **/
    private String version;

    /**
     * 节点ID: /brokers/ids
     **/
    private String ids;

    /**
     * kafka是否开启JMX功能(用于指标监控、版本获取)
     */
    private Boolean jmxEnabled = false;

    /**
     * 获取Jmx缓存键值
     **/
    public String getJmxCacheKey() {
        return this.getHost() + ":" + this.getJmxPort();
    }

    public String toString() {
        return JSON.toJSONString(this);
    }
}
