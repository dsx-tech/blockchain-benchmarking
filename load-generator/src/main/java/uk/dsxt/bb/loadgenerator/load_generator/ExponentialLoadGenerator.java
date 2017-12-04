package uk.dsxt.bb.loadgenerator.load_generator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExponentialLoadGenerator extends LoadGenerator {
    private final double base;
    private final double powerDivider;
    private final int minDelay;

    /**
     * Generates delays to simulate exponentially increasing load.
     *
     * @param base         Base of exponent.
     * @param maxIntensity Intensity (events per second), that if been reached, won't be crossed
     * @param powerDivider Number to divide exponent (Makes load's growth more smooth)
     */
    @JsonCreator
    public ExponentialLoadGenerator(@JsonProperty("base") double base, @JsonProperty("maxIntensity") double maxIntensity,
                                    @JsonProperty("powerDivider") double powerDivider) {
        this.base = base;
        this.minDelay = (int) (1000.0d / maxIntensity);
        this.powerDivider = powerDivider;
    }

    @Override
    public int internalNextDelay() {
        return (int) Math.max(minDelay, 1000.0d / Math.pow(base, (getTimeFromStart() / (powerDivider * 1000))));
    }
}
