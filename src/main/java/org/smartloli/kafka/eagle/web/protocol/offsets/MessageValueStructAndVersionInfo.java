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
package org.smartloli.kafka.eagle.web.protocol.offsets;

import org.apache.kafka.common.protocol.types.Struct;


/**
 * Topic __consumer_offsets consumer message include struct & version data.
 *
 * @author smartloli.
 *
 *         Created by Jan 3, 2017
 */
public class MessageValueStructAndVersionInfo  {

    private Struct value;
    private Short version;

    public Struct getValue() {
        return value;
    }

    public void setValue(Struct value) {
        this.value = value;
    }

    public Short getVersion() {
        return version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }

}
