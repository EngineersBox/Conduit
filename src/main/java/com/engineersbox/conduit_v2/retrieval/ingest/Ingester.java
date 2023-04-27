package com.engineersbox.conduit_v2.retrieval.ingest;

public class Ingester<R> {

    private R rawData = null;

    public void clear() {
        this.rawData = null;
    }

    public void consumeSource() {

    }

    public R getCurrent() {
        return this.rawData;
    }

}
