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
package org.smartloli.kafka.eagle.web.service;

import com.alibaba.fastjson.JSONObject;

/**
 * ZkService operate comand and get metadata from zookeeper interface.
 *
 * @author smartloli.
 *
 *         Created by Jan 18, 2017.
 *
 *         Update by hexiang 20170216
 */
public interface ZkService {

    /**
     * Zookeeper 删除节点
     */
    Boolean delete(String clusterAlias, String cmd);

    /**
     * Find zookeeper leader node.
     */
    String findZkLeader(String clusterAlias);

    /**
     * 获取Zookeeper节点信息
     */
    String get(String clusterAlias, String cmd);

    /**
     * 列出Zookeeper节点
     */
    String ls(String clusterAlias, String cmd);

    /**
     * 查询zookeeper健康状态
     */
    String status(String host, String port);

    /**
     * 获取Zookeeper版本
     */
    String version(String host, String port);

    /**
     * 获取 zookeeper 集群信息
     */
    String zkCluster(String clusterAlias);

    /**
     * 判断ZkCli是否活跃
     */
    JSONObject zkCliStatus(String clusterAlias);

}
