package com.engineersbox.conduit_v2.retrieval.ingest.connection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CUSTOM,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonTypeIdResolver(ConnectorTypeResolver.class)
public interface Connector<T, C extends ConnectorConfiguration> extends AutoCloseable {

    void saturate(final C config);

    void configure() throws Exception;

    T retrieve() throws Exception;

}
