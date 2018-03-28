package uk.dsxt.bb.network_manager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Collections;
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
        this.configuration = configuration == null ? Collections.emptyMap() : configuration;
        this.defaultConfiguration = defaultConfiguration == null ? new NetworkAction[0] : defaultConfiguration;
    }

    /**
     * Returns configuration of NetworkActions for node
     *
     * @param nodeIndex index of node
     * @return Array of {@link NetworkAction} for node
     */
    public NetworkAction[] getActions(int nodeIndex) {
        return configuration.getOrDefault(nodeIndex, defaultConfiguration);
    }
}
