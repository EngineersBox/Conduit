package com.engineersbox.conduit.pipeline.ingestion;

@FunctionalInterface
public interface IngestSource {

    Object ingest(final IngestionContext ctx);

}
