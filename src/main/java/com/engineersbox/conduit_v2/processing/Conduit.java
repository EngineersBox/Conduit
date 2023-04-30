package com.engineersbox.conduit_v2.processing;

import com.engineersbox.conduit.pipeline.BatchingConfiguration;
import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;
import com.engineersbox.conduit_v2.processing.task.TaskExecutorPool;
import com.engineersbox.conduit_v2.retrieval.content.ContentManager;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Conduit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conduit.class);

    private final MetricsSchemaProvider schemaProvider = null;
    private final ContentManager<?, ?, ?, ?> contentManager = null;
    private final TaskExecutorPool executor;
    private final BatchingConfiguration batchingConfiguration;

    public Conduit() {
        // TODO: Implement the client provider
        this.executor = new TaskExecutorPool(() -> null);
        this.batchingConfiguration = null; // TODO: Get this from somewhere
    }

    public void execute() {
        final AtomicReference<RetrievalHandler<Metric>> retrieverReference = new AtomicReference<>(this.contentManager);
        final List<List<Metric>> batchedMetricWorkloads = List.of();
        // TODO: Update this when MetricSchema changed to use new Metric class or new metric class replaced with old one
        /* final List<List<Metric>> batchedMetricWorkloads = */ this.batchingConfiguration.splitWorkload(Collections.singleton(this.schemaProvider.provide().values()));
        batchedMetricWorkloads.forEach((final List<Metric> metrics) -> {
            handleMetric(
                    metrics,
                    retrieverReference
            );
        });
        this.schemaProvider.refresh();
    }

    private void handleMetric(final List<Metric> metrics,
                              final AtomicReference<RetrievalHandler<Metric>> retriever) {
        final Proto.Event eventTemplate = null; // Get from schema
        this.executor.submit(new MetricProcessingTask(
                metrics,
                eventTemplate,
                retriever
        ));
    }

}
