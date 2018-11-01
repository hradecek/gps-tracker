package com.hradecek.tracker.mqtt.producer;

import com.hradecek.tracker.mqtt.Bootstrap;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.mqtt.MqttClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Ivo Hradek <ivohradek@gmail.com>
 */
public class GpxProducer extends BaseProducer {

    private static final Random random = new Random();

    private final GPX gpx;
    private final int maxDelay;

    private GpxProducer(Vertx vertx, GPX gpx, int maxDelay) {
        super(vertx);

        this.gpx = gpx;
        this.maxDelay = maxDelay;
    }

    public static class GpxProducerBuilder {
        private final Vertx vertx;
        private final GPX gpx;
        private int maxDelay = 1_000;

        GpxProducerBuilder(Vertx vertx, String gpxFilePath) {
            this.vertx = vertx;
            try {
                this.gpx = GPX.read(gpxFile(gpxFilePath));
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot read GPX resource: " + gpxFilePath);
            }
        }

        public GpxProducerBuilder maxDelay(int maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        public GpxProducer build() {
            return new GpxProducer(vertx, gpx, maxDelay);
        }

        private static Path gpxFile(final String fileName) {
            final URL gpxUrl = Bootstrap.class.getClassLoader().getResource(fileName);
            if (null == gpxUrl) {
                throw new IllegalArgumentException("Cannot get resource: " + fileName);
            }

            try {
                return Paths.get(gpxUrl.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Connot get resource: " + fileName);
            }
        }
    }

    public static GpxProducerBuilder builder(Vertx vertx, String gpxFile) {
        return new GpxProducerBuilder(vertx, gpxFile);
    }

    @Override
    public void produce() {
        mqttClient.rxConnect(1883, "localhost")
                  .flatMapObservable(ack -> delayedPoints(parseTrackSegmentsFromGpx(gpx), maxDelay))
                  .flatMapSingle(mqttPublish(mqttClient)::apply)
                  .doOnEach(s -> System.out.println("Published: " + s.getValue()))
                  .subscribe();
    }

    private static Observable<String> delayedPoints(List<TrackSegment> segments, int maxDelay) {
        return waypointsToObservable(segments)
                .zipWith(delayObservable(maxDelay), (jsonString, sequence) -> jsonString);
    }

    private static List<TrackSegment> parseTrackSegmentsFromGpx(GPX gpx) {
        if (gpx.getTracks().size() < 1) {
            throw new IllegalArgumentException("There are no tracks in specified GPX file");
        } else if (gpx.getTracks().size() > 1) {
            System.out.println("There are more than one track in specified GPX file. Using the first one.");
        }

        final List<TrackSegment> segments = gpx.getTracks().get(0).getSegments();
        if (segments.size() < 1) {
            throw new IllegalArgumentException("There are no segments in track");
        } else if (segments.size() > 1) {
            System.out.println("There are more than one segment in track. Using the first one.");
        }

        return segments;
    }

    private static Observable<String> waypointsToObservable(List<TrackSegment> waypoints) {
        return Observable.fromIterable(waypoints.get(0))
                .map(GpxProducer::waypointToJsonString);
    }

    private static String waypointToJsonString(WayPoint wayPoint) {
        return new JsonObject().put("lat", wayPoint.getLatitude())
                .put("lon", wayPoint.getLongitude())
                .toString();
    }

    private static Observable<Long> delayObservable(int maxDelay) {
        return Observable.interval(random.nextInt(maxDelay), TimeUnit.MILLISECONDS);
    }

    private static Function<String, Single<Integer>> mqttPublish(final MqttClient mqttClient) {
        return payload -> mqttClient.rxPublish(producerUuid.toString(),
                                               Buffer.buffer(payload),
                                               MqttQoS.AT_MOST_ONCE,
                                               false,
                                               false);
    }
}
