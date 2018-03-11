package uk.dsxt.bb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BlockNode extends NetworkAction {
    private int nodeIndex;
    private int startMillis;
    private int finishMillis;

    @JsonCreator
    public BlockNode(@JsonProperty int nodeIndex, @JsonProperty int startMillis, @JsonProperty int finishMillis) {
        this.nodeIndex = nodeIndex;
        this.startMillis = startMillis;
        this.finishMillis = finishMillis;
    }
}
