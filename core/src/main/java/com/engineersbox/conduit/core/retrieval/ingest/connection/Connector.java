package com.engineersbox.conduit.core.retrieval.ingest.connection;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Optional;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CUSTOM,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonTypeIdResolver(ConnectorTypeResolver.class)
public abstract class Connector<T, C extends ConnectorConfiguration> implements AutoCloseable {

    @SuppressWarnings({"unused"})
    @JsonProperty("cacheKey")
    @JsonAlias("cache_key")
    public Optional<String> cacheKey;

    public String name() {
        return this.getClass().getName();
    }

    public abstract void saturate(final C config);

    public abstract void configure() throws Exception;

    public abstract T retrieve() throws Exception;

    public boolean isAnonymous() {
        return false;
    }

}
