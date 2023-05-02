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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Conduit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conduit.class);

    private final MetricsSchemaProvider schemaProvider;
    private final TaskExecutorPool executor;
    private final BatchingConfiguration batchingConfiguration;
    private boolean executing = false;
    private final List<? super ForkJoinTask<?>> tasks;
    private final boolean async;
    private final boolean schemaProviderLocking;

    public Conduit(final MetricsSchemaProvider schemaProvider,
                   final Supplier<RiemannClient> clientProvider,
                   final BatchingConfiguration batchingConfiguration) {
        this.schemaProvider = schemaProvider;
        // TODO: Implement the client provider
        this.executor = new TaskExecutorPool(clientProvider);
        this.batchingConfiguration = batchingConfiguration; // TODO: Get this from somewhere
        this.tasks = new ArrayList<>();
        // TODO: These flags need to come from config
        this.async = false;
        this.schemaProviderLocking = true;
    }

    public void execute() {
        this.executing = true;
        final MetricsSchema schema = this.schemaProvider.provide();
        if (this.schemaProviderLocking) {
            this.schemaProvider.lock();
        }
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
        submitTasks(batchedMetricWorkloads.stream()
                .map((final List<Metric> metrics) -> new MetricProcessingTask(
                        metrics,
                        schema.getEventTemplate(),
                        retrieverReference,
                        schema.getHandler() != null
                )).toArray(MetricProcessingTask[]::new));

        this.schemaProvider.refresh();
        if (this.schemaProviderLocking) {
            this.schemaProvider.unlock();
        }
        this.executing = false;
        this.tasks.clear();
    }

    private void submitTasks(final MetricProcessingTask ...tasks) {
        if (this.async) {
            // NOTE: For complete async behaviour need async = true and schemaProviderLocking = false
            this.executor.invokeAll(
                    this.tasks::add,
                    tasks
            );
        }
        this.executor.waitAll(tasks);
    }

    public boolean isExecuting() {
        return this.executing;
    }

    public List<? super ForkJoinTask<?>> getTasks() {
        return this.async ? this.tasks : null;
    }

}
