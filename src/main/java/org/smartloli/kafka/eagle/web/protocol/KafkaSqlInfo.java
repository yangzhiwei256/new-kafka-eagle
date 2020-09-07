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

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * KSQL 封装类
 *
 * @author smartloli.
 * @Created by Feb 28, 2017
 */
@Data
public class KafkaSqlInfo {

    /**
     * 主题名称
     **/
    private String topic;

    /**
     * 主题分区号
     **/
    private List<Integer> partition = new ArrayList<>();

    /**
     * 原始KSQL
     **/
    private String sql;

    /**
     * 限制访问数量
     **/
    private Integer limit;

    /**
     * 查询记录字段信息：例如 字段名称、字段类型
     **/
    private JSONObject schema = new JSONObject();

    /**
     * 判断KSQL是否有效
     **/
    private boolean valid;

    /**
     * kafka代理节点信息
     **/
    private List<HostsInfo> brokerInfos = new ArrayList<>();

    /**
     * kafka集群名称
     **/
    private String clusterAlias;
}
