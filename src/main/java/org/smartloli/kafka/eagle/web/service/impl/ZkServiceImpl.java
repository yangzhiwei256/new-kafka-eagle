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
package org.smartloli.kafka.eagle.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kafka.zk.KafkaZkClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.data.Stat;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.service.ZkService;
import org.smartloli.kafka.eagle.web.support.KafkaZkClientTemplate;
import org.smartloli.kafka.eagle.web.support.OperationCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Implements ZkService all method.
 * 
 * @author smartloli.
 * Created by Jan 18, 2017.
 * Update by hexiang 20170216
 *
 * @see ZkService
 */
@Service
@Slf4j
public class ZkServiceImpl implements ZkService {

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;
    @Autowired
    private KafkaZkClientTemplate kafkaZkClientTemplate;

    /**
     * KafkaConstants delete command.
     */
    @Override
    public Boolean delete(String clusterAlias, String cmd) {
        return kafkaZkClientTemplate.doExecute(clusterAlias, new OperationCallback<KafkaZkClient, Boolean>() {
            @Override
            public Boolean execute(KafkaZkClient kafkaZkClient) {
                if (kafkaZkClient.pathExists(cmd)) {
                    if (kafkaZkClient.deleteRecursive(cmd)) {
                        log.info("[" + cmd + "] has delete success");
                        return true;
                    } else {
                        log.info("[" + cmd + "] has delete failed");
                        return false;
                    }
                }
                return false;
            }
        });
    }

    /**
     * KafkaConstants get command.
     */
    @Override
    public String get(String clusterAlias, String cmd) {

        return kafkaZkClientTemplate.doExecute(clusterAlias, kafkaZkClient -> {
            String ret = "";
            if (kafkaZkClient.pathExists(cmd)) {
                Tuple2<Option<byte[]>, Stat> tuple2 = kafkaZkClient.getDataAndStat(cmd);
                ret += new String(tuple2._1.get()) + "\n";
                ret += "cZxid = " + tuple2._2.getCzxid() + "\n";
                ret += "ctime = " + tuple2._2.getCtime() + "\n";
                ret += "mZxid = " + tuple2._2.getMzxid() + "\n";
                ret += "mtime = " + tuple2._2.getMtime() + "\n";
                ret += "pZxid = " + tuple2._2.getPzxid() + "\n";
                ret += "cversion = " + tuple2._2.getCversion() + "\n";
                ret += "dataVersion = " + tuple2._2.getVersion() + "\n";
                ret += "aclVersion = " + tuple2._2.getAversion() + "\n";
                ret += "ephemeralOwner = " + tuple2._2.getEphemeralOwner() + "\n";
                ret += "dataLength = " + tuple2._2.getDataLength() + "\n";
                ret += "numChildren = " + tuple2._2.getNumChildren() + "\n";
            }
            return ret;
        });
    }

    /**
     * KafkaConstants ls command.
     */
    @Override
    public String ls(String clusterAlias, String cmd) {
        return kafkaZkClientTemplate.doExecute(clusterAlias, kafkaZkClient -> {
            String target = "";
            if (kafkaZkClient.pathExists(cmd)) {
                Seq<String> seq = kafkaZkClient.getChildren(cmd);
                target = JavaConversions.seqAsJavaList(seq).toString();
            }
            return target;
        });
    }

    /**
     * Get zookeeper health status.
     *
     * @param host KafkaConstants host
     * @param port KafkaConstants port
     * @return String.
     */
    @Override
    public String status(String host, String port) {
        String target = "";
        Socket sock = null;
        try {
            String tmp = "";
            if (port.contains("/")) {
                tmp = port.split("/")[0];
            } else {
				tmp = port;
			}
			sock = new Socket(host, Integer.parseInt(tmp));
		} catch (Exception e) {
            log.error("Socket[" + host + ":" + port + "] connect refused");
            return "death";
		}
		BufferedReader reader = null;
		OutputStream outstream = null;
		try {
			outstream = sock.getOutputStream();
			outstream.write("stat".getBytes());
			outstream.flush();
			sock.shutdownOutput();

			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Mode: ")) {
                    target = line.replaceAll("Mode: ", "").trim();
                }
            }
        } catch (Exception ex) {
            log.error("Read Kafka:[{}:{}] buffer has error", host, port, ex);
            return "death";
		} finally {
			try {
				sock.close();
				if (reader != null) {
                    reader.close();
                }
                if (outstream != null) {
                    outstream.close();
                }
            } catch (Exception ex) {
                log.error("Close read has error", ex);
            }
        }
        return target;
    }

    @Override
    public String version(String host, String port) {
        String target = "";
        Socket sock = null;
        try {
            String tmp = "";
            if (port.contains("/")) {
                tmp = port.split("/")[0];
            } else {
                tmp = port;
            }
            sock = new Socket(host, Integer.parseInt(tmp));
        } catch (Exception e) {
            log.error("Socket[" + host + ":" + port + "] connect refused");
            return "unkown";
        }
        BufferedReader reader = null;
        OutputStream outstream = null;
        try {
            outstream = sock.getOutputStream();
            outstream.write("stat".getBytes());
            outstream.flush();
            sock.shutdownOutput();

            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("version: ")) {
                    target = line.split("version: ")[1].split("-")[0];
                }
            }
        } catch (Exception ex) {
            log.error("Read KafkaConstants buffer has error,msg is " + ex.getMessage());
            return "unkown";
        } finally {
            try {
                sock.close();
                if (reader != null) {
                    reader.close();
                }
                if (outstream != null) {
                    outstream.close();
                }
            } catch (Exception ex) {
                log.error("Close read has error,msg is " + ex.getMessage());
            }
        }
        return target;
    }

    /**
     * Get zookeeper cluster information.
     */
    @Override
    public String zkCluster(String clusterAlias) {
        JSONArray targets = new JSONArray();
        int id = 1;
        for (String zk : kafkaClustersConfig.getClusterConfigByName(clusterAlias).getZkList().split(",")) {
            JSONObject object = new JSONObject();
            object.put("id", id++);
            object.put("host", zk.split(":")[0]);
            object.put("port", zk.split(":")[1].split("/")[0]);
            object.put("mode", status(zk.split(":")[0], zk.split(":")[1]));
            object.put("version", version(zk.split(":")[0], zk.split(":")[1]));
            targets.add(object);
        }
        return targets.toJSONString();
    }

    @Override
    public JSONObject zkCliStatus(String clusterAlias) {
        return kafkaZkClientTemplate.doExecute(clusterAlias, new OperationCallback<KafkaZkClient, JSONObject>() {
            @Override
            public JSONObject execute(KafkaZkClient kafkaZkClient) {
                JSONObject target = new JSONObject();
                if (kafkaZkClient != null) {
                    target.put("live", true);
                    target.put("list", kafkaClustersConfig.getClusterConfigByName(clusterAlias).getZkList());
                } else {
                    target.put("live", false);
                    target.put("list", kafkaClustersConfig.getClusterConfigByName(clusterAlias).getZkList());
                }
                return target;
            }
        });
	}

	/** Find zookeeper leader node && reback. */
	public String findZkLeader(String clusterAlias) {
        String address = "";
        for (String zk : kafkaClustersConfig.getClusterConfigByName(clusterAlias).getZkList().split(",")) {
            String status = status(zk.split(":")[0], zk.split(":")[1]);
            if (KafkaConstants.LEADER.equals(status)) {
                address = zk;
                break;
            }
        }
        return address;
	}

}
