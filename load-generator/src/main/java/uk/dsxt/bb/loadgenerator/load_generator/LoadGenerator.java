package uk.dsxt.bb.loadgenerator.load_generator;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConstantLoadGenerator.class, name = "ConstantLoad"),
        @JsonSubTypes.Type(value = ExponentialLoadGenerator.class, name = "ExponentialLoad"),
        @JsonSubTypes.Type(value = LinearLoadGenerator.class, name = "LinearLoad"),
        @JsonSubTypes.Type(value = LinearDecreaseLoadGenerator.class, name = "LinearDecreaseLoad")
})
public abstract class LoadGenerator {
    abstract public int nextDelay();
}
