package uk.dsxt.bb.loadgenerator.load_generator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LinearDecreaseLoadGenerator extends LoadGenerator {
    private final double intensityChangePerSecond;
    private final double startIntensity;
    private final int targetDelay;
    private long totalDelay;
    private boolean useTargetDelay;

    /**
     * Generates delays to simulate linearly decreasing load.
     *
     * @param startIntensity           Intensity (events per second) to start from
     * @param targetIntensity          Intensity (events per second), that if been reached, won't be crossed
     * @param intensityChangePerSecond Change of intensity per second, should be negative.
     */
    @JsonCreator
    public LinearDecreaseLoadGenerator(@JsonProperty("startIntensity") double startIntensity,
                                       @JsonProperty("targetIntensity") double targetIntensity,
                                       @JsonProperty("intensityChangePerSecond") double intensityChangePerSecond) {
        if (intensityChangePerSecond > 0.0d) {
            log.error("Change of intensity per second should be negative in LinearDecreaseLoadGenerator");
            throw new IllegalArgumentException("Change of intensity per second should be negative");
        }
        this.startIntensity = startIntensity;
        this.targetDelay = (int) (1000.0d / targetIntensity);
        this.intensityChangePerSecond = intensityChangePerSecond;
    }

    @Override
    public int nextDelay() {
        final int delay;
        if (useTargetDelay) {
            delay = targetDelay;
        } else {
            final int functionDelay = (int) (1000.0d / (startIntensity + intensityChangePerSecond * (totalDelay / 1000)));
            if (functionDelay >= targetDelay || functionDelay < 0) {
                useTargetDelay = true;
            }
            delay = useTargetDelay ? targetDelay : functionDelay;
        }
        totalDelay += delay;
        return delay;
    }
}
