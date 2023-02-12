package com.engineersbox.conduit.pipeline.ingestion;

import java.util.function.Function;

public interface IngestSource extends Function<IngestionContext, String> {

    @Override
    String apply(final IngestionContext ctx);

}
