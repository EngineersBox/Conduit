package com.engineersbox.conduit.pipeline;

import com.engineersbox.conduit.pipeline.ingestion.IngestSource;
import com.engineersbox.conduit.pipeline.ingestion.IngestionContext;
import com.engineersbox.conduit.schema.DimensionIndex;
import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit.schema.metric.MetricContainerType;
import com.engineersbox.conduit.schema.metric.MetricType;
import com.engineersbox.conduit.schema.metric.MetricValueType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.riemann.riemann.Proto;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Pipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Supplier<MetricsSchema> schemaSupplier;
    private MetricsSchema schema;
    private long schemaHash;
    private final IngestSource ingestSource;
    private final BatchingConfiguration batchConfig;
    private IngestionContext ingestionContext;

    public Pipeline(final MetricsSchema schema,
                    final IngestSource ingestSource,
                    final BatchingConfiguration batchConfig) {
        this.schemaSupplier = () -> schema;
        this.schemaHash = 0L;
        this.ingestSource = ingestSource;
        this.batchConfig = batchConfig;
        this.ingestionContext = IngestionContext.defaultContext();
    }

    public Pipeline(final String schemaPath,
                    final IngestSource ingestSource,
                    final BatchingConfiguration batchConfig) {
        if (!Path.of(schemaPath).toFile().exists()) {
            throw new IllegalArgumentException("Could not load schema from file path: " + schemaPath);
        }
        this.schemaSupplier = () -> {
            final File schemaFile = Path.of(schemaPath).toFile();
            if (this.schemaHash == computeHash(schemaFile)) {
                return this.schema;
            }
            try {
                return MetricsSchema.from(OBJECT_MAPPER.readTree(schemaFile));
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read schema from path " + schemaPath, e);
            }
        };
        this.schemaHash = -1;
        this.ingestSource = ingestSource;
        this.batchConfig = batchConfig;
        this.ingestionContext = IngestionContext.defaultContext();
    }

    private static long computeHash(final File file) {
        try {
            return Files.asByteSource(file).hash(Hashing.adler32()).padToLong();
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to compute Adler32 hash for file " + file.getName());
        }
    }

    public Pipeline(final MetricsSchema schema,
                    final IngestSource ingestSource) {
        this(
                schema,
                ingestSource,
                new BatchingConfiguration(1, 1)
        );
    }

    public void configureIngestionContext(final IngestionContext ctx) {
        this.ingestionContext = ctx;
    }

    public void executeHandled(final Consumer<List<Proto.Event>> batchedEventsConsumer) {
        this.schema = this.schemaSupplier.get();
        final List<List<Metric>> workload = this.batchConfig.splitWorkload(new ArrayList<>(this.schema.values()));
        final ExecutorService executor = this.batchConfig.generateExecutorService();
        final ReadContext context = JsonPath.using(this.schema.getJsonPathConfiguration())
                .parse(this.ingestSource.apply(this.ingestionContext));
        workload.stream()
                .map((final List<Metric> batch) -> CompletableFuture.runAsync(
                        () -> handleBatch(
                                batch,
                                context,
                                batchedEventsConsumer
                        ),
                        executor
                )).forEach(CompletableFuture::join);
    }

    private void handleBatch(final List<Metric> batch,
                             final ReadContext context,
                             final Consumer<List<Proto.Event>> batchedEventsConsumer) {
        final List<List<Metric>> partitionedBatch = ListUtils.partition(
                batch,
                Math.min(
                        batchConfig.getBulkSize(),
                        batch.size()
                )
        );
        for (final List<Metric> bindings : partitionedBatch) {
            final List<Proto.Event> events = bindings.stream()
                    .flatMap((final Metric metric) -> {
                        try {
                            return parseEvents(
                                    context,
                                    metric
                            ).stream();
                        } catch (final ClassNotFoundException e) {
                            LOGGER.error("Unable to parse events: ", e);
                            return Stream.of();
                        }
                    }).toList();
            batchedEventsConsumer.accept(events);
        }
    }

    private List<Proto.Event> parseEvents(final ReadContext context,
                                          final Metric metric) throws ClassNotFoundException {
        LOGGER.debug("Concrete type: " + TypeUtils.toString(metric.getType().intoConcrete().getType()));
        return parseCoerceMetricEvents(
                context.read(
                        metric.getPath(),
                        metric.getType().intoConcrete()
                ),
                metric.getType(),
                metric,
                0,
                ""
        );
    }

    private List<Proto.Event> parseCoerceMetricEvents(final Object value,
                                                      final MetricType type,
                                                      final Metric metric,
                                                      final int currentDimension,
                                                      final String suffix) {
        if (type.isLeaf()) {
            return List.of(parsePrimitiveMetricEvent(
                    value,
                    type.getValueType(),
                    metric.getNamespace(),
                    suffix
            ));
        }
        final MetricContainerType containerType = type.getContainerType();
        return switch (containerType) {
            case LIST -> parseListMetricEvents(
                    value,
                    type,
                    metric,
                    currentDimension,
                    suffix
            );
            case MAP -> parseMapMetricEvents(
                    value,
                    type,
                    metric,
                    currentDimension,
                    suffix
            );
            default -> throw new IllegalStateException("Unknown metric container type: " + containerType.name());
        };
    }

    private List<Proto.Event> parseListMetricEvents(final Object value,
                                                    final MetricType type,
                                                    final Metric metric,
                                                    final int currentDimension,
                                                    final String suffix) {
        final List<?> list = (List<?>) value;
        final List<Proto.Event> events = new ArrayList<>();
        int index = 0;
        for (final Object component : list) {
            final String nextSuffix = formatSuffix(
                    suffix,
                    currentDimension,
                    index,
                    metric
            );
            events.addAll(parseCoerceMetricEvents(
                    component,
                    type.getChild().get(),
                    metric,
                    currentDimension + 1,
                    nextSuffix
            ));
            index++;
        }
        return events;
    }

    private List<Proto.Event> parseMapMetricEvents(final Object value,
                                                   final MetricType type,
                                                   final Metric metric,
                                                   final int currentDimension,
                                                   final String suffix) {
        final Map<String, ?> map = (Map<String, ?>) value;
        final List<Proto.Event> events = new ArrayList<>();
        int index = 0;
        for (final Map.Entry<String, ?> entry : map.entrySet()) {
            final String nextSuffix = formatSuffix(
                    suffix,
                    currentDimension,
                    index,
                    metric
            );
            events.addAll(parseCoerceMetricEvents(
                    entry.getValue(),
                    type.getChild().get(),
                    metric,
                    currentDimension + 1,
                    nextSuffix + entry.getKey()
            ));
            index++;
        }
        return events;
    }

    private String formatSuffix(final String current,
                                final int dimension,
                                final int index,
                                final Metric binding) {
        String nextSuffix = current;
        final String dimIdxSuffix = binding.getSuffix(DimensionIndex.ofQuery(
                dimension,
                index
        ));
        if (dimIdxSuffix != null) {
            nextSuffix += dimIdxSuffix.replace("{index}", Integer.toString(index));
        } else {
            nextSuffix += "/" + index;
        }
        return nextSuffix;
    }

    private Proto.Event parsePrimitiveMetricEvent(final Object value,
                                                  final MetricValueType type,
                                                  final String metricNamespace,
                                                  final String suffix) {
        final Proto.Event.Builder builder = this.schema.getEventTemplate().toBuilder()
                .setService(metricNamespace + suffix);
        switch (type) {
            case DOUBLE -> builder.setMetricD((double) value);
            case FLOAT -> builder.setMetricF((float) value);
            case INTEGER -> builder.setMetricSint64((long) value);
            case BOOLEAN -> builder.setMetricSint64((boolean) value ? 1 : 0);
            case STRING -> builder.setState((String) value);
            default -> throw new ClassCastException("Unsupported primitive type: " + type.name());
        }
        return builder.build();
    }

}
