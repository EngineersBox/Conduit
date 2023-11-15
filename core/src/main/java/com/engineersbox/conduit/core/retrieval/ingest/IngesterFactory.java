package com.engineersbox.conduit.core.retrieval.ingest;

import com.engineersbox.conduit.core.processing.PollingCondition;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.ingest.source.provider.SourceProvider;
import com.engineersbox.conduit.core.schema.Schema;

public abstract class IngesterFactory {

    public abstract <
            T, R,
            E extends ConnectorConfiguration,
            C extends Connector<T, E>
        > Ingester<T, R, E, C> construct(final Schema schema,
                                         final SourceProvider<T, R> sourceProvider,
                                         final PollingCondition pollingCondition);

    @SuppressWarnings("unchecked")
    public static IngesterFactory defaultFactory() {
        return new IngesterFactory() {
            @Override
            public <T, R, E extends ConnectorConfiguration, C extends Connector<T, E>> Ingester<T, R, E, C> construct(final Schema schema,
                                                                                                                      final SourceProvider<T, R> sourceProvider,
                                                                                                                      final PollingCondition pollingCondition) {
                return new Ingester<>(
                        sourceProvider,
                        (C) schema.getConnector(),
                        pollingCondition
                );
            }
        };
    }

}
