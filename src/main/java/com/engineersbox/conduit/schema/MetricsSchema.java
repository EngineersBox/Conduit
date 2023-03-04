package com.engineersbox.conduit.schema;

import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit.schema.metric.MetricContainerType;
import com.engineersbox.conduit.schema.metric.MetricType;
import com.engineersbox.conduit.schema.metric.MetricValueType;
import com.engineersbox.conduit.schema.provider.JsonProvider;
import com.engineersbox.conduit.schema.provider.MappingProvider;
import com.engineersbox.conduit.source.Source;
import com.engineersbox.conduit.source.SourceType;
import com.engineersbox.conduit.source.custom.CustomSource;
import com.engineersbox.conduit.source.http.*;
import com.engineersbox.conduit.util.ObjectMapperModule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Range;
import com.google.protobuf.TextFormat;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.internal.filter.ValueNodes;
import com.networknt.schema.ValidationMessage;
import io.riemann.riemann.Proto;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MetricsSchema extends HashMap<String, Metric> {

    private Configuration jsonPathConfiguration;
    private Source source;
    private Proto.Event eventTemplate;
    private Path handler;
    private boolean requiresJseGlobals;

    private MetricsSchema() {
    }

    public Configuration getJsonPathConfiguration() {
        return this.jsonPathConfiguration;
    }

    public Source getSource() {
        return this.source;
    }

    public Proto.Event getEventTemplate() {
        return this.eventTemplate;
    }

    public static MetricsSchema.Builder builder() {
        return new Builder();
    }

    public static MetricsSchema from(final Metric...bindings) {
        final MetricsSchema.Builder builder = MetricsSchema.builder();
        Arrays.stream(bindings).forEach(builder::put);
        return builder.build();
    }

    public static MetricsSchema from(final JsonNode definition) {
        final Set<ValidationMessage> messages = Validator.validate(definition);
        if (messages.isEmpty()) {
            return parse(definition);
        }
        final var anon = new Object(){
            int index = 0;
        };
        final String formattedMessages = messages.stream()
                .map((final ValidationMessage msg) -> String.format(
                            " - [%d]: %s",
                            anon.index++,
                            msg.getMessage()
                )).collect(Collectors.joining("\n"));
        throw new IllegalArgumentException("Invalid schema definition:\n" + formattedMessages);
    }

    private static MetricsSchema parse(final JsonNode definition) {
        final MetricsSchema.Builder builder = MetricsSchema.builder()
                .withJsonPathConfig(parseJsonPathConfiguration(definition.get("configuration")))
                .withSource(parseSource(definition.get("source")))
                .withHandler(parseHandlerPath(definition.get("handler")));
        try {
            builder.withEventTemplate(parseEventTemplate(definition.get("event_template")));
        } catch (final TextFormat.ParseException e) {
            throw new IllegalArgumentException("Unable to parse Riemann Proto.Event template for MetricsSchema", e);
        }
        final JsonNode metrics = definition.get("metrics");
        for (final JsonNode metric : metrics) {
            final JsonNode handlerMethodNode = metric.get("handler_method");
            if (handlerMethodNode != null) {
                if (builder.schema.handler == null) {
                    throw new IllegalArgumentException("Missing \"handler\" path to reference handler method from");
                }
                builder.schema.requiresJseGlobals = true;
            }
            builder.put(
                    Metric.path(metric.get("path").asText())
                            .namespace(metric.get("namespace").asText())
                            .handlerMethod(handlerMethodNode != null ? handlerMethodNode.asText() : null)
                            .type(parseMetricType(
                                    MetricType.builder(),
                                    metric.get("type")
                            )).complete()
            );
        }
        return builder.build();
    }

    private static Path parseHandlerPath(final JsonNode handlerNode) {
        if (handlerNode == null) {
            return null;
        }
        return Path.of(handlerNode.asText());
    }

    private static Source parseSource(final JsonNode sourceNode) {
        if (sourceNode == null
            || sourceNode.isMissingNode()
            || sourceNode.isNull()
            || sourceNode.isEmpty()) {
            throw new IllegalArgumentException("Missing required \"source\" node in schema definition");
        }
        final SourceType sourceType = SourceType.valueOf(sourceNode.get("type").asText());
        return switch (sourceType) {
            case HTTP -> parseHTTPSource(sourceNode);
            case CUSTOM -> parseCustomSource(sourceNode);
        };
    }

    private static HTTPSource parseHTTPSource(final JsonNode sourceNode) {
        final URI uri = URI.create(sourceNode.get("uri").asText());
        final JsonNode authNode = sourceNode.get("auth");
        final HTTPAuthType authType = HTTPAuthType.valueOf(authNode.get("type").asText());
        return new HTTPSource(
                SourceType.HTTP,
                switch (authType) {
                    case BASIC -> new HTTPSourceBasicAuthConfig(
                            authNode.get("username").asText(),
                            authNode.get("password").asText()
                    );
                    case CERTIFICATE -> new HTTPSourceCertificateAuthConfig(
                            authNode.get("location").asText()
                    );
                },
                uri
        );
    }

    private static CustomSource parseCustomSource(final JsonNode sourceNode) {
        final Map<String, Object> properties = ObjectMapperModule.OBJECT_MAPPER.convertValue(
                sourceNode,
                new TypeReference<Map<String, Object>>() {}
        );
        properties.remove("type");
        return new CustomSource(properties);
    }

    private static MetricType parseMetricType(final MetricType.Builder builder,
                                              final JsonNode typeNode) {

        final JsonNode childTypeNode = typeNode.isObject() ? typeNode.get("type") : typeNode;
        if (!childTypeNode.isObject()) {
            builder.withValueType(MetricValueType.valueOf(childTypeNode.asText().toUpperCase())).build();
        }
        final JsonNode containerTypeNode = typeNode.get("container");
        if (containerTypeNode == null) {
            return builder.build();
        }
        final MetricContainerType containerType = MetricContainerType.valueOf(containerTypeNode.asText().toUpperCase());
        if (containerType.equals(MetricContainerType.NONE)) {
            return builder.build();
        }
        builder.withContainerType(containerType);
        final JsonNode suffixFormatNode = typeNode.get("suffix_format");
        if (suffixFormatNode == null) {
            builder.addSuffixFormat(Range.all(), "/{index}");
        } else if (suffixFormatNode.isArray()) {
            for (final JsonNode formatNode : suffixFormatNode) {
                final JsonNode fromJsonNode = formatNode.get("from");
                final JsonNode toJsonNode = formatNode.get("to");
                final int from = fromJsonNode == null ? -1 : fromJsonNode.asInt();
                final int to = toJsonNode == null ? -1 : toJsonNode.asInt();
                Range<Integer> range;
                if (from == -1 && to == -1) {
                    range = Range.all();
                } else if (from > -1 && to == -1) {
                    range = Range.atLeast(from);
                } else if (from == -1 && to > -1) {
                    range = Range.lessThan(to);
                } else {
                    range = Range.closedOpen(from, to);
                }
                builder.addSuffixFormat(
                        range,
                        formatNode.get("template").asText()
                );
            }
        } else {
            builder.addSuffixFormat(Range.all(), suffixFormatNode.asText());
        }
        return builder.withChild(parseMetricType(
                MetricType.builder(),
                childTypeNode
        )).build();
    }

    private static Configuration parseJsonPathConfiguration(final JsonNode configuration) {
        if (configuration == null
            || configuration.isMissingNode()
            || configuration.isNull()
            || configuration.isEmpty()) {
            return Configuration.defaultConfiguration();
        }
        final Configuration.ConfigurationBuilder builder = Configuration.builder();
        if (configuration.has("json_provider")) {
            builder.jsonProvider(JsonProvider.valueOf(
                    configuration.get("json_provider")
                            .asText()
                            .toUpperCase()
            ).getNewProvider());
        }
        if (configuration.has("mapping_provider")) {
            builder.mappingProvider(MappingProvider.valueOf(
                    configuration.get("mapping_provider")
                            .asText()
                            .toUpperCase()
            ).getNewProvider());
        }
        return builder.build();
    }

    private static Proto.Event parseEventTemplate(final JsonNode eventTemplate) throws TextFormat.ParseException {
        if (eventTemplate == null) {
            return Proto.Event.getDefaultInstance();
        }
        final Proto.Event.Builder eventTemplateBuilder = Proto.Event.newBuilder();
        TextFormat.getParser().merge(
                eventTemplate.asText(),
                eventTemplateBuilder
        );
        return eventTemplateBuilder.build();
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

        public Builder withEventTemplate(final Proto.Event eventTemplate) {
            this.schema.eventTemplate = eventTemplate;
            return this;
        }

        public Builder withSource(final Source source) {
            this.schema.source = source;
            return this;
        }

        public Builder withJsonPathConfig(final Configuration jsonPathConfig) {
            this.schema.jsonPathConfiguration = jsonPathConfig;
            return this;
        }

        public Builder withHandler(final Path handler) {
            this.schema.handler = handler;
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
