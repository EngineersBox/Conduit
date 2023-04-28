package com.engineersbox.conduit_v2.retrieval.ingest;

import com.engineersbox.conduit.schema.source.SourceType;

public abstract class Source<T> {

    private final SourceType type;

    protected Source(final SourceType type) {
        this.type = type;
    }

    public SourceType getType() {
        return this.type;
    }

    abstract public <C extends Connector<T>> T invoke(final C connector, final IngestionContext ctx);

}
