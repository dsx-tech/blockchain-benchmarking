package uk.dsxt.bb.network_manager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.dsxt.bb.network_manager.NetworkManagerMain;

import java.util.List;

@Getter
public class DelayPackets extends NetworkAction {
    private final int delayMillis;

    @JsonCreator
    public DelayPackets(@JsonProperty("startMillis") int startMillis, @JsonProperty("finishMillis") int finishMillis,
                        @JsonProperty("delayMillis") int delayMillis) {
        super(startMillis, finishMillis);
        this.delayMillis = delayMillis;
    }

    @Override
    public void performStart(List<String> allHosts) {
        NetworkManagerMain.exec("sudo tc qdisc add dev eth0 root handle 1: prio");
        NetworkManagerMain.exec(String.format("sudo tc qdisc add dev eth0 parent 1:3 handle 31: netem delay %dms", delayMillis));
        for (String host : allHosts) {
            NetworkManagerMain.exec(String.format("sudo tc filter add dev eth0 protocol ip parent 1:0 prio 3 u32 match ip dst %s flowid 1:3", host));
        }

    }

    @Override
    public void performFinish(List<String> allHosts) {
        NetworkManagerMain.exec("sudo tc qdisc del dev eth0 root");
    }
}
