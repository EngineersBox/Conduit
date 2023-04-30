package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit.schema.DimensionIndex;
import com.engineersbox.conduit.schema.metric.MetricContainerType;
import com.engineersbox.conduit.schema.metric.MetricType;
import com.engineersbox.conduit.schema.metric.MetricValueType;
import com.engineersbox.conduit_v2.processing.pipeline.Pipeline;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.RiemannClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MetricProcessingTask implements ClientBoundWorkerTask {

    private final Metric initialMetric; // Received from pipeline
    private final Proto.Event eventTemplate;
    private final AtomicReference<RetrievalHandler<Metric>> retreiver;
    private final Pipeline pipeline;

    public MetricProcessingTask(final Metric metric,
                                final Proto.Event eventTemplate,
                                final AtomicReference<RetrievalHandler<Metric>> retriever) {
        this.initialMetric = metric;
        this.eventTemplate = eventTemplate;
        this.retreiver = retriever;
        this.pipeline = null; // Initialise this
    }

    @Override
    public void accept(final RiemannClient riemannClient) {
        final Proto.Event[] events = parseCoerceMetricEvents(
                this.retreiver.get().lookup(this.initialMetric),
                this.initialMetric.getType(),
                this.initialMetric,
                0,
                ""
        ).toArray(Proto.Event[]::new);
        try {
            riemannClient.sendEvents(events).deref(1, TimeUnit.SECONDS);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
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
                    type.getChild().get(),
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
        final Proto.Event.Builder builder = this.eventTemplate.toBuilder()
                .setService(metricNamespace + suffix);
        return type.buildMetric(builder, value).build();
    }

}
