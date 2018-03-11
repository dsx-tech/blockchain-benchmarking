package uk.dsxt.bb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class DropPackets extends NetworkAction {
    private int percentage;
    private int nodeIndex;
    private int startMillis;
    private int finishMillis;

    @JsonCreator
    public DropPackets(@JsonProperty int percentage, @JsonProperty int node,
                       @JsonProperty int startMillis, @JsonProperty int finishMillis) {
        this.percentage = percentage;
        this.nodeIndex = node;
        this.startMillis = startMillis;
        this.finishMillis = finishMillis;
    }
}
