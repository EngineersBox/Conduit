package com.engineersbox.conduit.core.retrieval.content;

import com.engineersbox.conduit.core.retrieval.ingest.IngesterFactory;
import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.Source;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.path.PathTraversalHandler;
import com.engineersbox.conduit.core.schema.Schema;
import com.jayway.jsonpath.Configuration;

public abstract class ContentManagerFactory {

    public abstract <
            T,
            E extends ConnectorConfiguration,
            C extends Connector<T, E>
        > ContentManager<T, E, C> construct(final Schema schema,
                                            final Source<T> source,
                                            final IngestionContext context,
                                            final IngesterFactory ingesterFactory);

    public static ContentManagerFactory defaultFactory() {
        return new ContentManagerFactory() {
            @Override
            public <
                    T,
                    E extends ConnectorConfiguration,
                    C extends Connector<T, E>
                > ContentManager<T, E, C> construct(final Schema schema,
                                                    final Source<T> source,
                                                    final IngestionContext context,
                                                    final IngesterFactory ingesterFactory) {
                final Configuration jsonPathConfig = schema.getJsonPathConfiguration();
                return new ContentManager<>(
                        ingesterFactory.construct(schema, source),
                        context,
                        new PathTraversalHandler<>(jsonPathConfig)
                );
            }
        };
    }

}