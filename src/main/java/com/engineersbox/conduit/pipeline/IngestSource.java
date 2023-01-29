package com.engineersbox.conduit.pipeline;

@FunctionalInterface
public interface IngestSource {

    Object ingest(final IngestionContext ctx);

}
