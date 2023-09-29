package com.engineersbox.conduit.core.retrieval.ingest;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ingester<T, E extends ConnectorConfiguration, C extends Connector<T, E>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ingester.class);

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
        LOGGER.trace(
                "Consuming source {} from connector {}",
                this.source.name(),
                this.connector.name()
        );
        this.data = this.source.invoke(
                this.connector,
                context
        );
    }

    public T getCurrent() {
        return this.data;
    }

}
