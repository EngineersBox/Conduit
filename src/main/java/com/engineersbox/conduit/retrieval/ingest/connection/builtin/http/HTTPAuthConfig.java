package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CUSTOM,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonTypeIdResolver(HTTPAuthConfigTypeResolver.class)
public abstract class HTTPAuthConfig {

    private final String type;

    @JsonCreator
    protected HTTPAuthConfig(@JsonProperty("type") final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
