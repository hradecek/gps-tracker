package com.hradecek.tracker.mqtt;

import io.vertx.core.Handler;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducerRecord;
import io.vertx.reactivex.mqtt.MqttEndpoint;
import io.vertx.reactivex.mqtt.MqttServer;
import io.vertx.reactivex.mqtt.messages.MqttPublishMessage;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivo Hradek <ivohradek@gmail.com>
 */
public class Broker {

    private static final Vertx vertx = Vertx.vertx();
    private static final MqttServer mqttServer = MqttServer.create(vertx);
    private static final KafkaProducer<String, String> producer = KafkaProducer.create(vertx, kafkaProducerConfig());

    public static void main(String[] args) {
        mqttServer.endpointHandler(endpointHandler(producer))
                  .rxListen()
                  .subscribe();
    }

    private static Handler<MqttEndpoint> endpointHandler(final KafkaProducer<String, String> kafkaProducer) {
        return endpoint -> {
            endpoint.publishHandler(publishMessageHandler(kafkaProducer));
            endpoint.publishReleaseHandler(endpoint::publishComplete);
            System.out.println("MQTT client" + endpoint.clientIdentifier());
            endpoint.accept(false);
        };
    }

    private static Handler<MqttPublishMessage> publishMessageHandler(final KafkaProducer<String, String> kafkaProducer) {
        return message -> {
            System.out.println("Topic: " + message.topicName());
            System.out.println("\t" + message.payload().toString());
            final KafkaProducerRecord<String, String> record =
                    KafkaProducerRecord.create("streams-plaintext-input", message.payload().toString());
            kafkaProducer.write(record);
        };
    }

    private static Map<String, String> kafkaProducerConfig() {
        return new HashMap<String, String>() {{
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
            put(ProducerConfig.ACKS_CONFIG, "1");
        }};
    }
}
