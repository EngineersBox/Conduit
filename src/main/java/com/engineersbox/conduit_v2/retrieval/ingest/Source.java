package com.engineersbox.conduit_v2.retrieval.ingest;

public abstract class Source<T> {

    private final ConnectorType type;

    protected Source(final ConnectorType type) {
        this.type = type;
    }

    public ConnectorType getType() {
        return this.type;
    }

    abstract public <C extends Connector<T>> T invoke(final C connector, final IngestionContext ctx);

}
