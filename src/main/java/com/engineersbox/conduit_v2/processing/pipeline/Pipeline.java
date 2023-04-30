package com.engineersbox.conduit_v2.processing.pipeline;

import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;
import com.engineersbox.conduit_v2.processing.task.TaskExecutorPool;
import com.engineersbox.conduit_v2.retrieval.content.ContentManager;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Pipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

    private final MetricsSchemaProvider schemaProvider = null;
    private final ContentManager<?, ?, ?, ?> contentManager = null;
    private final EventTransformer transformer = null;
    private final TaskExecutorPool executor;

    public Pipeline() {
        // TODO: Implement the client provider
        this.executor = new TaskExecutorPool(() -> null);
    }

    public void execute() {
        final AtomicReference<RetrievalHandler<Metric>> retrieverReference = new AtomicReference<>(this.contentManager);
        // Update metric to be correct type in MetricSchema definition
        this.schemaProvider.provide().forEach((final String key, final Object metric) -> {
            LOGGER.debug("Submitting metric for parsing: {}", key);
            handleMetric(
                    (Metric) metric,
                    retrieverReference
            );
        });
        this.schemaProvider.refresh();
    }

    private void handleMetric(final Metric metric,
                              final AtomicReference<RetrievalHandler<Metric>> retriever) {
        final Proto.Event eventTemplate = null; // Get from schema
        this.executor.submit(new MetricProcessingTask(
                metric,
                eventTemplate,
                retriever
        ));
    }

}
