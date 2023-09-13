package com.engineersbox.conduit.retrieval.content;

import com.engineersbox.conduit.schema.Schema;
import com.engineersbox.conduit.retrieval.ingest.IngesterFactory;
import com.engineersbox.conduit.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.retrieval.ingest.Source;
import com.engineersbox.conduit.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.retrieval.path.PathTraversalHandler;
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