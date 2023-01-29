package com.engineersbox.conduit.pipeline;

import com.engineersbox.conduit.pipeline.ingestion.IngestSource;
import com.engineersbox.conduit.pipeline.ingestion.IngestionContext;
import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.PathBinding;
import com.google.common.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;
import io.riemann.riemann.Proto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
                value.getTypeRef().getType()
        );
    }

    @SuppressWarnings("unchecked")
    private List<Proto.Event> parseCoerceMetricEvents(final Object value,
                                                      final Class<?> clazz,
                                                      final Type type) throws ClassNotFoundException {
        if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
            return List.of(parsePrimitiveMetricEvent(value, clazz));
        } else if (clazz.isArray()) {
            final Object[] array = (Object[]) value;
            final Class<?> arrayComponentClass = clazz.arrayType();
            final Type arrayComponentType = TypeUtils.getArrayComponentType(type);
            final List<Proto.Event> events = new ArrayList<>();
            for (final Object component : array) {
                events.addAll(parseCoerceMetricEvents(
                        component,
                        arrayComponentClass,
                        arrayComponentType
                ));
            }
            return events;
        } else if (TypeUtils.isAssignable(type, ClassUtils.getClass(Collection.class.getName()))) {
            final Collection<Object> collection = (Collection<Object>) value;
            final List<Proto.Event> events = new ArrayList<>();
            final Type collectionComponentType = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
            for (final Object component : collection) {
                events.addAll(parseCoerceMetricEvents(
                        component,
                        TypeToken.of(collectionComponentType).getRawType(),
                        collectionComponentType
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
                TypeToken.of(convertedMetric.getClass()).getType()
        );
    }

    private Proto.Event parsePrimitiveMetricEvent(final Object value,
                                                  final Class<?> type) {
        if (ClassUtils.isAssignable(type, double.class, true)) {
            return this.eventTemplate.toBuilder()
                    .setMetricD((double) value)
                    .build();
        } else if (ClassUtils.isAssignable(type, float.class, true)) {
            return this.eventTemplate.toBuilder()
                    .setMetricF((float) value)
                    .build();
        } else if (ClassUtils.isAssignable(type, long.class, true)) {
            return this.eventTemplate.toBuilder()
                    .setMetricSint64((long) value)
                    .build();
        } else if (ClassUtils.isAssignable(type, boolean.class, true)) {
            return this.eventTemplate.toBuilder()
                    .setMetricSint64((boolean) value ? 1 : 0)
                    .build();
        }
        throw new ClassCastException("Unexpected non-primitive type: " + type);
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
