package com.engineersbox.conduit_v2.retrieval.ingest;

import com.engineersbox.conduit_v2.processing.schema.Schema;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;

public abstract class IngesterFactory {

    public abstract <
            T,
            E extends ConnectorConfiguration,
            C extends Connector<T, E>
        > Ingester<T, E, C> construct(final Schema schema,
                                      final Source<T> source);

    public static IngesterFactory defaultFactory() {
        return new IngesterFactory() {
            @Override
            public <T, E extends ConnectorConfiguration, C extends Connector<T, E>> Ingester<T, E, C> construct(Schema schema, Source<T> source) {
                return new Ingester<>(source, (C) schema.getConnector());
            }
        };
    }

}
