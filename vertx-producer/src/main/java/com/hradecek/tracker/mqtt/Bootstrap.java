package com.hradecek.tracker.mqtt;

import com.hradecek.tracker.mqtt.producer.ProducerVerticle;
import io.vertx.reactivex.core.Vertx;

/**
 * <p>Bootstrap class.
 *
 * <p>Contains <i>main</i> method.
 *
 * @author Ivo Hradek <ivohradek@gmail.com>
 */
public class Bootstrap {

    private static final Vertx vertx = Vertx.vertx();

    public static void main(String[] args) {
        vertx.rxDeployVerticle(ProducerVerticle.class.getCanonicalName()).subscribe();
    }
}
