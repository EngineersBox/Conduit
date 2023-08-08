package com.engineersbox.conduit_v2.retrieval.content;

import com.engineersbox.conduit_v2.processing.schema.Schema;
import com.engineersbox.conduit_v2.retrieval.ingest.IngesterFactory;
import com.engineersbox.conduit_v2.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit_v2.retrieval.ingest.Source;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit_v2.retrieval.path.PathTraversalHandler;
import com.jayway.jsonpath.Configuration;

import java.util.function.Function;

public abstract class ContentManagerFactory {

    private ContentManagerFactory() {
        throw new UnsupportedOperationException("Factory class");
    }

    public static <
            T,
            R,
            E extends ConnectorConfiguration,
            C extends Connector<R, E>
        > ContentManager<T, R, E, C> construct(final Schema schema,
                                               final Source<R> source,
                                               final IngestionContext context,
                                               final Function<R, T> converter) {
        final Configuration jsonPathConfig = schema.getJsonPathConfiguration();
        return new ContentManager<>(
                IngesterFactory.construct(schema, source, converter),
                context,
                new PathTraversalHandler<>(jsonPathConfig)
        );
    }

    public static <
            T,
            E extends ConnectorConfiguration,
            C extends Connector<T, E>
        > ContentManager<T, T, E, C> construct(final Schema schema,
                                               final Source<T> source,
                                               final IngestionContext context) {
        return ContentManagerFactory.construct(
                schema,
                source,
                context,
                Function.identity()
        );
    }

}
