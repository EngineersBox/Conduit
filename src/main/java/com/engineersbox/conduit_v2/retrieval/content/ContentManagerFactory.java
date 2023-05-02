package com.engineersbox.conduit_v2.retrieval.content;

import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit_v2.retrieval.ingest.IngesterFactory;
import com.engineersbox.conduit_v2.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit_v2.retrieval.path.PathTraversalHandler;
import com.jayway.jsonpath.Configuration;

import java.util.function.Function;

public class ContentManagerFactory {

    public static <
            T,
            R,
            E extends ConnectorConfiguration,
            C extends Connector<R, E>
        > ContentManager<T, R, E, C> construct(final MetricsSchema schema,
                                               final IngestionContext context,
                                               final Function<R, T> converter) {
        final Configuration jsonPathConfig = null; // TODO: Get from schema
        return new ContentManager<>(
                IngesterFactory.construct(schema, converter),
                context,
                new PathTraversalHandler<>(jsonPathConfig)
        );
    }

    @SuppressWarnings("unchecked")
    public static <
            T,
            E extends ConnectorConfiguration,
            C extends Connector<T, E>
        > ContentManager<T, T, E, C> construct(final MetricsSchema schema,
                                                   final IngestionContext context) {
        return ContentManagerFactory.construct(
                schema,
                context,
                Function.identity()
        );
    }

}
