package com.engineersbox.conduit_v2.retrieval.ingest;

import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;

import java.util.function.Function;

public class IngesterFactory {

    public static <
            T,
            R,
            E extends ConnectorConfiguration,
            C extends Connector<R, E>
        > Ingester<T, R, E, C> construct(final MetricsSchema schema,
                                         final Source<R> source,
                                         final Function<R, T> converter) {
        return new Ingester<>(
                source,
                (C) schema.getConnector(),
                converter
        );
    }

    public static <
            T,
            E extends ConnectorConfiguration,
            C extends Connector<T, E>
        > Ingester<T, T, E, C> construct(final MetricsSchema schema,
                                         final Source<T> source) {
        return IngesterFactory.construct(schema, source, Function.identity());
    }

}
