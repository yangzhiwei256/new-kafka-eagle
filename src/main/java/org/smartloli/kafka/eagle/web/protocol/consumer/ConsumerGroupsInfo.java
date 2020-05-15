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
package org.smartloli.kafka.eagle.web.protocol.consumer;



/**
 * Stats consumer groups and topic.
 *
 * @author smartloli.
 *
 *         Created by Mar 17, 2020
 */
public class ConsumerGroupsInfo  {
    private String cluster;
    private String group;
    private String topic;
    private String coordinator;
    private String activeTopic;
    private String activeThread;

    public String getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(String coordinator) {
        this.coordinator = coordinator;
    }

    public String getActiveTopic() {
        return activeTopic;
    }

    public void setActiveTopic(String activeTopic) {
        this.activeTopic = activeTopic;
    }

    public String getActiveThread() {
        return activeThread;
    }

    public void setActiveThread(String activeThread) {
        this.activeThread = activeThread;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
