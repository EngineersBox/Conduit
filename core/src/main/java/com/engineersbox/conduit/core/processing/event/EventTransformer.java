package com.engineersbox.conduit.core.processing.event;

import com.engineersbox.conduit.core.schema.metric.DimensionIndex;
import com.engineersbox.conduit.core.schema.metric.Metric;
import com.engineersbox.conduit.core.schema.metric.MetricKind;
import com.engineersbox.conduit.core.schema.metric.MetricType;
import io.riemann.riemann.Proto;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventTransformer.class);

    private final Proto.Event eventTemplate;

    public EventTransformer(final Proto.Event eventTemplate) {
        this.eventTemplate = eventTemplate;
    }

    public List<Proto.Event> parseCoerceMetricEvents(final Object value,
                                                     final MetricType type,
                                                     final Metric metric) {
        return parseCoerceMetricEvents(
                value,
                type,
                metric,
                0,
                ""
        );
    }

    public List<Proto.Event> parseCoerceMetricEvents(final Object value,
                                                     final MetricType type,
                                                     final Metric metric,
                                                     final int currentDimension,
                                                     final String suffix) {
        if (type.isLeaf()) {
            return List.of(parsePrimitiveMetricEvent(
                    value,
                    type.getType(),
                    metric.getNamespace(),
                    suffix
            ));
        }
        final MetricKind metricKind = type.getType();
        return switch (metricKind) {
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
            case INFER -> throw new IllegalStateException("Cannot infer type for non-leaf structural type (containers)");
            default -> throw new IllegalStateException("Unknown metric container type: " + metricKind.name());
        };
    }

    private List<Proto.Event> parseListMetricEvents(final Object value,
                                                    final MetricType type,
                                                    final Metric metric,
                                                    final int currentDimension,
                                                    final String suffix) {
        if (!(value instanceof List<?> list)) {
            LOGGER.warn(
                    "Expected raw metric value to be of type {}, instead got {}, skipping",
                    List.class.getName(),
                    value.getClass().getName()
            );
            return List.of();
        }
        final List<Proto.Event> events = new ArrayList<>();
        int index = 0;
        for (final Object component : list) {
            final String nextSuffix = formatSuffix(
                    suffix,
                    "",
                    currentDimension,
                    index,
                    metric
            );
            events.addAll(parseCoerceMetricEvents(
                    component,
                    type.getStructure(),
                    metric,
                    currentDimension + 1,
                    nextSuffix
            ));
            index++;
        }
        return events;
    }

    @SuppressWarnings("unchecked")
    private List<Proto.Event> parseMapMetricEvents(final Object value,
                                                   final MetricType type,
                                                   final Metric metric,
                                                   final int currentDimension,
                                                   final String suffix) {
        if (!(value instanceof Map<?, ?> mapValue)) {
            LOGGER.warn(
                    "Expected raw metric value to be of type {}, instead got {}, skipping",
                    Map.class.getName(),
                    value.getClass().getName()
            );
            return List.of();
        }
        final Map<String, ?> map = (Map<String, ?>) mapValue;
        final List<Proto.Event> events = new ArrayList<>();
        int index = 0;
        for (final Map.Entry<String, ?> entry : map.entrySet()) {
            final String nextSuffix = formatSuffix(
                    suffix,
                    entry.getKey(),
                    currentDimension,
                    index,
                    metric
            );
            events.addAll(parseCoerceMetricEvents(
                    entry.getValue(),
                    type.getStructure(),
                    metric,
                    currentDimension + 1,
                    nextSuffix
            ));
            index++;
        }
        return events;
    }

    private String formatSuffix(final String current,
                                final String name,
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
        if (name != null) {
            nextSuffix = nextSuffix.replace("{name}", name);
        }
        return nextSuffix;
    }

    private Proto.Event parsePrimitiveMetricEvent(final Object value,
                                                  final MetricKind type,
                                                  final String metricNamespace,
                                                  final String suffix) {
        // TODO: Remove test values set on builder here
        final Proto.Event.Builder builder = this.eventTemplate.toBuilder()
                .setService(metricNamespace + suffix)
                .addTags("tag1")
                .addTags("tag2")
                .setTtl(RandomUtils.nextFloat(0f, 10_000f))
                .setTimeMicros(ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now()))
                .addAttributes(
                        Proto.Attribute.newBuilder()
                                .setKey("attr1")
                                .setValue("value1")
                                .build()
                ).addAttributes(
                        Proto.Attribute.newBuilder()
                                .setKey("attr2")
                                .setValue("value2")
                                .build()
                );
        return type.buildMetric(builder, value).build();
    }

}
