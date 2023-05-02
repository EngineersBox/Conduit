package com.engineersbox.conduit_v2.processing;

import com.engineersbox.conduit.pipeline.BatchingConfiguration;
import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;
import com.engineersbox.conduit_v2.processing.task.TaskExecutorPool;
import com.engineersbox.conduit_v2.retrieval.content.ContentManager;
import com.engineersbox.conduit_v2.retrieval.content.ContentManagerFactory;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.RiemannClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class Conduit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conduit.class);

    private final MetricsSchemaProvider schemaProvider;
    private final TaskExecutorPool executor;
    private final BatchingConfiguration batchingConfiguration;

    public Conduit(final MetricsSchemaProvider schemaProvider,
                   final Supplier<RiemannClient> clientProvider,
                   final BatchingConfiguration batchingConfiguration) {
        this.schemaProvider = schemaProvider;
        // TODO: Implement the client provider
        this.executor = new TaskExecutorPool(clientProvider);
        this.batchingConfiguration = batchingConfiguration; // TODO: Get this from somewhere
    }

    public void execute() {
        final MetricsSchema schema = this.schemaProvider.provide();
        this.schemaProvider.lock();
        final ContentManager<?,?,?,?> contentManager = ContentManagerFactory.construct(
                schema,
                null,
                Function.identity() // TODO: allow customisation via config
        );
        final AtomicReference<RetrievalHandler<Metric>> retrieverReference = new AtomicReference<>(contentManager);
        final List<List<Metric>> batchedMetricWorkloads = List.of();
        // TODO: Update this when MetricSchema changed to use new Metric class or new metric class replaced with old one
        /* final List<List<Metric>> batchedMetricWorkloads = */ this.batchingConfiguration.splitWorkload(Collections.singleton(schema.values()));
        contentManager.poll();
        batchedMetricWorkloads.forEach((final List<Metric> metrics) -> {
            handleMetric(
                    metrics,
                    retrieverReference,
                    schema.getHandler() != null
            );
        });
        this.schemaProvider.refresh();
        this.schemaProvider.unlock();
    }

    private void handleMetric(final List<Metric> metrics,
                              final AtomicReference<RetrievalHandler<Metric>> retriever,
                              final boolean hasLuaHandlers) {
        final Proto.Event eventTemplate = null; // Get from schema
        this.executor.submit(new MetricProcessingTask(
                metrics,
                eventTemplate,
                retriever,
                hasLuaHandlers
        ));
    }

}
