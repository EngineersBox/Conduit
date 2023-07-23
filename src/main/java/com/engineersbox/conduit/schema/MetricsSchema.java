package com.engineersbox.conduit.schema;

import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit.schema.metric.MetricContainerType;
import com.engineersbox.conduit.schema.metric.MetricType;
import com.engineersbox.conduit.schema.metric.MetricValueType;
import com.engineersbox.conduit.schema.provider.JsonProvider;
import com.engineersbox.conduit.schema.provider.MappingProvider;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorType;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.builtin.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import com.google.protobuf.TextFormat;
import com.jayway.jsonpath.Configuration;
import com.networknt.schema.ValidationMessage;
import io.riemann.riemann.Proto;
import org.apache.commons.collections4.IteratorUtils;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.tuple.ImmutableEntry;
import org.eclipse.collections.impl.utility.Iterate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MetricsSchema extends UnifiedMap<String, Metric> {

    private Configuration jsonPathConfiguration;
    private Connector<?,?> connector;
    private Proto.Event eventTemplate;
    private Path handler;
    private boolean requiresJseGlobals;

    private MetricsSchema() {
        super();
    }

    public Configuration getJsonPathConfiguration() {
        return this.jsonPathConfiguration;
    }

    public Connector<?, ?> getConnector() {
        return this.connector;
    }

    public Proto.Event getEventTemplate() {
        return this.eventTemplate;
    }

    public Path getHandler() {
        return this.handler;
    }

    public boolean requiresJseGlobals() {
        return this.requiresJseGlobals;
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
                .withConnector(parseConnector(definition.get("source")))
                .withHandler(parseHandlerPath(definition.get("handler")));
        try {
            builder.withEventTemplate(parseEventTemplate(definition.get("event_template")));
        } catch (final TextFormat.ParseException e) {
            throw new IllegalArgumentException("Unable to parse Riemann Proto.Event template for MetricsSchema", e);
        }
        final JsonNode metrics = definition.get("metrics");
        for (final JsonNode metric : metrics) {
            final JsonNode handlersNode = metric.get("handlers");
            if (handlersNode != null) {
                if (builder.schema.handler == null) {
                    throw new IllegalArgumentException("Missing \"handler\" path to reference handler methods from");
                }
                builder.schema.requiresJseGlobals = true;
            }
            builder.put(
                    Metric.path(metric.get("path").asText())
                            .namespace(metric.get("namespace").asText())
                            .handlers(parseHandlers(handlersNode))
                            .type(parseMetricType(
                                    MetricType.builder(),
                                    metric.get("type")
                            )).complete()
            );
        }
        return builder.build();
    }

    private static ImmutableMap<String, String> parseHandlers(final JsonNode handlersNode) {
        if (handlersNode == null || handlersNode.isNull()) {
            return Maps.immutable.empty();
        }
        return Iterate.toMap(
                IteratorUtils.asIterable(Iterators.transform(
                        handlersNode.fields(),
                        (final Map.Entry<String, JsonNode> handler) -> ImmutableEntry.of(
                                handler.getKey(),
                                handler.getValue().asText()
                        )
                )),
                Map.Entry::getKey,
                Map.Entry::getValue
        ).toImmutable();
    }

    private static Path parseHandlerPath(final JsonNode handlerNode) {
        if (handlerNode == null) {
            return null;
        }
        return Path.of(handlerNode.asText());
    }

    private static Connector<?,?> parseConnector(final JsonNode sourceNode) {
        if (sourceNode == null
            || sourceNode.isMissingNode()
            || sourceNode.isNull()
            || sourceNode.isEmpty()) {
            throw new IllegalArgumentException("Missing required \"source\" node in schema definition");
        }
        final ConnectorType connectorType = ConnectorType.valueOf(sourceNode.get("type").asText());
        return switch (connectorType) {
            case HTTP -> parseHTTPConnector(sourceNode);
            case CUSTOM -> null;
        };
    }

    private static HTTPConnector parseHTTPConnector(final JsonNode sourceNode) {
        final URI uri = URI.create(sourceNode.get("uri").asText());
        final JsonNode authNode = sourceNode.get("auth");
        final HTTPAuthType authType = HTTPAuthType.valueOf(authNode.get("type").asText());
        final HTTPConnector connector = new HTTPConnector();
        final HTTPConnectorConfiguration config = new HTTPConnectorConfiguration(
                uri,
                switch (authType) {
                    case BASIC -> new HTTPBasicAuthConfig(
                            authNode.get("username").asText(),
                            authNode.get("password").asText()
                    );
                    case CERTIFICATE -> new HTTPCertificateAuthConfig(
                            HTTPCertType.valueOf(authNode.get("certificate_type").asText()).name(),
                            authNode.has("password")
                                    ? authNode.get("password").asText()
                                    : null,
                            authNode.get("location").asText()
                    );
                }
        );
        connector.saturate(config);
        return connector;
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
                formatEventTemplate(eventTemplate),
                eventTemplateBuilder
        );
        return eventTemplateBuilder.build();
    }

    private static String formatEventTemplate(final JsonNode node) {
        return IteratorUtils.toString(
                node.fields(),
                (final Entry<String, JsonNode> childNode) -> childNode.getKey() + ": " + childNode.getValue(),
                ", ",
                "",
                ""
        );
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

        public Builder withConnector(final Connector<?,?> connector) {
            this.schema.connector = connector;
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
