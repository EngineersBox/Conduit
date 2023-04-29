package com.engineersbox.conduit_v2.retrieval.ingest;

import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorType;

public abstract class Source<T> {

    private final ConnectorType type;

    protected Source(final ConnectorType type) {
        this.type = type;
    }

    public ConnectorType getType() {
        return this.type;
    }

    abstract public <E extends ConnectorConfiguration, C extends Connector<T, E>> T invoke(final C connector, final IngestionContext ctx);

}
