package com.engineersbox.conduit_v2.retrieval.ingest;

public class Ingester<R> {

    private final Source<R> source = null;
    private final Connector<R> connector = null;

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
