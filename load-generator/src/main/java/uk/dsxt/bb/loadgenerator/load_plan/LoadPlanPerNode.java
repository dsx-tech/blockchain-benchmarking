package uk.dsxt.bb.loadgenerator.load_plan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.loadgenerator.load_generator.LoadGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class LoadPlanPerNode extends LoadPlan {
    private final Map<Integer, LoadGeneratorWithDuration[]> configuration;
    private final LoadGeneratorWithDuration[] defaultConfiguration;
    /**
     * Contains millis from start for each node. In general time is different for all nodes,
     * because millis are relative to start of LoadGenerator's period. So when one period of
     * load generation for node finishes, millis become 0.
     */
    private final Map<Integer, Long> millisSinceStartPerNode = new HashMap<>();
    /**
     * Stores index of LoadGenerator's period for each node.
     */
    private final Map<Integer, Integer> loadGeneratorIndexPerNode = new HashMap<>();

    /**
     * Creates custom load configuration for each node.
     *
     * @param configuration Contains mapping from nodeIndex to node's load configuration
     */
    public LoadPlanPerNode(Map<Integer, LoadGeneratorWithDuration[]> configuration) {
        this.configuration = configuration;
        defaultConfiguration = null;
    }

    /**
     * Creates custom load configuration for each node, with given default configuration.
     *
     * @param configuration        Contains mapping from nodeIndex to node's load configuration
     * @param defaultConfiguration Contains load configuration for all nodes, not presented in {@code configuration} map
     */
    @JsonCreator
    public LoadPlanPerNode(@JsonProperty("configuration") Map<Integer, LoadGeneratorWithDuration[]> configuration,
                           @JsonProperty("defaultConfiguration") LoadGeneratorWithDuration... defaultConfiguration) {
        this.configuration = configuration == null ? Collections.emptyMap() : configuration;
        this.defaultConfiguration = defaultConfiguration;
    }

    @Override
    public int nextDelay(int nodeIndex) {
        if (!configuration.containsKey(nodeIndex) && defaultConfiguration == null) {
            throw new IllegalArgumentException(
                    String.format("There is no configuration for node=%d, and no default configuration", nodeIndex));
        }
        loadGeneratorIndexPerNode.putIfAbsent(nodeIndex, 0);
        int currentLoadGeneratorIndex = loadGeneratorIndexPerNode.get(nodeIndex);
        LoadGeneratorWithDuration[] currentConfiguration = configuration.getOrDefault(nodeIndex, defaultConfiguration);
        if (currentLoadGeneratorIndex >= currentConfiguration.length) {
            log.warn(String.format("Load generation plan at node %d is finished", nodeIndex));
            return Integer.MAX_VALUE;
        }
        LoadGenerator currentLoadGenerator = currentConfiguration[currentLoadGeneratorIndex].getLoadGenerator();
        long currentDuration = currentConfiguration[currentLoadGeneratorIndex].getDurationMillis();
        millisSinceStartPerNode.putIfAbsent(nodeIndex, 0L);
        long millisSinceStart = millisSinceStartPerNode.get(nodeIndex);
        if (currentDuration != 0 && millisSinceStart > currentDuration) {
            loadGeneratorIndexPerNode.put(nodeIndex, ++currentLoadGeneratorIndex);
            millisSinceStartPerNode.put(nodeIndex, 0L);
            return nextDelay(nodeIndex);
        } else {
            int delay = currentLoadGenerator.nextDelay();
            millisSinceStartPerNode.put(nodeIndex, currentLoadGenerator.getTimeFromStart());
            return delay;
        }
    }
}
