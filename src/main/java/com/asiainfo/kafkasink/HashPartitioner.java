package com.asiainfo.kafkasink;

import kafka.producer.Partitioner;

public class HashPartitioner implements Partitioner {
    @Override
    public int partition(Object key, int numPartitions) {
        int partition;
        String _key = (String)key;
        partition = Math.abs(_key.hashCode()) % numPartitions;
        return partition;
    }
}
