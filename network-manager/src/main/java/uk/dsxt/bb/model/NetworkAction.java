package uk.dsxt.bb.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "action")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BlockNode.class, name = "BlockNode"),
        @JsonSubTypes.Type(value = DropPackets.class, name = "DropPackets")
})
abstract class NetworkAction {
}
