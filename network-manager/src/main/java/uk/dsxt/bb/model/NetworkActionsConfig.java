package uk.dsxt.bb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class NetworkActionsConfig {
    private final Map<Integer, NetworkAction[]> configuration;
    private final NetworkAction[] defaultConfiguration;

    /**
     * Creates custom network actions configuration for each node, with given default configuration.
     *
     * @param configuration        Contains mapping from nodeIndex to node's network actions sequence
     * @param defaultConfiguration Contains network actions configuration for all nodes, not presented in {@code configuration} map
     */
    @JsonCreator
    public NetworkActionsConfig(@JsonProperty("configuration") Map<Integer, NetworkAction[]> configuration,
                                @JsonProperty("defaultConfiguration") NetworkAction... defaultConfiguration) {
        this.configuration = configuration;
        this.defaultConfiguration = defaultConfiguration;
    }
}
