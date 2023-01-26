package com.engineersbox.conduit.pipeline;

import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.PathBinding;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Pipeline {

    private final MetricsSchema schema;
    private final IngestSource ingestSource;
    private final Configuration configuration;

    public Pipeline(final MetricsSchema schema,
                    final IngestSource ingestSource,
                    final Configuration configuration) {
        this.schema = schema;
        this.ingestSource = ingestSource;
        this.configuration = configuration;
    }

    public Pipeline(final MetricsSchema schema,
                    final IngestSource ingestSource) {
        this(
                schema,
                ingestSource,
                Configuration.defaultConfiguration()
        );
    }

    public void executeYielding(final BiConsumer<String, TypedMetricValue<?>> metricConsumer) {
        final ReadContext context = JsonPath.using(this.configuration).parse(this.ingestSource.ingest());
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
