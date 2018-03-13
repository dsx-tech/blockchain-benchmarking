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
    public DropPackets(@JsonProperty int startMillis, @JsonProperty int finishMillis,
                       @JsonProperty int percentage, @JsonProperty int node) {
        super(startMillis, finishMillis);
        this.percentage = percentage;
        this.nodeIndex = node;
    }

    @Override
    public void performStart() {
        // TODO: 13.03.2018 real implementation: print command
    }

    @Override
    public void performFinish() {

    }
}
