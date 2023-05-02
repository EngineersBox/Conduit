package com.engineersbox.conduit_v2.retrieval.ingest;

import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;

import java.util.function.Function;

public class Ingester<T, R, E extends ConnectorConfiguration, C extends Connector<R, E>> {

    private final Source<R> source;
    private final C connector;
    // NOTE: This can be provided programmatically as a config, otherwise should be Function.identity()
    private final Function<R, T> rawConverter;
    private T data = null;

    public Ingester(final Source<R> source,
                    final C connector,
                    final Function<R, T> rawConverter) {
        this.source = source;
        this.connector = connector;
        this.rawConverter = rawConverter;
    }

    public void clear() {
        this.data = null;
    }

    public void consumeSource(final IngestionContext context) {
        this.data = this.rawConverter.apply(this.source.invoke(
                this.connector,
                context
        ));
    }

    public T getCurrent() {
        return this.data;
    }

}
