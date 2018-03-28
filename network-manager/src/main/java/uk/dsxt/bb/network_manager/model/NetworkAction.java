package uk.dsxt.bb.network_manager.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "action")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BlockNode.class, name = "BlockNode"),
        @JsonSubTypes.Type(value = DropPackets.class, name = "DropPackets")
})
public abstract class NetworkAction {
    private int startMillis;
    private int finishMillis;

    public abstract void performStart(List<String> allHosts);

    public abstract void performFinish(List<String> allHosts);
}
