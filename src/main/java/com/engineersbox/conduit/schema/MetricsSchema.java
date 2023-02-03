package com.engineersbox.conduit.schema;

import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit.schema.metric.MetricType;
import com.engineersbox.conduit.schema.provider.JsonProvider;
import com.engineersbox.conduit.schema.provider.MappingProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.networknt.schema.ValidationMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class MetricsSchema extends HashMap<String, Metric> {

    private Configuration jsonPathConfiguration;

    private MetricsSchema() {
    }

    public Configuration getJsonPathConfiguration() {
        return this.jsonPathConfiguration;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MetricsSchema from(final Metric...bindings) {
        final Builder builder = MetricsSchema.builder();
        Arrays.stream(bindings).forEach(builder::put);
        return builder.build();
    }

    public static MetricsSchema from(final JsonNode definition) {
        final Set<ValidationMessage> messages = Validator.validate(definition);
        if (messages.isEmpty()) {
            return parse(definition);
        }
        // TODO: Throw error with validation messages
        return null;
    }

    private static MetricsSchema parse(final JsonNode definition) {
        final Builder builder = new MetricsSchema.Builder();
        final JsonNode configuration = definition.get("configuration");
        builder.withJsonPathConfig(parseJsonPathConfiguration(configuration));
        // TODO: Parse and handle the 'source' node here
        final JsonNode metrics = definition.get("metrics");
        for (final JsonNode metric : metrics) {
            builder.put(
                    Metric.path(metric.get("path").asText())
                            .namespace(metric.get("namespace").asText())
                            .type(parseMetricType(
                                    MetricType.builder(),
                                    metric.get("type")
                            )).complete()
            );
        }
        return builder.build();
    }

    private static MetricType parseMetricType(final MetricType.Builder builder,
                                              final JsonNode typeNode) {

    }

    private static Configuration parseJsonPathConfiguration(final JsonNode configuration) {
        return Configuration.builder()
                .jsonProvider(JsonProvider.valueOf(configuration.get("json_provider").asText().toUpperCase()).getNewProvider())
                .mappingProvider(MappingProvider.valueOf(configuration.get("mapping_provider").asText().toUpperCase()).getNewProvider())
                .build();
    }

    public static class Builder {

        private final MetricsSchema schema;

        private Builder() {
            this.schema = new MetricsSchema();
        }

        public Builder put(final Metric binding) {
            binding.validate();
            this.schema.put(
                    binding.getPath(),
                    binding
            );
            return this;
        }

        public Builder withJsonPathConfig(final Configuration jsonPathConfig) {
            this.schema.jsonPathConfiguration = jsonPathConfig;
            return this;
        }

        public MetricsSchema build() {
            if (this.schema.jsonPathConfiguration == null) {
                this.schema.jsonPathConfiguration = Configuration.defaultConfiguration();
            }
            return this.schema;
        }

    }

}
