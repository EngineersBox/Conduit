package com.engineersbox.conduit_v2.processing.schema;

import com.engineersbox.conduit.schema.Validator;
import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.schema.json.JsonPathConfigDeserializer;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.eclipsecollections.EclipseCollectionsModule;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.jayway.jsonpath.Configuration;
import com.networknt.schema.ValidationMessage;
import io.riemann.riemann.Proto;
import org.eclipse.collections.api.list.MutableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Metrics schema definition
 */
public class Schema {

    /* TODO: Support hooking into validation (schema valdiator library supports this)
     *       in order to build schema object at the same time as validation completes
     *       for a given node. Once validated, this can be reused and streamed node by
     *       node.  After the initial configuration section (non-metrics block)
     *       has been validated and parsed, this should be forwarded to a different thread
     *       to handle configuring the ingestion setup as needed (creating classes and stuff)
     *       At the same time metrics are being validated and then each one (once validated),
     *       is forwarded (pipelined) through the Conduit handlers to the worker thread pools
     *       that each have pipelines (or whatever the configuration is, could be one pipeline
     *       with many threads).
     *
     */

    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    static {
        configureMapper(MAPPER);
    }

    public static void configureMapper(final ObjectMapper mapper) {
        mapper.registerModules(
                new ProtobufModule(),
                new EclipseCollectionsModule()
        ).setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    // NOTE: Deserialized via ConnectorDeserializer
    @JsonProperty("source")
    private Connector<?,?> source;
    @JsonProperty("configuration")
    @JsonDeserialize(using = JsonPathConfigDeserializer.class)
    private Configuration jsonPathConfiguration;
    // NOTE: Deserialized via com.hubspot.jackson.datatype.protobuf.ProtobufModule
    @JsonProperty("eventTemplate")
    @JsonAlias("event_template")
    private Proto.Event eventTemplate;
    @JsonProperty("handler")
    private String handler;
    // NOTE: Collection deserialized via com.fasterxml.jackson.datatype.eclipsecollections.EclipseCollectionsModule
    // NOTE: Single metric deserialized via MetricDeserializer
//    private MutableList<Metric> metrics;

    public static Schema from(final String raw) throws IOException {
        return from(raw, MAPPER);
    }

    public static Schema from(final String raw, final ObjectMapper mapper) throws IOException {
        return from(mapper.readTree(raw), mapper);
    }

    public static Schema from(final File file) throws IOException {
        return from(file, MAPPER);
    }

    public static Schema from(final File file, final ObjectMapper mapper) throws IOException {
        return from(mapper.readTree(file), mapper);
    }

    private static Schema from(final JsonNode definition, final ObjectMapper mapper) throws IOException {
        final Set<ValidationMessage> messages = Validator.validate(definition);
        if (messages.isEmpty()) {
            return mapper.treeToValue(definition, Schema.class);
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

}
