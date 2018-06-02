package uk.dsxt.bb.network_manager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.dsxt.bb.network_manager.NetworkManagerMain;

import java.util.List;

@Getter
public class DropPackets extends NetworkAction {
    private final double percentage;

    @JsonCreator
    public DropPackets(@JsonProperty("startMillis") int startMillis, @JsonProperty("finishMillis") int finishMillis,
                       @JsonProperty("percentage") double percentage) {
        super(startMillis, finishMillis);
        this.percentage = percentage;
    }

    @Override
    public void performStart(List<String> allHosts) {
        // TODO: 02.06.2018 use allHosts parameter to define specific IPs
        NetworkManagerMain.exec(String.format("sudo tc qdisc change dev eth0 root netem loss %.1f%%", percentage));
    }

    @Override
    public void performFinish(List<String> allHosts) {
        NetworkManagerMain.exec("sudo tc qdisc change dev eth0 root netem loss 0%");
    }
}
