package uk.dsxt.bb.loadgenerator.load_plan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.dsxt.bb.loadgenerator.load_generator.LoadGenerator;

@Getter
public class LoadGeneratorWithDuration {
    private final LoadGenerator loadGenerator;
    private final long durationMillis;

    /**
     * Stores {@link LoadGenerator} and {@code durationMillis}.
     *
     * @param loadGenerator  Any implementation of {@link LoadGenerator}
     * @param durationMillis Duration of LoadGeneration in millis. Pass 0 for infinity
     */
    @JsonCreator
    public LoadGeneratorWithDuration(@JsonProperty("loadGenerator") LoadGenerator loadGenerator,
                                     @JsonProperty("durationMillis") long durationMillis) {
        this.loadGenerator = loadGenerator;
        this.durationMillis = durationMillis;
    }
}
