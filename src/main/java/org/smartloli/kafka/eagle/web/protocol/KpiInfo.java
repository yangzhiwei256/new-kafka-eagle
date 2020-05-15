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

import lombok.Data;

/**
 * kafka 指标信息
 *
 * @author smartloli.
 * Created by Jul 19, 2017
 */
@Data
public class KpiInfo {

    /**
     * kafka集群名称
     **/
    private String cluster;

    /**
     * 数据类型：zookeeper/kafka
     **/
    private String type;

    /**
     * 代理服务器信息
     **/
    private String broker;

    /**
     * 指标说明
     **/
    private String key;

    /**
     * 指标值
     **/
    private String value;

    /**
     * 数据收集日期
     **/
    private String tm;

    /**
     * 数据采集时间
     **/
    private long timespan;
}
