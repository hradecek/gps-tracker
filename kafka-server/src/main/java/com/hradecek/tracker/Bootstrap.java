package com.hradecek.tracker;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

/**
 * @author Ivo Hradek <ivohradek@gmail.com>
 */
public class Bootstrap {

    public static void main(String[] args) {
        final KafkaStreams streams = new KafkaStreams(createTopology(), kafkaConfig());
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static Topology createTopology() {
        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream("streams-plaintext-input").to("streams-output");

        return builder.build();
    }

    private static Properties kafkaConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "tracker-kafka-server");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        return props;
    }
}
