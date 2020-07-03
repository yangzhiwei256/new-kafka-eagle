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
package org.smartloli.kafka.eagle.web.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.web.constant.ServerDevice;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Check whether the corresponding port on the server can be accessed.
 *
 * @author smartloli.
 *
 *         Created by Mar 16, 2018
 */
public class NetUtils {

    private static final Logger LOG = LoggerFactory.getLogger(NetUtils.class);

    /** Check server host & port whether normal. */
    public static boolean telnet(String host, int port) {
        if (StringUtils.isEmpty(host) || 0 == port) {
            return false;
        }
        TelnetClient client = new TelnetClient();
        try {
            client.connect(host, port);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Ping server whether alive. */
    public static boolean ping(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(ServerDevice.TIME_OUT);
        } catch (Exception e) {
            LOG.error("Ping [" + host + "] server has crash or not exist.", e);
            return false;
        }
    }

    /** Get server ip. */
    public static String ip() {
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Get local server ip has error", e);
        }
        return ip;
    }

}
