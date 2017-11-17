package uk.dsxt.bb.loadgenerator.load_generator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExponentialLoadGenerator extends LoadGenerator {
    private final double base;
    private final int minDelay;
    private long totalDelay;

    /**
     * Generates delays to simulate exponentially increasing load.
     *
     * @param base         Base of exponent.
     * @param maxIntensity Intensity (events per second), that if been reached, won't be crossed
     */
    @JsonCreator
    public ExponentialLoadGenerator(@JsonProperty("base") double base, @JsonProperty("maxIntensity") double maxIntensity) {
        this.base = base;
        this.minDelay = (int) (1000.0d / maxIntensity);
    }

    @Override
    public int nextDelay() {
        final int delay = (int) Math.max(minDelay, 1000.0d / Math.pow(base, (totalDelay / 1000)));
        totalDelay += delay;
        return delay;
    }
}
