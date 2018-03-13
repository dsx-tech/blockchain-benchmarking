package uk.dsxt.bb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BlockNode extends NetworkAction {
    private int nodeIndex;

    @JsonCreator
    public BlockNode(@JsonProperty int nodeIndex, @JsonProperty int startMillis, @JsonProperty int finishMillis) {
        super(startMillis, finishMillis);
        this.nodeIndex = nodeIndex;
    }

    @Override
    public void performStart() {
        // TODO: 13.03.2018 real implementation: print command
    }

    @Override
    public void performFinish() {
    }
}
