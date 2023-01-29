package com.engineersbox.conduit.pipeline;

import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.PathBinding;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.riemann.riemann.Proto;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Pipeline {

    private final MetricsSchema schema;
    private final Proto.Event eventTemplate;
    private final IngestSource ingestSource;
    private final BatchingConfiguration batchConfig;
    private IngestionContext ingestionContext;

    public Pipeline(final MetricsSchema schema,
                    final Proto.Event eventTemplate,
                    final IngestSource ingestSource,
                    final BatchingConfiguration batchConfig) {
        this.schema = schema;
        this.eventTemplate = eventTemplate;
        this.ingestSource = ingestSource;
        this.batchConfig = batchConfig;
        this.ingestionContext = IngestionContext.defaultContext();
    }

    public Pipeline(final MetricsSchema schema,
                    final Proto.Event eventTemplate,
                    final IngestSource ingestSource) {
        this(
                schema,
                eventTemplate,
                ingestSource,
                new BatchingConfiguration(1, 1)
        );
    }

    public void configureIngestionContext(final IngestionContext ctx) {
        this.ingestionContext = ctx;
    }

    public void executeYielding(final BiConsumer<String, TypedMetricValue<?>> metricConsumer) {
        final ReadContext context = JsonPath.using(this.schema.getJsonPathConfiguration())
                .parse(this.ingestSource.ingest(this.ingestionContext));
        this.schema.values().forEach((final PathBinding binding) -> {
            metricConsumer.accept(
                    binding.getMetricName(),
                    new TypedMetricValue<>(context.read(
                            binding.getPath(),
                            binding.getDataType()
                    ))
            );
        });
    }

    public Map<String, TypedMetricValue<?>> executeGrouped() {
        final Map<String, TypedMetricValue<?>> results = new HashMap<>();
        executeYielding(results::put);
        return results;
    }

}
