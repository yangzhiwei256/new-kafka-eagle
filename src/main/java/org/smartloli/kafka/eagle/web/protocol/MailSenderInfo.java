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

import java.io.File;
import java.util.*;

/**
 * Definition email sender information.
 *
 * @author smartloli.
 *
 *         Created by Aug 15, 2016
 */
@Data
public class MailSenderInfo {
    private String mailServerHost;
    private String mailServerPort = "25";
    private String fromAddress;
    private String toAddress;
    private String userName;
    private String password;
    private boolean validate = false;
    private String subject;
    private String content;
    private String[] attachFileNames;
    private List<File> fileList;
    private Map<String, String> imagesMap = new HashMap<>();

    public Properties getProperties() {
        Properties p = new Properties();
        p.put("mail.smtp.host", this.mailServerHost);
        p.put("mail.smtp.port", this.mailServerPort);
        p.put("mail.smtp.auth", validate ? "true" : "false");
        // p.setProperty("mail.smtp.starttls.enable", "true");
        return p;
    }

    @Override
    public String toString() {
        return "MailSenderInfo [mailServerHost=" + mailServerHost + ", mailServerPort=" + mailServerPort + ", fromAddress=" + fromAddress + ", toAddress=" + toAddress + ", userName=" + userName + ", password=" + password + ", validate="
                + validate + ", subject=" + subject + ", content=" + content + ", attachFileNames=" + Arrays.toString(attachFileNames) + ", fileList=" + fileList + "]";
    }

}
