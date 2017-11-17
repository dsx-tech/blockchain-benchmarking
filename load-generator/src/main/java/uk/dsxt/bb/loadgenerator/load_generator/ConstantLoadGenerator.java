package uk.dsxt.bb.loadgenerator.load_generator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConstantLoadGenerator extends LoadGenerator {
    private final int delay;

    /**
     * Generates delays to simulate constant load.
     *
     * @param intensity Desired intensity of events per second
     */
    @JsonCreator
    public ConstantLoadGenerator(@JsonProperty("intensity") double intensity) {
        this.delay = (int) (1000.0d / intensity);
    }

    @Override
    public int nextDelay() {
        return delay;
    }
}
