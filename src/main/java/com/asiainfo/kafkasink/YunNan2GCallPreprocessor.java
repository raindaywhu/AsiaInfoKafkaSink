/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * limitations under the License.
 */

package com.asiainfo.kafkasink;

import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 2G呼叫信令（ga_cc_bdr）转换为4G S1-MME格式
 * This is an example of a <code>MessagePreprocessor</code> implementation.
 */
public class YunNan2GCallPreprocessor implements MessagePreprocessor {
    private static final Logger logger = LoggerFactory.getLogger(KafkaSink.class);

    /**
     * extract the hour of the time stamp as the key. So the data is partitioned
     * per hour.
     * @param event This is the Flume event that will be sent to Kafka
     * @param context The Flume runtime context.
     * @return Hour of the timestamp
     */
    @Override
    public String extractKey(Event event, Context context) {
        // get timestamp header if it's present.
        String key = event.getHeaders().get("key");
        return key;
    }

    /**
     * A custom property is read from the Flume config.
     * @param event This is the Flume event that will be sent to Kafka
     * @param context The Flume runtime context.
     * @return topic provided as a custom property
     */
    @Override
    public String extractTopic(Event event, Context context) {
        return context.getString("topic", "default-topic");

        //by chenrui 待测试
        //return context.getString("topic", "default-topic");
    }

    /**
     * 2G呼叫信令（ga_cc_bdr）原格式：start_time,call_type,calling,called,imsi,imei,tmsi_o,start_lac,start_ci,end_lac,end_ci
     *
     * Trying to prepend each message with the timestamp.
     * @return modified message of the form: timestamp + ":" + original message body
     */
    @Override
    public String transformMessage(String messageBody) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(messageBody)) {
            String[] msg = messageBody.split(",", -1);
            //logger.info("Length:"+msg.length);
            if (msg.length >= 11 && msg[4].length() > 10) {
                String mmtype=msg[1];
                String PROCEDURE_TYPE="";//").append().append("
                if("0".equals(mmtype)){
                    PROCEDURE_TYPE="2001";
                }else if("1".equals(mmtype)){
                    PROCEDURE_TYPE="2002";
                }
                String called=msg[3];
                String imsi=msg[4];
                String imei=msg[5];
                String msisdn=msg[2];
                String procedure_start_time=msg[0];
                String tmsi=msg[6];
                String lac=msg[7];
                String cell=msg[8];
                String end_lac=msg[9];
                String end_ci=msg[10];

                //0-10  called,imsi,imei,MSISDN,PROCEDURE_TYPE,PROCEDURE_START_TIME
                sb.append("22||||").append(called).append("||").append(imsi).append("|").append(imei).append("|").append(msisdn).append("|").append(PROCEDURE_TYPE).append("|").append(procedure_start_time);
                //11-32,TMSI
                sb.append("||||||||||||||||").append(tmsi).append("|||||||");
                //LAC,CI,end_lac,end_ci
                sb.append(lac).append("|").append(cell).append("|").append(end_lac).append("|").append(end_ci).append("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
                return sb.toString();
            } else {
                logger.warn("dropped error message " + messageBody);
                return sb.toString();
            }
        } else {
            return messageBody;
        }
    }

}
