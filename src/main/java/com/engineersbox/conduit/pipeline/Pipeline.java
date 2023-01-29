package com.engineersbox.conduit.pipeline;

import com.engineersbox.conduit.pipeline.ingestion.IngestSource;
import com.engineersbox.conduit.pipeline.ingestion.IngestionContext;
import com.engineersbox.conduit.schema.DimensionIndex;
import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.PathBinding;
import com.google.common.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;
import io.riemann.riemann.Proto;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Pipeline {

    private final MetricsSchema schema;
    private final Proto.Event eventTemplate;
    private final IngestSource ingestSource;
    private final BatchingConfiguration batchConfig;
    private IngestionContext ingestionContext;
    private final Map<Type, TriFunction<Object, Class<?>, Type, ?>> typeConversionHandlers;

    public Pipeline(final MetricsSchema schema,
                    final Proto.Event eventTemplate,
                    final IngestSource ingestSource,
                    final BatchingConfiguration batchConfig) {
        this.schema = schema;
        this.eventTemplate = eventTemplate;
        this.ingestSource = ingestSource;
        this.batchConfig = batchConfig;
        this.ingestionContext = IngestionContext.defaultContext();
        this.typeConversionHandlers = new HashMap<>();
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

    public <T> void submitTypeConversionHandler(final TypeRef<?> type,
                                            final TriFunction<Object, Class<?>, Type, ?> typeConversionHandler) {
        this.typeConversionHandlers.put(
                type.getType(),
                typeConversionHandler
        );
    }

    public void executeHandled(final Consumer<List<Proto.Event>> batchedEventsConsumer) {
        final List<List<PathBinding>> workload = this.batchConfig.splitWorkload(new ArrayList<>(this.schema.values()));
        final ExecutorService executor = this.batchConfig.generateExecutorService();
        final ReadContext context = JsonPath.using(this.schema.getJsonPathConfiguration())
                        .parse(this.ingestSource.ingest(this.ingestionContext));
        workload.stream()
                .map((final List<PathBinding> batch) -> CompletableFuture.runAsync(
                        () -> handleBatch(
                                batch,
                                context,
                                batchedEventsConsumer
                        ),
                        executor
                )).forEach(CompletableFuture::join);

    }

    private void handleBatch(final List<PathBinding> batch,
                             final ReadContext context,
                             final Consumer<List<Proto.Event>> batchedEventsConsumer) {
        final List<List<PathBinding>> partitionedBatch = ListUtils.partition(
                batch,
                Math.min(
                        batchConfig.getBulkSize(),
                        batch.size()
                )
        );
        for (final List<PathBinding> bindings : partitionedBatch) {
            final List<Proto.Event> events = bindings.stream()
                    .flatMap((final PathBinding binding) -> {
                        try {
                            return parseEvents(
                                    context,
                                    binding
                            ).stream();
                        } catch (final ClassNotFoundException ignored) {
                            // TODO: Log this
                            return Stream.of();
                        }
                    }).toList();
            batchedEventsConsumer.accept(events);
        }
    }

    private List<Proto.Event> parseEvents(final ReadContext context,
                                          final PathBinding binding) throws ClassNotFoundException {
        final TypedMetricValue<?> value = new TypedMetricValue<>(context.read(
                binding.getPath(),
                binding.getDataType()
        ));
        return parseCoerceMetricEvents(
                value.getValue(),
                value.getTypeClass(),
                value.getTypeRef().getType(),
                binding,
                0,
                ""
        );
    }

    @SuppressWarnings("unchecked")
    private List<Proto.Event> parseCoerceMetricEvents(final Object value,
                                                      final Class<?> clazz,
                                                      final Type type,
                                                      final PathBinding binding,
                                                      final int currentDimension,
                                                      final String suffix) throws ClassNotFoundException {
        if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
            return List.of(parsePrimitiveMetricEvent(
                    value,
                    clazz,
                    binding.getMetricNamespace(),
                    suffix
            ));
        } else if (clazz.isArray()) {
            final Object[] array = (Object[]) value;
            final Class<?> arrayComponentClass = clazz.arrayType();
            final Type arrayComponentType = TypeUtils.getArrayComponentType(type);
            final List<Proto.Event> events = new ArrayList<>();
            int index = 0;
            for (final Object component : array) {
                final String nextSuffix = formatSuffix(
                        suffix,
                        currentDimension,
                        index,
                        binding
                );
                events.addAll(parseCoerceMetricEvents(
                        component,
                        arrayComponentClass,
                        arrayComponentType,
                        binding,
                        currentDimension + 1,
                        nextSuffix
                ));
                index++;
            }
            return events;
        } else if (TypeUtils.isAssignable(type, ClassUtils.getClass(Collection.class.getName()))) {
            final Collection<Object> collection = (Collection<Object>) value;
            final List<Proto.Event> events = new ArrayList<>();
            final Type collectionComponentType = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
            final Class<?> collectionComponentClass = TypeToken.of(collectionComponentType).getRawType();
            int index = 0;
            for (final Object component : collection) {
                final String nextSuffix = formatSuffix(
                        suffix,
                        currentDimension,
                        index,
                        binding
                );
                events.addAll(parseCoerceMetricEvents(
                        component,
                        collectionComponentClass,
                        collectionComponentType,
                        binding,
                        currentDimension + 1,
                        nextSuffix
                ));
            }
            return events;
        } else if (TypeUtils.isAssignable(type, ClassUtils.getClass(Map.class.getName()))) {
            final Type[] mapTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (!TypeUtils.isAssignable(mapTypeArguments[0], TypeUtils.wrap(String.class).getType())) {
                throw new ClassNotFoundException("Map type metric does not have string keys, cannot convert to metric");
            }
            final Class<?> mapValueClass = TypeToken.of(mapTypeArguments[1]).getRawType();
            final Map<Object, Object> map = (Map<Object, Object>) value;
            final List<Proto.Event> events = new ArrayList<>();
            for (final Map.Entry<Object, Object> entry : map.entrySet()) {
                events.addAll(parseCoerceMetricEvents(
                        entry.getValue(),
                        mapValueClass,
                        mapTypeArguments[1],
                        binding,
                        currentDimension + 1,
                        suffix + entry.getKey()
                ));
            }
            return events;
        }
        final TriFunction<Object, Class<?>, Type, ?> handler = this.typeConversionHandlers.get(type);
        if (handler == null) {
            // TODO: Make this configurable to skip and just log with return of List.of()
            throw new ClassNotFoundException(String.format(
                    "Unable to convert metric of type %s into usable value",
                    type
            ));
        }
        final Object convertedMetric = handler.apply(value, clazz, type);
        return parseCoerceMetricEvents(
                value,
                convertedMetric.getClass(),
                TypeToken.of(convertedMetric.getClass()).getType(),
                binding,
                currentDimension + 1,
                suffix
        );
    }

    private String formatSuffix(final String current,
                                final int dimension,
                                final int index,
                                final PathBinding binding) {
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
                                                  final Class<?> type,
                                                  final String metricNamespace,
                                                  final String suffix) {
        final Proto.Event.Builder builder = this.eventTemplate.toBuilder()
                .setService(metricNamespace + suffix);
        if (ClassUtils.isAssignable(type, double.class, true)) {
            builder.setMetricD((double) value);
        } else if (ClassUtils.isAssignable(type, float.class, true)) {
            builder.setMetricF((float) value);
        } else if (ClassUtils.isAssignable(type, long.class, true)) {
            builder.setMetricSint64((long) value);
        } else if (ClassUtils.isAssignable(type, boolean.class, true)) {
            builder.setMetricSint64((boolean) value ? 1 : 0);
        } else {
            throw new ClassCastException("Unexpected non-primitive type: " + type);
        }
        return builder.build();
    }

    public void executeYielding(final BiConsumer<String, TypedMetricValue<?>> metricConsumer) {
        final ReadContext context = JsonPath.using(this.schema.getJsonPathConfiguration())
                .parse(this.ingestSource.ingest(this.ingestionContext));
        this.schema.values().forEach((final PathBinding binding) -> {
            metricConsumer.accept(
                    binding.getMetricNamespace(),
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
