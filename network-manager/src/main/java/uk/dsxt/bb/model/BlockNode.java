package uk.dsxt.bb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class BlockNode extends NetworkAction {
    @JsonCreator
    public BlockNode(@JsonProperty int nodeIndex, @JsonProperty int startMillis, @JsonProperty int finishMillis) {
        super(startMillis, finishMillis, nodeIndex);
    }

    @Override
    public void performStart(List<String> allHosts) {
        if (getNodeIndex() > 0 ) {
            System.out.println("sudo block" + allHosts.get(getNodeIndex()));
        } else {
            System.out.println("sudo block");
        }
    }

    @Override
    public void performFinish(List<String> allHosts) {

    }
}
