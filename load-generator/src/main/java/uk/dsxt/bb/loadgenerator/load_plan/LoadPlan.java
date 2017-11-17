package uk.dsxt.bb.loadgenerator.load_plan;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LoadPlanPerNode.class, name = "LoadPlanPerNode"),
        @JsonSubTypes.Type(value = SameLoadPlan.class, name = "SameLoadPlan")
})
public abstract class LoadPlan {
    public abstract int nextDelay(int nodeIndex);
}
