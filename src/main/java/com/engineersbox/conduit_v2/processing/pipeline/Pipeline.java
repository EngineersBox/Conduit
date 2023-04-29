package com.engineersbox.conduit_v2.processing.pipeline;

import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.processing.task.TaskExecutorPool;
import com.engineersbox.conduit_v2.retrieval.content.ContentManager;
import io.riemann.riemann.Proto;

import java.util.List;

public class Pipeline {

    private final MetricsSchemaProvider schemaProvider = null;
    private final ContentManager<?> contentManager = null;
    private final EventTransformer transformer = null;
    private final TaskExecutorPool executor;

    public Pipeline() {
        // TODO: Implement the client provider
        this.executor = new TaskExecutorPool(() -> null);
    }

    private List<Proto.Event> handleMetric(final Metric metric) {
        final Object value = this.contentManager.retrieveMetricValue(metric);
        return this.transformer.transform(metric, value);
    }

}
