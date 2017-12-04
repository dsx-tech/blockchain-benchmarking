package uk.dsxt.bb.loadgenerator.external_monitor;

import com.timgroup.statsd.NonBlockingStatsDClient;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class StatsDConnector {
    private NonBlockingStatsDClient nonBlockingStatsDClient;

    public StatsDConnector() {
        nonBlockingStatsDClient = new NonBlockingStatsDClient("", "18.195.47.142", 8125, null,
                exception -> log.error(String.format("StatsDConnector. Can't send metrics to StatsD, reason=%s", exception.getMessage()))
        );
    }

    public void count(String metricName, long delta) {
        nonBlockingStatsDClient.count(metricName, delta);
    }

    public void time(String metricName, long ms) {
        nonBlockingStatsDClient.time(metricName, ms);
    }

    public void gauge(String metricName, long value) {
        nonBlockingStatsDClient.gauge(metricName, value);
    }
}
