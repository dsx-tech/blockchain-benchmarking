package uk.dsxt.bb.loadgenerator.load_generator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LinearLoadGenerator extends LoadGenerator {
    private final double intensityChangePerSecond;
    private final double startIntensity;
    private final int targetDelay;
    private boolean useTargetDelay;

    /**
     * Generates delays to simulate linearly increasing load.
     *
     * @param startIntensity           Intensity (events per second) to start from
     * @param targetIntensity          Intensity (events per second), that if been reached, won't be crossed
     * @param intensityChangePerSecond Change of intensity per second, should be positive.
     */
    @JsonCreator
    public LinearLoadGenerator(@JsonProperty("startIntensity") double startIntensity,
                               @JsonProperty("targetIntensity") double targetIntensity,
                               @JsonProperty("intensityChangePerSecond") double intensityChangePerSecond) {
        if (intensityChangePerSecond < 0.0d) {
            log.error("Change of intensity per second should be positive in LinearLoadGenerator");
            throw new IllegalArgumentException("Change of intensity per second should be positive");
        }
        this.startIntensity = startIntensity;
        this.targetDelay = (int) (1000.0d / targetIntensity);
        this.intensityChangePerSecond = intensityChangePerSecond;
    }

    @Override
    public int internalNextDelay() {
        final int delay;
        if (useTargetDelay) {
            delay = targetDelay;
        } else {
            delay = (int) (1000.0d / (startIntensity + intensityChangePerSecond * (getTimeFromStart() / 1000)));
            if (delay <= targetDelay) {
                useTargetDelay = true;
            }
        }
        return delay;
    }
}
