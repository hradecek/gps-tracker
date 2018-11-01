package com.hradecek.tracker.mqtt.producer;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.mqtt.MqttClient;

import java.util.UUID;

/**
 * @author Ivo Hradek <ivohradek@gmail.com>
 */
abstract class BaseProducer implements Producer {

    protected static final UUID producerUuid = UUID.randomUUID();
    protected final MqttClient mqttClient;

    public BaseProducer(Vertx vertx) {
        this.mqttClient = MqttClient.create(vertx);
    }
}
