package uk.dsxt.bb.loadgenerator.load_plan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.dsxt.bb.loadgenerator.load_generator.LoadGenerator;

import java.util.Arrays;

public class SameLoadPlan extends LoadPlan {
    private final LoadGeneratorWithDuration[] configuration;
    private long millisSinceStart;
    private int loadGeneratorIndex;

    @JsonCreator
    public SameLoadPlan(@JsonProperty("loadGeneratorWithDuration") LoadGeneratorWithDuration... loadGeneratorWithDuration) {
        if (loadGeneratorWithDuration == null || loadGeneratorWithDuration.length == 0) {
            throw new IllegalArgumentException("Can't construct SameLoadPlan with empty settings");
        }
        configuration = Arrays.copyOf(loadGeneratorWithDuration, loadGeneratorWithDuration.length);
    }

    @Override
    public int nextDelay(int _nodeIndex) {
        // Don't use nodeIndex in this implementation, load generation is the same for all nodes

        if (loadGeneratorIndex >= configuration.length) {
            throw new RuntimeException("Load generation plan is finished");
        }
        LoadGenerator currentLoadGenerator = configuration[loadGeneratorIndex].getLoadGenerator();
        long currentDuration = configuration[loadGeneratorIndex].getDurationMillis();
        if (currentDuration != 0 && millisSinceStart > currentDuration) {
            loadGeneratorIndex++;
            millisSinceStart = 0;
            return nextDelay(_nodeIndex);
        } else {
            int delay = currentLoadGenerator.nextDelay();
            millisSinceStart += delay;
            return delay;
        }
    }
}
