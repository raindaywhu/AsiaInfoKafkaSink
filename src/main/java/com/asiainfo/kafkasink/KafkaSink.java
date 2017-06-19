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

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A Flume Sink that can publish messages to Kafka.
 * This is a general implementation that can be used with any Flume agent and a channel.
 * This supports key and messages of type String.
 * Extension points are provided to for users to implement custom key and topic extraction
 * logic based on the message content as well as the Flume context.
 * Without implementing this extension point(MessagePreprocessor), it's possible to publish
 * messages based on a static topic. In this case messages will be published to a random
 * partition.
 */
public class KafkaSink extends AbstractSink implements Configurable {

    private static final Logger logger = LoggerFactory.getLogger(KafkaSink.class);
    private Properties producerProps;
    private Producer<String, String> producer;
    private MessagePreprocessor messagePreProcessor;
    private String topic;
    private Context context;
    private int batchSize;
    private List<KeyedMessage<String, String>> messageList;
    private SinkCounter sinkCounter;


    @Override
    public Status process() throws EventDeliveryException {
        Status result = Status.READY;
        Channel channel = getChannel();
        Transaction transaction = null;
        Event event = null;
        String eventKey = null;
        KeyedMessage<String, String> data;
        try {
            long processedEvent = 0;
            transaction = channel.getTransaction();
            transaction.begin();// 事务开始
             messageList.clear();

            for (; processedEvent < batchSize; processedEvent++) {
                event = channel.take();// 从channel取出一个事件
                if (event == null) {
                    result = Status.BACKOFF;
                    break;
                }
                sinkCounter.incrementEventDrainAttemptCount();
                String eventBody = new String(event.getBody());
                // if the metadata extractor is set, extract the topic and the key.
                if (messagePreProcessor != null) {
                    eventBody = messagePreProcessor.transformMessage(eventBody);
                    eventKey = messagePreProcessor.extractKey(event, context);
                }
                // log the event for debugging
                if (logger.isDebugEnabled()) {
                    logger.debug("{Event} " + eventBody);
                }
                if(StringUtils.isEmpty(eventKey)){
                    data = new KeyedMessage<String, String>(topic,eventBody);
                }else{
                    data = new KeyedMessage<String, String>(topic, eventKey,eventBody);
                }

                // publish
                messageList.add(data);
                logger.debug("Add data [" + eventBody
                        + "] into messageList,position:" + processedEvent);
            }

            if (processedEvent == 0) {
                sinkCounter.incrementBatchEmptyCount();
                result = Status.BACKOFF;
            } else {
                if (processedEvent < batchSize) {
                    sinkCounter.incrementBatchUnderflowCount();
                } else {
                    sinkCounter.incrementBatchCompleteCount();
                }
                sinkCounter.addToEventDrainAttemptCount(processedEvent);
                producer.send(messageList);
                logger.info("Send MessageList to Kafka: [ message List size = "
                        + messageList.size() + ",processedEvent number = "
                        + processedEvent + "] ");
            }
            transaction.commit();//batchSize个事件处理完成，一次事务提交
            sinkCounter.addToEventDrainSuccessCount(processedEvent);
            result = Status.READY;
        } catch (Exception ex) {
            transaction.rollback();
            String errorMsg = "Failed to publish event: " + event;
            logger.error(errorMsg);
            throw new EventDeliveryException(errorMsg, ex);
        } finally {
            transaction.close();
        }
        return result;
    }

    @Override
    public synchronized void start() {
        // instantiate the producer


        ProducerConfig config = new ProducerConfig(producerProps);
        sinkCounter.start();
        producer = new Producer<String, String>(config);

        //初始化topic
        topic = StringUtils.defaultIfEmpty(producerProps
                        .getProperty(com.asiainfo.kafkasink.Constants.CUSTOME_TOPIC_KEY_NAME),
                com.asiainfo.kafkasink.Constants.DEFAULT_TOPIC_NAME);

        super.start();
        sinkCounter.incrementConnectionCreatedCount();
    }

    @Override
    public synchronized void stop() {
        producer.close();
        sinkCounter.stop();
        super.stop();
        sinkCounter.incrementConnectionClosedCount();
    }


    @Override
    public void configure(Context context) {
        this.context = context;
        Map<String, String> params = context.getParameters();
        producerProps = new Properties();

        batchSize = context.getInteger("batchSize",
                com.asiainfo.kafkasink.Constants.DEFAULT_BATCH_SIZE);
        logger.info("batchSize : " + batchSize);
        messageList = new ArrayList<KeyedMessage<String, String>>(batchSize);

        for (String key : params.keySet()) {
            String value = params.get(key).trim();
            key = key.trim();
            producerProps.put(key, value);

            if (logger.isDebugEnabled()) {
                logger.debug("Reading a Kafka Producer Property: key: " + key + ", value: " + value);
            }
        }

        if (sinkCounter == null) {
            sinkCounter = new SinkCounter(getName());
        }
        // get the message Preprocessor if set
        //通过反射将配置文件中配置的 YunNan2GMessagePreprocessor 实例化
        String preprocessorClassName = context.getString(com.asiainfo.kafkasink.Constants.PREPROCESSOR);
        if (preprocessorClassName != null) {
            try {
                Class preprocessorClazz = Class.forName(preprocessorClassName.trim());
                Object preprocessorObj = preprocessorClazz.newInstance();
                if (preprocessorObj instanceof MessagePreprocessor) {
                    messagePreProcessor = (MessagePreprocessor) preprocessorObj;
                } else {
                    String errorMsg = "Provided class for MessagePreprocessor does not implement " +
                            "'com.thilinamb.flume.sink.MessagePreprocessor'";
                    logger.error(errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }
            } catch (ClassNotFoundException e) {
                String errorMsg = "Error instantiating the MessagePreprocessor implementation.";
                logger.error(errorMsg, e);
                throw new IllegalArgumentException(errorMsg, e);
            } catch (InstantiationException e) {
                String errorMsg = "Error instantiating the MessagePreprocessor implementation.";
                logger.error(errorMsg, e);
                throw new IllegalArgumentException(errorMsg, e);
            } catch (IllegalAccessException e) {
                String errorMsg = "Error instantiating the MessagePreprocessor implementation.";
                logger.error(errorMsg, e);
                throw new IllegalArgumentException(errorMsg, e);
            }
        }

        if (messagePreProcessor == null) {
            // MessagePreprocessor is not set. So read the topic from the config.
            topic = context.getString(com.asiainfo.kafkasink.Constants.TOPIC, com.asiainfo.kafkasink.Constants.DEFAULT_TOPIC);
            if (topic.equals(com.asiainfo.kafkasink.Constants.DEFAULT_TOPIC)) {
                logger.warn("The Properties 'metadata.extractor' or 'topic' is not set. Using the default topic name" +
                        com.asiainfo.kafkasink.Constants.DEFAULT_TOPIC);
            } else {
                logger.info("Using the static topic: " + topic);
            }
        }
    }
}
