package com.engineersbox.conduit.retrieval.ingest;

import com.engineersbox.conduit.schema.Schema;
import com.engineersbox.conduit.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.retrieval.ingest.connection.ConnectorConfiguration;

public abstract class IngesterFactory {

    public abstract <
            T,
            E extends ConnectorConfiguration,
            C extends Connector<T, E>
        > Ingester<T, E, C> construct(final Schema schema,
                                      final Source<T> source);

    @SuppressWarnings("unchecked")
    public static IngesterFactory defaultFactory() {
        return new IngesterFactory() {
            @Override
            public <T, E extends ConnectorConfiguration, C extends Connector<T, E>> Ingester<T, E, C> construct(final Schema schema,
                                                                                                                final Source<T> source) {
                return new Ingester<>(
                        source,
                        (C) schema.getConnector()
                );
            }
        };
    }

}
