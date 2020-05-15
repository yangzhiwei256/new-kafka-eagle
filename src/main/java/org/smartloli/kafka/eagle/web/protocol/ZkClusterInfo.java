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
import lombok.Data;

/**
 * KafkaConstants cluster information.
 *
 * @author smartloli.
 *
 *         Created by Feb 5, 2018
 */
@Data
public class ZkClusterInfo {

    private String zkPacketsReceived;// received client packet numbers
    private String zkPacketsSent;// send client packet numbers
    private String zkAvgLatency;// response client request avg time
    private String zkNumAliveConnections;// has connected client numbers
    /** waiting deal with client request numbers in queue. */
    private String zkOutstandingRequests = "";
    /** server mode,like standalone|cluster[leader,follower]. */
    private String zkOpenFileDescriptorCount;
    private String zkMaxFileDescriptorCount;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
