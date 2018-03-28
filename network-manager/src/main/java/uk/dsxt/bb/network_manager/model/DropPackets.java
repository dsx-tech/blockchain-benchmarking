package uk.dsxt.bb.network_manager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class DropPackets extends NetworkAction {
    private int percentage;

    @JsonCreator
    public DropPackets(@JsonProperty("startMillis") int startMillis, @JsonProperty("finishMillis") int finishMillis,
                       @JsonProperty("percentage") int percentage) {
        super(startMillis, finishMillis);
        this.percentage = percentage;
    }

    @Override
    public void performStart(List<String> allHosts) {

    }

    @Override
    public void performFinish(List<String> allHosts) {

    }
}
