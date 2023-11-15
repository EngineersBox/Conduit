package com.engineersbox.conduit.core.retrieval.content;

import com.engineersbox.conduit.core.processing.PollingCondition;
import com.engineersbox.conduit.core.retrieval.configuration.AffinityBoundConfigProvider;
import com.engineersbox.conduit.core.retrieval.ingest.IngesterFactory;
import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.ingest.source.provider.SourceProvider;
import com.engineersbox.conduit.core.retrieval.path.PathTraversalHandler;
import com.engineersbox.conduit.core.schema.Schema;
import com.jayway.jsonpath.Configuration;

public abstract class ContentManagerFactory {

    public abstract <
            T, R,
            E extends ConnectorConfiguration,
            C extends Connector<T, E>
        > ContentManager<T, R, E, C> construct(final Schema schema,
                                               final SourceProvider<T, R> sourceProvider,
                                               final IngestionContext context,
                                               final IngesterFactory ingesterFactory,
                                               final PollingCondition pollingCondition);

    public static ContentManagerFactory defaultFactory() {
        return new ContentManagerFactory() {
            @Override
            public <
                    T, R,
                    E extends ConnectorConfiguration,
                    C extends Connector<T, E>
                    > ContentManager<T, R, E, C> construct(final Schema schema,
                                                           final SourceProvider<T, R> sourceProvider,
                                                           final IngestionContext context,
                                                           final IngesterFactory ingesterFactory,
                                                           final PollingCondition pollingCondition) {
                final Configuration config = schema.getJsonPathConfiguration();
                return new ContentManager<>(
                        ingesterFactory.construct(schema, sourceProvider),
                        context,
                        new PathTraversalHandler<>(config),
                        pollingCondition
                );
            }
        };
    }

    public static ContentManagerFactory defaultAffinityBoundFactory(final boolean cachedConfig) {
        return new ContentManagerFactory() {
            @Override
            public <
                    T, R,
                    E extends ConnectorConfiguration,
                    C extends Connector<T, E>
                > ContentManager<T, R, E, C> construct(final Schema schema,
                                                       final SourceProvider<T, R> sourceProvider,
                                                       final IngestionContext context,
                                                       final IngesterFactory ingesterFactory,
                                                       final PollingCondition pollingCondition) {
                AffinityBoundConfigProvider.bindDefaultConfiguration(schema.getJsonPathConfiguration());
                return new ContentManager<>(
                        ingesterFactory.construct(schema, sourceProvider),
                        context,
                        new PathTraversalHandler<>(cachedConfig),
                        pollingCondition
                );
            }
        };
    }

}
