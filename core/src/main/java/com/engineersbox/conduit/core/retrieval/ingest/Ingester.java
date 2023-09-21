package com.engineersbox.conduit.core.retrieval.ingest;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;

public class Ingester<T, E extends ConnectorConfiguration, C extends Connector<T, E>> {

    private final Source<T> source;
    private final C connector;
    private T data = null;

    public Ingester(final Source<T> source,
                    final C connector) {
        this.source = source;
        this.connector = connector;
    }

    public void clear() {
        this.data = null;
    }

    public void consumeSource(final IngestionContext context) throws Exception {
        this.data = this.source.invoke(
                this.connector,
                context
        );
    }

    public T getCurrent() {
        return this.data;
    }

}
