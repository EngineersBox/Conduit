package com.engineersbox.conduit.core.retrieval.ingest;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.ingest.source.Source;
import com.engineersbox.conduit.core.schema.Schema;

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
