package com.engineersbox.conduit_v2.ingest;

import com.engineersbox.conduit.schema.source.SourceType;

public abstract class Source<T, C extends Connector<T>> {

    private final SourceType type;

    protected Source(final SourceType type) {
        this.type = type;
    }

    public SourceType getType() {
        return this.type;
    }

    abstract public T invoke(final C connector, final IngestionContext ctx);

}
