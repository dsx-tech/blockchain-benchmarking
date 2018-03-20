package uk.dsxt.bb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class DropPackets extends NetworkAction {
    private int percentage;

    @JsonCreator
    public DropPackets(@JsonProperty int startMillis, @JsonProperty int finishMillis,
                       @JsonProperty int nodeIndex, @JsonProperty int percentage) {
        super(startMillis, finishMillis, nodeIndex);
        this.percentage = percentage;
    }

    @Override
    public void performStart(List<String> allHosts) {

    }

    @Override
    public void performFinish(List<String> allHosts) {

    }
}
