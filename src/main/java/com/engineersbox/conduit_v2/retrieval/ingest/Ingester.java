package com.engineersbox.conduit_v2.retrieval.ingest;

import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;

public class Ingester<R, E extends ConnectorConfiguration, C extends Connector<R, E>> {

    private final Source<R> source = null;
    private final C connector = null;

    private R rawData = null;

    public void clear() {
        this.rawData = null;
    }

    public void consumeSource(final IngestionContext context) {
        this.rawData = this.source.invoke(
                this.connector,
                context
        );
    }

    public R getCurrent() {
        return this.rawData;
    }

}
