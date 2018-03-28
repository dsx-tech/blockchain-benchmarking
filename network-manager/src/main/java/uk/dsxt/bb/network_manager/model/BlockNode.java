package uk.dsxt.bb.network_manager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.dsxt.bb.network_manager.Main;

import java.util.List;

@Getter
public class BlockNode extends NetworkAction {
    @JsonCreator
    public BlockNode(@JsonProperty("startMillis") int startMillis, @JsonProperty("finishMillis") int finishMillis) {
        super(startMillis, finishMillis);
    }

    // block connections to all instances
    @Override
    public void performStart(List<String> allHosts) {
        for (String host : allHosts) {
            Main.exec("sudo iptables -A INPUT -s " + host + " -j DROP");
        }
    }

    // restore connections to all instances
    @Override
    public void performFinish(List<String> allHosts) {
        for (String host : allHosts) {
            Main.exec("sudo iptables -D INPUT -s " + host + " -j DROP");
        }
    }
}
