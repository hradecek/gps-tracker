package com.hradecek.tracker.mqtt.producer;

import io.vertx.reactivex.core.AbstractVerticle;

/**
 * @author Ivo Hradek <ivohradek@gmail.com>
 */
public class ProducerVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        GpxProducer.builder(vertx, "sk_kosice_bratislava_1.gpx").build().produce();
    }
}
