package com.engineersbox.conduit_v2.processing.event;

import com.engineersbox.conduit_v2.processing.schema.metric.DimensionIndex;
import com.engineersbox.conduit_v2.processing.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.schema.metric.MetricKind;
import com.engineersbox.conduit_v2.processing.schema.metric.MetricType;
import io.riemann.riemann.Proto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventTransformer {

    private final Proto.Event eventTemplate;

    public EventTransformer(final Proto.Event eventTemplate) {
        this.eventTemplate = eventTemplate;
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
            default -> throw new IllegalStateException("Unknown metric container type: " + metricKind.name());
        };
    }

    private List<Proto.Event> parseListMetricEvents(final Object value,
                                                    final MetricType type,
                                                    final Metric metric,
                                                    final int currentDimension,
                                                    final String suffix) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
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
            return List.of();
        }
        final Map<String, ?> map = (Map<String, ?>) mapValue;
        final List<Proto.Event> events = new ArrayList<>();
        int index = 0;
        for (final Map.Entry<String, ?> entry : map.entrySet()) {
            // TODO: supply entry key to replace {name} template variable
            final String nextSuffix = formatSuffix(
                    suffix,
                    currentDimension,
                    index,
                    metric
            );
            events.addAll(parseCoerceMetricEvents(
                    entry.getValue(),
                    type.getStructure(),
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
                                                  final MetricKind type,
                                                  final String metricNamespace,
                                                  final String suffix) {
        final Proto.Event.Builder builder = this.eventTemplate.toBuilder()
                .setService(metricNamespace + suffix)
                .addTags("tag1")
                .addTags("tag2")
                .setTtl(5732.9573f)
                .setTimeMicros(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
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
