package com.engineersbox.conduit_v2.processing.pipeline;

import com.engineersbox.conduit.schema.metric.MetricType;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import io.riemann.riemann.Proto;

import java.util.List;

public class EventTransformer {

    public static List<Proto.Event> transform(final Metric metric,
                                              final Object value) {
        return List.of();
    }

    private static List<Proto.Event> transform0(final Metric metric,
                                                final MetricType type,
                                                final Object value,
                                                final int currentDimension,
                                                final String suffix) {
        return List.of();
    }

}
