package com.engineersbox.conduit.core.retrieval.ingest;

import com.engineersbox.conduit.core.config.ConduitConfig;
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
                                         final PollingCondition pollingCondition,
                                         final ConduitConfig config);

    public static IngesterFactory defaultFactory() {
        return new IngesterFactory() {
            @Override
            public <T, R, E extends ConnectorConfiguration, C extends Connector<T, E>> Ingester<T, R, E, C> construct(final Schema schema,
                                                                                                                      final SourceProvider<T, R> sourceProvider,
                                                                                                                      final PollingCondition pollingCondition,
                                                                                                                      final ConduitConfig config) {
                return new Ingester<>(
                        sourceProvider,
                        schema,
                        pollingCondition,
                        config
                );
            }
        };
    }

}
